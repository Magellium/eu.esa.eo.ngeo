package int_.esa.eo.ngeo.downloadmanager.plugin.http;

import int_.esa.eo.ngeo.downloadmanager.http.ResponseHeaderParser;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpRequestAndResponse;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.ProductDownloadProgressMonitor;
import int_.esa.eo.ngeo.downloadmanager.plugin.model.FileDownloadMetadata;
import int_.esa.eo.ngeo.downloadmanager.plugin.thread.AbortableFileDownload;
import int_.esa.eo.ngeo.downloadmanager.plugin.utils.MoveDirVisitor;
import int_.esa.eo.ngeo.downloadmanager.plugin.utils.Transferrer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.pse.umsso.client.UmssoCLException;
import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

/**
 * Runnable using HTTP protocol to download a part of file
 */
public class HttpFileDownloadRunnable implements Runnable, AbortableFileDownload {
    private static final String DEAD_DOWNLOAD_THREAD = "\n\n\n=============DEAD DOWNLOAD THREAD========================";

    private static final String GZIP_CONTENT_TYPE = "gzip";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpFileDownloadRunnable.class);

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
            UmSsoHttpRequestAndResponse fileDownloadRequestAndResponse = null;

            try {
                Path partiallyDownloadedPath = fileDownloadMetadata.getPartiallyDownloadedPath();
                Path downloadPathParent = partiallyDownloadedPath.getParent();
                if (!Files.exists(downloadPathParent)) {
                    Files.createDirectories(downloadPathParent);
                }
                destination = Files.newByteChannel(partiallyDownloadedPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                long currentFileSize = destination.size();

                URL fileUrl = fileDownloadMetadata.getFileURL();
                LOGGER.debug(String.format("Executing HTTP request for file download: %s", fileUrl.toString()));
                //file has been partially downloaded
                if (currentFileSize > 0) {
                    List<Header> headers = new ArrayList<>();
                    headers.add(new BasicHeader("Range", String.format("bytes=%s-%s", currentFileSize, fileDownloadMetadata.getDownloadSize())));
                    fileDownloadRequestAndResponse = umSsoHttpClient.executeGetRequest(fileUrl, headers);
                }else{
                    fileDownloadRequestAndResponse = umSsoHttpClient.executeGetRequest(fileUrl);
                }
                UmssoHttpResponse response = fileDownloadRequestAndResponse.getResponse();

                int responseCode = response.getStatusLine().getStatusCode();
                LOGGER.debug(String.format("HTTP response code for file download: %s", responseCode));
                switch (responseCode) {
                case HttpStatus.SC_OK:
                case HttpStatus.SC_PARTIAL_CONTENT:
                    boolean hasTransferCompleted = false;

                    if(getFileDownloadStatus() == EDownloadStatus.RUNNING) {
                        transferrer = new Transferrer(transferrerReadLength);
                        InputStream bodyAsStream = getBodyAsStream(response);

                        ReadableByteChannel source = Channels.newChannel(bodyAsStream);
                        hasTransferCompleted = transferrer.doTransfer(source, destination, fileDownloadMetadata.getUuid(), productDownloadProgressMonitor);
                    }

                    if(hasTransferCompleted) {
                        destination.close();

                        Path moveFromPath = fileDownloadMetadata.getPartiallyDownloadedPath();
                        Path moveToPath = fileDownloadMetadata.getCompletelyDownloadedPath();

                        LOGGER.debug(String.format("Moving completed file. %n From: %s%n To: %s", moveFromPath.toAbsolutePath().toString(), moveToPath.toAbsolutePath().toString()));
                        LOGGER.debug(String.format("Does the file name consist of a folder (metalink file name with folder scenario)? %s", Files.isDirectory(moveFromPath)));
                        if(Files.isDirectory(moveFromPath)) {
                            //Move the "top level" directory specified in the file name
                            Files.walkFileTree(moveFromPath, new MoveDirVisitor(moveToPath));
                        }else{
                            //File name does not consist of a folder, therefore move the file
                            Files.move(moveFromPath, moveToPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                        setFileDownloadStatus(EDownloadStatus.COMPLETED);
                        productDownloadProgressMonitor.notifyOfFileStatusChange(getFileDownloadStatus(), null);
                        productDownloadProgressMonitor.notifyOfCompletedPath(fileDownloadMetadata.getUuid(), moveToPath);
                    }

                    break;
                default:
                    setFileDownloadStatus(EDownloadStatus.IN_ERROR);
                    productDownloadProgressMonitor.notifyOfFileStatusChange(getFileDownloadStatus(), String.format("Response code for download of file is %s.", responseCode));
                    break;
                }
            } catch (FileSystemException ex) {
                LOGGER.error(String.format("%s for file %s", ex.getClass().getName(), ex.getFile()));
                LOGGER.error(DEAD_DOWNLOAD_THREAD, ex);
                setFileDownloadStatus(EDownloadStatus.IN_ERROR);
                productDownloadProgressMonitor.notifyOfFileStatusChange(getFileDownloadStatus(), String.format("File system error for file %s", ex.getFile()));
            } catch (UmssoCLException | IOException ex) {
                LOGGER.error(DEAD_DOWNLOAD_THREAD, ex);
                setFileDownloadStatus(EDownloadStatus.IN_ERROR);
                productDownloadProgressMonitor.notifyOfFileStatusChange(getFileDownloadStatus(), ex.getLocalizedMessage());

            } catch (Exception ex) {
                LOGGER.error(DEAD_DOWNLOAD_THREAD, ex);
                setFileDownloadStatus(EDownloadStatus.IN_ERROR);
                productDownloadProgressMonitor.notifyOfFileStatusChange(getFileDownloadStatus(), ex.getLocalizedMessage());
            } finally {
                if (fileDownloadRequestAndResponse != null) {
                    fileDownloadRequestAndResponse.cleanupHttpResources();
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

    /*
     * Get the body of the response as a stream.
     * If the content encoding of the response is gzip, provide a GZIPInputStream which will handle the compressed stream
     */
    private InputStream getBodyAsStream(UmssoHttpResponse response) throws IOException {
        String contentEncoding = new ResponseHeaderParser().searchForResponseHeaderValue(response.getHeaders(), HttpHeaders.CONTENT_ENCODING);
        if(StringUtils.isNotEmpty(contentEncoding) && contentEncoding.equals(GZIP_CONTENT_TYPE)) {
            return new GZIPInputStream(response.getBodyAsStream());
        }else{
            return response.getBodyAsStream();
        }
    }
}
