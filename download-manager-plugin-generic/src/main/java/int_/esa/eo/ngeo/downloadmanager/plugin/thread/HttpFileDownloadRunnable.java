package int_.esa.eo.ngeo.downloadmanager.plugin.thread;

import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.ProductDownloadProgressMonitor;
import int_.esa.eo.ngeo.downloadmanager.plugin.model.FileDownloadMetadata;
import int_.esa.eo.ngeo.downloadmanager.plugin.utils.Transferrer;
import int_.esa.umsso.UmSsoHttpClient;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.pse.umsso.client.UmssoCLException;

/**
 * Runnable using HTTP protocol to download a part of file
 */
public class HttpFileDownloadRunnable implements Runnable, AbortableFileDownload {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbortableFileDownload.class);

	private FileDownloadMetadata fileDownloadMetadata;
	private ProductDownloadProgressMonitor productDownloadProgressMonitor;
	private UmSsoHttpClient umSsoHttpClient;
	private int transferrerReadLength;
	private Transferrer transferrer;
	//This status is used to indicate the status of an individual download, and uses the same enum as the product download status
	private EDownloadStatus fileDownloadStatus;
	
	public HttpFileDownloadRunnable(FileDownloadMetadata fileMetadata, ProductDownloadProgressMonitor productDownloadProgressMonitor, UmSsoHttpClient umSsoHttpClient, int transferrerReadLength) {
		this.fileDownloadMetadata = fileMetadata;
		this.productDownloadProgressMonitor = productDownloadProgressMonitor;
		this.umSsoHttpClient = umSsoHttpClient;
		this.transferrerReadLength = transferrerReadLength;
		this.setFileDownloadStatus(EDownloadStatus.NOT_STARTED);
	}
	
	public void run() {
		if(getFileDownloadStatus() == EDownloadStatus.NOT_STARTED) {
			setFileDownloadStatus(EDownloadStatus.RUNNING);
			SeekableByteChannel destination = null;
		
			GetMethod method = new GetMethod(fileDownloadMetadata.getFileURL().toString());
	
			try {
				Path partiallyDownloadedPath = fileDownloadMetadata.getPartiallyDownloadedPath();
				Path downloadPathParent = partiallyDownloadedPath.getParent();
				if (!Files.exists(downloadPathParent)) {
					Files.createDirectories(downloadPathParent);
				}
				destination = Files.newByteChannel(partiallyDownloadedPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				long currentFileSize = destination.size();
				
				//file has been partially downloaded
				if (currentFileSize > 0) {
					method.addRequestHeader("Range", String.format("bytes=%s-%s", currentFileSize, fileDownloadMetadata.getDownloadSize()));
				}
				
				umSsoHttpClient.executeHttpRequest(method);
				
				int responseCode = method.getStatusCode();
				switch (responseCode) {
				case HttpURLConnection.HTTP_OK:
				case HttpURLConnection.HTTP_PARTIAL:
					boolean hasTransferCompleted = false;
					
					if(getFileDownloadStatus() == EDownloadStatus.RUNNING) {
						transferrer = new Transferrer(transferrerReadLength);
						ReadableByteChannel source = Channels.newChannel(method.getResponseBodyAsStream());
						hasTransferCompleted = transferrer.doTransfer(source, destination, fileDownloadMetadata.getUuid(), productDownloadProgressMonitor);
					}
					
					if(hasTransferCompleted) {
						Path completedPath = fileDownloadMetadata.getCompletelyDownloadedPath();
						Files.move(partiallyDownloadedPath, completedPath, StandardCopyOption.REPLACE_EXISTING);
						setFileDownloadStatus(EDownloadStatus.COMPLETED);
						productDownloadProgressMonitor.notifyOfFileStatusChange(getFileDownloadStatus(), null);
						productDownloadProgressMonitor.notifyOfCompletedPath(fileDownloadMetadata.getUuid(), completedPath);
					}

					break;
				default:
					setFileDownloadStatus(EDownloadStatus.IN_ERROR);
					productDownloadProgressMonitor.notifyOfFileStatusChange(getFileDownloadStatus(), String.format("Response code for download of file is %s.", responseCode));
					break;
				}
			} catch (FileSystemException ex) {
				LOGGER.error(String.format("%s for file %s", ex.getClass().getName(), ex.getFile()));
				LOGGER.error("\n\n\n=============DEAD DOWNLOAD THREAD========================", ex);
				setFileDownloadStatus(EDownloadStatus.IN_ERROR);
				productDownloadProgressMonitor.notifyOfFileStatusChange(getFileDownloadStatus(), String.format("File system error for file %s", ex.getFile()));
			} catch (UmssoCLException | IOException ex) {
				LOGGER.error("\n\n\n=============DEAD DOWNLOAD THREAD========================", ex);
				setFileDownloadStatus(EDownloadStatus.IN_ERROR);
				productDownloadProgressMonitor.notifyOfFileStatusChange(getFileDownloadStatus(), ex.getLocalizedMessage());
				
			} catch (Throwable throwable) {
				LOGGER.error("\n\n\n=============DEAD DOWNLOAD THREAD========================", throwable);
				setFileDownloadStatus(EDownloadStatus.IN_ERROR);
				productDownloadProgressMonitor.notifyOfFileStatusChange(getFileDownloadStatus(), throwable.getLocalizedMessage());
			} finally {
				if (method != null) {
					method.abort();
					method.releaseConnection();
				}
				if (destination != null && destination.isOpen()) { 
					IOUtils.closeQuietly(destination);
				}
			}
		}
	}
	
	public synchronized void abortFileDownload(EDownloadStatus downloadStatus) {
		setFileDownloadStatus(downloadStatus);
		productDownloadProgressMonitor.notifyOfFileStatusChange(getFileDownloadStatus(), null);
		if(transferrer != null) {
			transferrer.abortTransfer();
		}
	}

	public synchronized EDownloadStatus getFileDownloadStatus() {
		return fileDownloadStatus;
	}

	public synchronized void setFileDownloadStatus(EDownloadStatus fileDownloadStatus) {
		this.fileDownloadStatus = fileDownloadStatus;
	}
}
