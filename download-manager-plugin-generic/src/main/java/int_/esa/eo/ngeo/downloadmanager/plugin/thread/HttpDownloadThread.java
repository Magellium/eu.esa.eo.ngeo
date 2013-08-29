package int_.esa.eo.ngeo.downloadmanager.plugin.thread;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.ProductDownloadProgressMonitor;
import int_.esa.eo.ngeo.downloadmanager.plugin.model.FileDownloadMetadata;
import int_.esa.eo.ngeo.downloadmanager.plugin.utils.Transferrer;
import int_.esa.umsso.UmSsoHttpClient;

import java.io.IOException;
import java.nio.channels.FileChannel;
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
 * Thread using HTTP protocol to download a part of file
 */
public class HttpDownloadThread implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloadThread.class);

	private FileDownloadMetadata fileDownloadMetadata;
	private ProductDownloadProgressMonitor productDownloadProgressMonitor;
	private UmSsoHttpClient umSsoHttpClient;
	private int transferrerReadLength;
	
	public HttpDownloadThread(FileDownloadMetadata fileMetadata, ProductDownloadProgressMonitor productDownloadProgressMonitor, UmSsoHttpClient umSsoHttpClient, int transferrerReadLength) {
		this.fileDownloadMetadata = fileMetadata;
		this.productDownloadProgressMonitor = productDownloadProgressMonitor;
		this.umSsoHttpClient = umSsoHttpClient;
		this.transferrerReadLength = transferrerReadLength;
	}
	
	public void run() {
		FileChannel destination = null;
		
		GetMethod method = new GetMethod(fileDownloadMetadata.getFileURL().toString());

		try {
			Path partiallyDownloadedPath = fileDownloadMetadata.getPartiallyDownloadedPath();
			Path downloadPathParent = partiallyDownloadedPath.getParent();
			if (!Files.exists(downloadPathParent)) {
				Files.createDirectories(downloadPathParent);
			}
			destination = (FileChannel) Files.newByteChannel(partiallyDownloadedPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
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
				
				Transferrer transferrer = new Transferrer(productDownloadProgressMonitor, transferrerReadLength);
				transferrer.doTransfer(destination, method.getResponseBodyAsStream(), fileDownloadMetadata, currentFileSize, productDownloadProgressMonitor);
				
				EDownloadStatus downloadStatus = productDownloadProgressMonitor.getStatus();
				switch (downloadStatus) {
				case IDLE:
				case COMPLETED:
				case IN_ERROR:
					throw new DMPluginException(String.format("Download Status should never be %s whilst in the progress of downloading a file.", downloadStatus));
				case PAUSED:
					//nothing special to do here; we have already updated the product file details and notified the listeners in the last byte written.
					break;
				case CANCELLED:
					//the deleting of partially downloaded files should be handled by the tidyUpCancelledDownload method
					break;
				case NOT_STARTED:
					// this state will be set when the product has been running and the download manager is being stopped gracefully
				case RUNNING:
					Path completedPath = fileDownloadMetadata.getCompletelyDownloadedPath();
					Files.move(partiallyDownloadedPath, completedPath, StandardCopyOption.REPLACE_EXISTING);
					productDownloadProgressMonitor.setFileDownloadComplete(fileDownloadMetadata.getUuid(), completedPath);
					break;
				}
				break;
			default:
				productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, String.format("Response code for download of file is %s.", responseCode));
				break;
			}
		} catch (UmssoCLException | IOException | DMPluginException ex) {
			productDownloadProgressMonitor.setError(ex);
			LOGGER.error("\n\n\n=============DEAD DOWNLOAD THREAD========================", ex);
		} catch (Throwable throwable) {
			LOGGER.error("\n\n\n=============DEAD DOWNLOAD THREAD========================", throwable);
			productDownloadProgressMonitor.setError(new DMPluginException(throwable));
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
