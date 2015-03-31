package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.SchemaNotFoundException;
import int_.esa.eo.ngeo.downloadmanager.http.ResponseHeaderParser;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpConnectionSettings;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpRequestAndResponse;
import int_.esa.eo.ngeo.downloadmanager.plugin.http.HttpFileDownloadRunnable;
import int_.esa.eo.ngeo.downloadmanager.plugin.http.IdleCheckThread;
import int_.esa.eo.ngeo.downloadmanager.plugin.model.FileDownloadMetadata;
import int_.esa.eo.ngeo.downloadmanager.plugin.model.ProductDownloadMetadata;
import int_.esa.eo.ngeo.downloadmanager.plugin.utils.PathResolver;
import int_.esa.eo.ngeo.downloadmanager.status.ValidDownloadStatusForUserAction;
import int_.esa.eo.ngeo.downloadmanager.transform.SchemaRepository;
import int_.esa.eo.ngeo.downloadmanager.transform.XMLWithSchemaTransformer;
import int_.esa.eo.ngeo.schema.ngeobadrequestresponse.BadRequestResponse;
import int_.esa.eo.ngeo.schema.ngeomissingproductresponse.MissingProductResponse;
import int_.esa.eo.ngeo.schema.ngeoproductdownloadresponse.ProductDownloadResponse;
import int_.esa.eo.ngeo.schema.ngeoproductdownloadresponse.ProductDownloadResponseType;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.metalinker.FileType;
import org.metalinker.Metalink;
import org.metalinker.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.pse.umsso.client.UmssoCLException;
import com.siemens.pse.umsso.client.util.UmssoHttpResponse;
import java.net.URISyntaxException;
import org.apache.http.client.utils.URIBuilder;

/**
 * This class is a generic HTTP Download Process, which can be used by any
 * plugin which downloads from an HTTP source.
 */
/**
 * @author lkn
 *
 */
public abstract class DownloadProcess implements IDownloadProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadProcess.class);

    private static final String UNABLE_TO_CANCEL_DOWNLOAD_STATUS = "Unable to cancel download, status is %s";
    private static final String UNABLE_TO_PARSE_RESPONSE_DETAILS = "HTTP response code %s (%s), unable to parse response details.";
    private static final String KEY_DOWNLOAD_THREAD_TIMEOUT_LENGTH_IN_HOURS = "DOWNLOAD_THREAD_TIMEOUT_LENGTH_IN_HOURS";
    private static final String KEY_TRANSFERRER_READ_LENGTH_IN_BYTES = "TRANSFERRER_READ_LENGTH_IN_BYTES";
    private static final String MIME_TYPE_METALINK = "application/metalink+xml";

    protected SchemaRepository schemaRepository;
    protected File baseProductDownloadDir;
    protected UmSsoHttpClient umSsoHttpClient;

    protected Properties pluginConfig;
    protected PathResolver pathResolver;

    protected URI productURI;
    protected ExecutorService fileDownloadExecutor;
    protected ScheduledExecutorService idleCheckExecutor;

    protected ProductDownloadMetadata productMetadata;
    protected ProductDownloadProgressMonitor productDownloadProgressMonitor;

    private final IProductDownloadListener productDownloadListener;

    public DownloadProcess(URI productURI, File downloadDir, IProductDownloadListener productDownloadListener, UmSsoHttpConnectionSettings umSsoHttpConnectionSettings, Properties pluginConfig, SchemaRepository schemaRepository) {
        this.productURI = productURI;
        this.baseProductDownloadDir = downloadDir;
        this.schemaRepository = schemaRepository;
        this.pathResolver = new PathResolver();
        this.pluginConfig = pluginConfig;

        this.umSsoHttpClient = new UmSsoHttpClient(umSsoHttpConnectionSettings);
        this.productDownloadListener = productDownloadListener;
    }

    @Override
    public EDownloadStatus startDownload() throws DMPluginException {
        try {
            /* 
             * If the download has been cancelled do not continue with the download process.
             * This situation can occur when a waiting product is started after a STOP_IMMEDIATELY command is sent.
             */
            if (getProductDownloadProgressMonitor().getStatusWhenDownloadWasAborted() == EDownloadStatus.CANCELLED) {
                return EDownloadStatus.CANCELLED;
            }

            if (this.productMetadata == null) {
                retrieveDownloadDetails();
            }
            if (getStatus() == EDownloadStatus.NOT_STARTED) {
                downloadProduct();
            }
            if (getStatus() == EDownloadStatus.RUNNING && postDownloadProcess()) {
                getProductDownloadProgressMonitor().setStatus(EDownloadStatus.COMPLETED);
            }
        } catch (DMPluginException dmPluginException) {
            String errorMessage = String.format("Plugin Exception: %s", dmPluginException.getMessage());
            productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, errorMessage);
            throw dmPluginException;
        } catch (Throwable t) {
            LOGGER.error(String.format("Plugin Exception: %s - %s", productURI.toString(), t.getClass().getName(), t.getMessage(), t));
            productDownloadProgressMonitor.setError(new DMPluginException(String.format("Plugin Exception: %s", t.getMessage()), t));
        }
        return getStatus();
    }

    /*
     *  If the number of files has been determined, then pause each file download
     *  Otherwise, pause the product download
     */
    @Override
    public EDownloadStatus pauseDownload() throws DMPluginException {
        ValidDownloadStatusForUserAction validDownloadStatusForUserAction = new ValidDownloadStatusForUserAction();
        if (!validDownloadStatusForUserAction.getValidDownloadStatusesToExecutePauseAction().contains(getStatus())) {
            throw new DMPluginException(String.format("Unable to pause download, status is %s", getStatus()));
        }

        stopIdleCheckIfActive();

        if (getProductDownloadProgressMonitor().getFileDownloadList().size() > 0) {
            getProductDownloadProgressMonitor().abortFileDownloads(EDownloadStatus.PAUSED);
        } else {
            getProductDownloadProgressMonitor().setStatus(EDownloadStatus.PAUSED);
        }

        return getStatus();
    }

    @Override
    public EDownloadStatus resumeDownload() throws DMPluginException {
        ValidDownloadStatusForUserAction validDownloadStatusForUserAction = new ValidDownloadStatusForUserAction();
        if (!validDownloadStatusForUserAction.getValidDownloadStatusesToExecuteResumeAction().contains(getStatus())) {
            throw new DMPluginException(String.format("Unable to resume download, status is %s", getStatus()));
        }
        getProductDownloadProgressMonitor().setStatus(EDownloadStatus.NOT_STARTED);

        return getStatus();
    }

    @Override
    public EDownloadStatus cancelDownload() throws DMPluginException {
        EDownloadStatus previousDownloadStatus = getStatus();
        ValidDownloadStatusForUserAction validDownloadStatusForUserAction = new ValidDownloadStatusForUserAction();
        if (!validDownloadStatusForUserAction.getValidDownloadStatusesToExecuteCancelAction().contains(previousDownloadStatus)) {
            throw new DMPluginException(String.format(UNABLE_TO_CANCEL_DOWNLOAD_STATUS, getStatus()));
        }

        stopIdleCheckIfActive();

        switch (previousDownloadStatus) {
            case RUNNING:
            case NOT_STARTED:
                if (getProductDownloadProgressMonitor().getFileDownloadList().size() > 0) {
                    getProductDownloadProgressMonitor().abortFileDownloads(EDownloadStatus.CANCELLED);
                } else {
                    //no products are currently being downloaded, so start tidy-up
                    tidyUpAfterCancelledDownload();
                }
                break;
            case PAUSED:
            case IDLE:
                tidyUpAfterCancelledDownload();
                break;
            default:
                throw new DMPluginException(String.format(UNABLE_TO_CANCEL_DOWNLOAD_STATUS, previousDownloadStatus));
        }

        return getStatus();
    }

    @Override
    public EDownloadStatus getStatus() {
        return getProductDownloadProgressMonitor().getStatus();
    }

    @Override
    public File[] getDownloadedFiles() {
        //check if the number of completed files equals the number of files identified as to be downloaded
        int numberOfFilesInProduct = productMetadata.getFileMetadataList().size();
        if (getProductDownloadProgressMonitor().getNumberOfCompletedFiles() == numberOfFilesInProduct) {
            if (productMetadata.getMetalinkDownloadDirectory() != null) {
                return new File[]{productMetadata.getMetalinkDownloadDirectory().toFile()};
            } else {
                return new File[]{productMetadata.getFileMetadataList().get(0).getCompletelyDownloadedPath().toFile()};
            }
        }
        return null;
    }

    /* 
     *  This method is the last one called by the Download Manager on a IDownloadProcess instance.
     *  It is called by the Download Manager either:
     * -    after the status COMPLETED, CANCELLED or IN_ERROR has been notified by the plugin to the Download Manager and the reference of downloaded files has been retrieved by the later
     * -    when the Download Manager ends. In this second case, the plugin is expected to :
     *      -  if RUNNING           : stop the download
     *      -  if RUNNING or PAUSED : store onto disk the current download state (if possible) in order to restart it
     */
    @Override
    public void disconnect() throws DMPluginException {
        LOGGER.debug(String.format("Disconnecting download process for %s, status %s", productURI, getStatus()));
        if (idleCheckExecutor != null) {
            //no elegant shutdown needs to be performed on the idle checker
            idleCheckExecutor.shutdownNow();
        }
        if (getStatus() == EDownloadStatus.RUNNING) {
            getProductDownloadProgressMonitor().abortFileDownloads(EDownloadStatus.NOT_STARTED);
            getProductDownloadProgressMonitor().setStatus(EDownloadStatus.NOT_STARTED);
        }
        try {
            if (fileDownloadExecutor != null) {
                fileDownloadExecutor.shutdown();
                fileDownloadExecutor.awaitTermination(10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            LOGGER.error("Timeout occurred when attempting to shutdown download file threads");
        }
    }

    public void retrieveDownloadDetails() {
        UmSsoHttpRequestAndResponse productDownloadRequestAndResponse = null;

        ResponseHeaderParser responseHeaderParser = new ResponseHeaderParser();
        XMLWithSchemaTransformer xmlWithSchemaTransformer = new XMLWithSchemaTransformer(schemaRepository);
        try {
            LOGGER.debug(String.format("Executing HTTP request to retrieve product details: %s", productURI.toURL().toString()));
            productDownloadRequestAndResponse = umSsoHttpClient.executeHeadRequest(productURI.toURL());
            UmssoHttpResponse productDownloadResponse = productDownloadRequestAndResponse.getResponse();
            StatusLine statusLine = productDownloadResponse.getStatusLine();
            Header[] productDownloadResponseHeaders = productDownloadResponse.getHeaders();

            int responseCode = statusLine.getStatusCode();
            String reasonPhrase = statusLine.getReasonPhrase();
            LOGGER.debug(String.format("HTTP response: %s %s", responseCode, reasonPhrase));
            switch (responseCode) {

                case HttpStatus.SC_MOVED_PERMANENTLY:
                case HttpStatus.SC_MOVED_TEMPORARILY:
                case HttpStatus.SC_SEE_OTHER:
                    Header[] newHeaders = productDownloadResponse.getHeaders();
                    for (Header header : newHeaders) {
                        if (header.getName().equalsIgnoreCase("Location")) {
                            URL url = new URL(header.getValue());
                            URIBuilder builder = new URIBuilder();
                            builder.setHost(url.getHost())
                                    .setPort(url.getPort())
                                    .setPath(url.getPath())
                                    .setQuery(url.getQuery())
                                    .setScheme(url.getProtocol());

                            this.productURI = builder.build();
                        }
                    }
                    retrieveDownloadDetails();
                    break;
                case HttpStatus.SC_OK:
                    productMetadata = new ProductDownloadMetadata();
                    String contentType = responseHeaderParser.searchForResponseHeaderValue(productDownloadResponseHeaders, HttpHeaders.CONTENT_TYPE);
                    String productName,
                     resolvedProductName;

                    LOGGER.debug(String.format("Content type: %s", contentType));

                    if (contentType != null && contentType.contains(MIME_TYPE_METALINK)) {
                        Metalink metalink = xmlWithSchemaTransformer.deserializeAndInferSchema(productDownloadResponse.getBodyAsStream(), Metalink.class);
                        LOGGER.debug(String.format("metalink: %s", metalink));

                        Path metalinkFolderPath = pathResolver.determineFolderPath(productURI, baseProductDownloadDir);
                        productMetadata.setMetalinkDownloadDirectory(metalinkFolderPath);

                        List<FileType> fileList = metalink.getFiles().getFiles();
                        for (FileType fileType : fileList) {
                            List<Url> urlList = fileType.getResources().getUrls();
                            //Assumption: We are not handling parallel downloading at this point
                            if (urlList.size() > 0) {
                                Url firstUrlForFile = urlList.get(0);

                                productName = URLDecoder.decode(fileType.getName(), "UTF-8");
                                resolvedProductName = pathResolver.resolveDuplicateFilePath(productName, productMetadata.getTempMetalinkDownloadDirectory().toFile());
                                FileDownloadMetadata fileMetadata = getFileMetadataForMetalinkEntry(firstUrlForFile.getValue(), resolvedProductName, productMetadata.getTempMetalinkDownloadDirectory());
                                FileUtils.touch(fileMetadata.getPartiallyDownloadedPath().toFile());
                                LOGGER.debug(String.format("metalink file download size %s", fileMetadata.getDownloadSize()));
                                productMetadata.getFileMetadataList().add(fileMetadata);
                            }
                        }
                        LOGGER.debug(String.format("Product contains %s files", fileList.size()));
                        getProductDownloadProgressMonitor().notifyOfProductDetails(productMetadata.getMetalinkDownloadDirectory().getFileName().toString(), productMetadata.getFileMetadataList());
                        getProductDownloadProgressMonitor().setStatus(EDownloadStatus.NOT_STARTED);
                    } else {
                        //download is a single file
                        String filenameFromResponseHeader = responseHeaderParser.searchForFilename(productDownloadResponseHeaders);
                        long fileSize = responseHeaderParser.searchForContentLength(productDownloadResponseHeaders);
                        resolvedProductName = pathResolver.determineFileName(filenameFromResponseHeader, productURI, baseProductDownloadDir);

                        LOGGER.debug("Content-Disposition = " + filenameFromResponseHeader);
                        LOGGER.debug("Content-Length = " + fileSize);
                        LOGGER.debug("fileName = " + resolvedProductName);

                        FileDownloadMetadata fileMetadata = new FileDownloadMetadata(productURI.toURL(), resolvedProductName, fileSize, baseProductDownloadDir.toPath());
                        FileUtils.touch(fileMetadata.getPartiallyDownloadedPath().toFile());
                        productMetadata.getFileMetadataList().add(fileMetadata);

                        getProductDownloadProgressMonitor().notifyOfProductDetails(fileMetadata.getCompletelyDownloadedPath().getFileName().toString(), productMetadata.getFileMetadataList());
                    }
                    break;
                case HttpStatus.SC_ACCEPTED:
                    ProductDownloadResponse parsedProductDownloadResponse = xmlWithSchemaTransformer.deserializeAndInferSchema(productDownloadResponse.getBodyAsStream(), ProductDownloadResponse.class);
                    ProductDownloadResponseType productDownloadResponseCode = parsedProductDownloadResponse.getResponseCode();
                    if (productDownloadResponseCode == ProductDownloadResponseType.ACCEPTED || productDownloadResponseCode == ProductDownloadResponseType.IN_PROGRESS) {
                        long retryAfter = parsedProductDownloadResponse.getRetryAfter().longValue();
                        LOGGER.info(String.format("Product %s not available at this time, retry after %s seconds", productURI.toString(), retryAfter));

                        idleCheckExecutor = Executors.newSingleThreadScheduledExecutor();
                        IdleCheckThread idleCheckThread = new IdleCheckThread(this);
                        idleCheckExecutor.schedule(idleCheckThread, retryAfter, TimeUnit.SECONDS);

                        getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IDLE);
                    } else {
                        getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, String.format("Product download was accepted, but response code is %s", productDownloadResponseCode));
                    }
                    break;
                case HttpStatus.SC_PARTIAL_CONTENT:
                    //Partial download is not relevant at this stage
                    break;
                case HttpStatus.SC_BAD_REQUEST:
                    try {
                        BadRequestResponse badRequestResponse = xmlWithSchemaTransformer.deserializeAndInferSchema(productDownloadResponse.getBodyAsStream(), BadRequestResponse.class);
                        LOGGER.error(String.format("badRequestResponse: %s", badRequestResponse));
                        getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, badRequestResponse.getResponseMessage());
                    } catch (ParseException | SchemaNotFoundException ex) {
                        String errorMessage = String.format(UNABLE_TO_PARSE_RESPONSE_DETAILS, responseCode, reasonPhrase);
                        LOGGER.error(errorMessage, ex);
                    }
                    break;
                case HttpStatus.SC_FORBIDDEN:
                    getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, "Forbidden access.");
                    break;
                case HttpStatus.SC_NOT_FOUND:
                    try {
                        MissingProductResponse missingProductResponse = xmlWithSchemaTransformer.deserializeAndInferSchema(productDownloadResponse.getBodyAsStream(), MissingProductResponse.class);
                        LOGGER.error(String.format("missingProductResponse: %s", missingProductResponse));
                        getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, missingProductResponse.getResponseMessage());
                    } catch (ParseException | SchemaNotFoundException ex) {
                        String errorMessage = String.format(UNABLE_TO_PARSE_RESPONSE_DETAILS, responseCode, reasonPhrase);
                        LOGGER.error(errorMessage, ex);
                        getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, errorMessage);
                    }
                    break;
                default:
                    String unexpectedResponseCodeMessage = String.format("Unexpected response when retrieving product details, HTTP response code %s (%s)", responseCode, reasonPhrase);
                    LOGGER.error(unexpectedResponseCodeMessage);
                    getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, unexpectedResponseCodeMessage);
                    break;
            }
        } catch (IOException | DMPluginException | ParseException | SchemaNotFoundException | URISyntaxException ex) {
            LOGGER.error("Exception occurred whilst retrieving download details.", ex);
            getProductDownloadProgressMonitor().setError(ex);
        } catch (UmssoCLException ex) {
            LOGGER.error("UMSSO exception occurred whilst retrieving download details.", ex);
            Throwable cause = ex.getCause();
            if (cause != null) {
                getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, cause.getLocalizedMessage());
            } else {
                getProductDownloadProgressMonitor().setError(ex);
            }
        } finally {
            if (productDownloadRequestAndResponse != null) {
                productDownloadRequestAndResponse.cleanupHttpResources();
            }
        }
    }

    private FileDownloadMetadata getFileMetadataForMetalinkEntry(String fileDownloadLink, String fileName, Path metalinkDownloadDirectory) throws DMPluginException, IOException, UmssoCLException {
        ResponseHeaderParser responseHeaderParser = new ResponseHeaderParser();
        long fileSize;

        URL fileDownloadLinkURL = new URL(fileDownloadLink);

        UmSsoHttpRequestAndResponse metalinkRequestAndResponse = null;
        try {
            metalinkRequestAndResponse = umSsoHttpClient.executeHeadRequest(fileDownloadLinkURL);
            UmssoHttpResponse response = metalinkRequestAndResponse.getResponse();

            int metalinkFileResponseCode = response.getStatusLine().getStatusCode();
            if (metalinkFileResponseCode == HttpStatus.SC_OK) {
                fileSize = responseHeaderParser.searchForContentLength(response.getHeaders());
                LOGGER.debug(String.format("metalink file content length = %s", fileSize));
            } else {
                throw new DMPluginException(String.format("Unable to retrieve metalink file details from file URL %s", fileDownloadLink));
            }
        } finally {
            if (metalinkRequestAndResponse != null) {
                metalinkRequestAndResponse.cleanupHttpResources();
            }
        }

        return new FileDownloadMetadata(fileDownloadLinkURL, fileName, fileSize, metalinkDownloadDirectory);
    }

    protected void downloadProduct() {
        LOGGER.debug("Start of download product");
        List<FileDownloadMetadata> fileMetadataList = productMetadata.getFileMetadataList();
        int numberOfFilesInProduct = fileMetadataList.size();
        if (numberOfFilesInProduct > 0) {
            try {
                getProductDownloadProgressMonitor().setStatus(EDownloadStatus.RUNNING);
                String transferrerReadLengthProperty = pluginConfig.getProperty(KEY_TRANSFERRER_READ_LENGTH_IN_BYTES);
                int transferrerReadLength = Integer.parseInt(transferrerReadLengthProperty);

                if (fileDownloadExecutor != null && !fileDownloadExecutor.isShutdown() && !fileDownloadExecutor.isTerminated()) {
                    getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, String.format("Internal Error with downloading products, unable to assign product %s to download threads.", productURI.toString()));
                } else {
                    fileDownloadExecutor = Executors.newSingleThreadExecutor();
                    for (FileDownloadMetadata fileDownloadMetadata : fileMetadataList) {
                        if (!getProductDownloadProgressMonitor().isDownloadComplete(fileDownloadMetadata.getUuid())) {
                            HttpFileDownloadRunnable httpFileDownloadRunnable = new HttpFileDownloadRunnable(fileDownloadMetadata, getProductDownloadProgressMonitor(), umSsoHttpClient, transferrerReadLength);
                            getProductDownloadProgressMonitor().getFileDownloadList().add(httpFileDownloadRunnable);
                            fileDownloadExecutor.execute(httpFileDownloadRunnable);
                        }
                    }

                    fileDownloadExecutor.shutdown();

                    String downloadThreadTimeoutLengthInHoursProperty = pluginConfig.getProperty(KEY_DOWNLOAD_THREAD_TIMEOUT_LENGTH_IN_HOURS);
                    Long downloadThreadTimeoutLengthInHours = Long.parseLong(downloadThreadTimeoutLengthInHoursProperty);
                    boolean threadCompleted = fileDownloadExecutor.awaitTermination(downloadThreadTimeoutLengthInHours, TimeUnit.HOURS);
                    if (!threadCompleted) {
                        getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, String.format("Download for product %s timed out.", productURI.toString()));
                        fileDownloadExecutor.shutdownNow();
                    } else {
                        LOGGER.debug(String.format("Threads completed for download %s", productURI.toString()));
                        if (getProductDownloadProgressMonitor().getNumberOfCompletedFiles() == numberOfFilesInProduct) {
                            tidyUpAfterCompletedDownload(numberOfFilesInProduct);
                        } else {
                            // The number of completed files does not equal the number of files in the product, therefore one of the following has happened:
                            // * A pause or cancel command has been sent by the core, which has caused a termination of all product download threads.
                            // * An error has occurred when downloading the products, which has caused a termination of all product download threads.
                            // * The Download Manager is shutting down whilst the download is running, therefore the status was set to NOT_STARTED.
                            // * A programmatic error has occurred, where the status is not PAUSED, CANCELLED or IN_ERROR
                            EDownloadStatus statusWhenDownloadWasAborted = getProductDownloadProgressMonitor().getStatusWhenDownloadWasAborted();
                            switch (statusWhenDownloadWasAborted) {
                                case PAUSED:
                                case IN_ERROR:
                                case NOT_STARTED:
                                    getProductDownloadProgressMonitor().setStatus(statusWhenDownloadWasAborted);
                                    break;
                                case CANCELLED:
                                    tidyUpAfterCancelledDownload();
                                    break;
                                default:
                                    getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, "Product download threads have terminated unexpectedly, please contact support.");
                                    break;
                            }
                        }

                        fileDownloadExecutor.shutdownNow();
                    }
                }
            } catch (InterruptedException e) {
                getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, String.format("Download for product %s was interrupted.", productURI.toString()));
            }
        } else {
            getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, String.format("No files found for download of product %s.", productURI.toString()));
        }
    }

    private void stopIdleCheckIfActive() {
        if (idleCheckExecutor != null) {
            idleCheckExecutor.shutdownNow();
        }
    }

    private void tidyUpAfterCancelledDownload() {
        LOGGER.debug(String.format("Tidying up cancelled download %s", productURI.toString()));
        //delete files, both partial and complete
        if (productMetadata != null) {
            List<FileDownloadMetadata> fileMetadataList = productMetadata.getFileMetadataList();
            for (FileDownloadMetadata fileDownloadMetadata : fileMetadataList) {
                try {
                    if (getProductDownloadProgressMonitor().isDownloadComplete(fileDownloadMetadata.getUuid())) {
                        Files.deleteIfExists(fileDownloadMetadata.getCompletelyDownloadedPath());
                    } else {
                        Files.deleteIfExists(fileDownloadMetadata.getPartiallyDownloadedPath());
                    }
                } catch (IOException e) {
                    LOGGER.error(String.format("Unable to complete tidyup of file %s: %s", fileDownloadMetadata.getFileName(), e.getLocalizedMessage()));
                }
            }
        }
        getProductDownloadProgressMonitor().confirmCancelAfterTidyUp();
    }

    private void tidyUpAfterCompletedDownload(int numberOfFilesInProduct) {
        //check if a product download is a metalink, regardless of how many products are contained in the metalink
        if (productMetadata.getMetalinkDownloadDirectory() != null) {
            try {
                Files.move(productMetadata.getTempMetalinkDownloadDirectory(), productMetadata.getMetalinkDownloadDirectory(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                getProductDownloadProgressMonitor().setError(e);
            }
        }
    }

    /**
     * This method allows for further processing after the download has been
     * completed.
     *
     * @return whether the post download processing has completed successfully.
     * @throws int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException
     */
    public abstract boolean postDownloadProcess() throws DMPluginException;

    public synchronized ProductDownloadProgressMonitor getProductDownloadProgressMonitor() {
        if (productDownloadProgressMonitor == null) {
            productDownloadProgressMonitor = new ProductDownloadProgressMonitor(productDownloadListener);
        }
        return productDownloadProgressMonitor;
    }
}
