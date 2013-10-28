package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.SchemaNotFoundException;
import int_.esa.eo.ngeo.downloadmanager.plugin.model.FileDownloadMetadata;
import int_.esa.eo.ngeo.downloadmanager.plugin.model.ProductDownloadMetadata;
import int_.esa.eo.ngeo.downloadmanager.plugin.model.ProxyDetails;
import int_.esa.eo.ngeo.downloadmanager.plugin.thread.HttpFileDownloadRunnable;
import int_.esa.eo.ngeo.downloadmanager.plugin.thread.IdleCheckThread;
import int_.esa.eo.ngeo.downloadmanager.plugin.utils.PathResolver;
import int_.esa.eo.ngeo.downloadmanager.status.ValidDownloadStatusForUserAction;
import int_.esa.eo.ngeo.downloadmanager.transform.SchemaRepository;
import int_.esa.eo.ngeo.downloadmanager.transform.XMLWithSchemaTransformer;
import int_.esa.eo.ngeo.schema.ngeobadrequestresponse.BadRequestResponse;
import int_.esa.eo.ngeo.schema.ngeomissingproductresponse.MissingProductResponse;
import int_.esa.eo.ngeo.schema.ngeoproductdownloadresponse.ProductDownloadResponse;
import int_.esa.eo.ngeo.schema.ngeoproductdownloadresponse.ProductDownloadResponseType;
import int_.esa.umsso.UmSsoHttpClient;

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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.metalinker.FileType;
import org.metalinker.Metalink;
import org.metalinker.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.pse.umsso.client.UmssoCLException;

public class HTTPDownloadProcess implements IDownloadProcess {
	private static final String KEY_DOWNLOAD_THREAD_TIMEOUT_LENGTH_IN_HOURS = "DOWNLOAD_THREAD_TIMEOUT_LENGTH_IN_HOURS";
	private static final String KEY_TRANSFERRER_READ_LENGTH_IN_BYTES = "TRANSFERRER_READ_LENGTH_IN_BYTES";
	private static final String ENABLE_UMSSO_JCL_USE = "ENABLE_UMSSO_JCL_USE";
	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPDownloadProcess.class);
	
	private static final String MIME_TYPE_METALINK = "application/metalink+xml";

	private SchemaRepository schemaRepository;

	private URI productURI;
	
	private File baseProductDownloadDir;
	
	private ProductDownloadMetadata productMetadata;
	private ProductDownloadProgressMonitor productDownloadProgressMonitor;
	
	private ExecutorService fileDownloadExecutor;
	
	private ScheduledExecutorService idleCheckExecutor;
	
	private UmSsoHttpClient umSsoHttpClient;
	
	private Properties pluginConfig;
	private PathResolver pathResolver;
	
	public HTTPDownloadProcess(URI productURI, File downloadDir, IProductDownloadListener productDownloadListener, ProxyDetails proxyDetails, String umssoUsername, String umssoPassword, Properties pluginConfig, SchemaRepository schemaRepository) {
		this.productURI = productURI;
		this.baseProductDownloadDir = downloadDir;
		this.productDownloadProgressMonitor = new ProductDownloadProgressMonitor(productDownloadListener);
		this.schemaRepository = schemaRepository;
		this.pathResolver = new PathResolver();
		this.pluginConfig = pluginConfig;

		String enableUmssoJclUseString = pluginConfig.getProperty(ENABLE_UMSSO_JCL_USE);
		boolean enableUmssoJclUse = Boolean.parseBoolean(enableUmssoJclUseString);

		umSsoHttpClient = new UmSsoHttpClient(umssoUsername, umssoPassword, enableUmssoJclUse, proxyDetails.getProxyLocation(), proxyDetails.getProxyPort(), proxyDetails.getProxyUser(), proxyDetails.getProxyPassword());
	}
	
	public EDownloadStatus startDownload() throws DMPluginException {
		if(this.productMetadata == null) {
			retrieveDownloadDetails();
		}
		if(getStatus() == EDownloadStatus.NOT_STARTED) {
			downloadProduct();
		}

		return getStatus();
	}
	
	public void retrieveDownloadDetails() {
		HttpMethodBase productDownloadHeaders = null;
		HttpMethodBase productDownloadBody = null;
		XMLWithSchemaTransformer xmlWithSchemaTransformer = new XMLWithSchemaTransformer(schemaRepository);
		try {
			LOGGER.debug("About to construct UmSsoHttpClient");
			productDownloadHeaders = umSsoHttpClient.executeHeadRequest(productURI.toURL());
			int responseCode = productDownloadHeaders.getStatusCode();
			switch (responseCode) {
			case HttpStatus.SC_OK:
				productMetadata = new ProductDownloadMetadata();
				String contentType = productDownloadHeaders.getResponseHeader("Content-Type").getValue();
				String productName = "", resolvedProductName = "";

				if(contentType.contains(MIME_TYPE_METALINK)) {
					productDownloadBody = retrieveDownloadDetailsBody(productURI.toURL());
					Metalink metalink = xmlWithSchemaTransformer.deserializeAndInferSchema(productDownloadBody.getResponseBodyAsStream(), Metalink.class);					
					LOGGER.debug(String.format("metalink: %s", metalink));

					Path metalinkFolderPath = pathResolver.determineFolderPath(productURI, baseProductDownloadDir);
					productMetadata.setMetalinkDownloadDirectory(metalinkFolderPath);

					List<FileType> fileList = metalink.getFiles().getFiles();
					for (FileType fileType : fileList) {
						List<Url> urlList = fileType.getResources().getUrls();
						//Assumption: We are not handling parallel downloading at this point
						if(urlList.size() > 0) {
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
					productDownloadProgressMonitor.notifyOfProductDetails(productMetadata.getMetalinkDownloadDirectory().getFileName().toString(), productMetadata.getFileMetadataList());
					productDownloadProgressMonitor.setStatus(EDownloadStatus.NOT_STARTED);
				}else{
					//download is a single file
					Header contentDispositionHeader = productDownloadHeaders.getResponseHeader("Content-Disposition");
					String disposition = contentDispositionHeader != null ? contentDispositionHeader.getValue() : null;
					long fileSize = productDownloadHeaders.getResponseContentLength();
					resolvedProductName = pathResolver.determineFileName(disposition, productURI, baseProductDownloadDir);

					LOGGER.debug("Content-Type = " + contentType);
					LOGGER.debug("Content-Disposition = " + disposition);
					LOGGER.debug("Content-Length = " + fileSize);
					LOGGER.debug("fileName = " + resolvedProductName);
					
					FileDownloadMetadata fileMetadata = new FileDownloadMetadata(productURI.toURL(), resolvedProductName, fileSize, baseProductDownloadDir.toPath());
					FileUtils.touch(fileMetadata.getPartiallyDownloadedPath().toFile());
					productMetadata.getFileMetadataList().add(fileMetadata);
				
					productDownloadProgressMonitor.notifyOfProductDetails(fileMetadata.getCompletelyDownloadedPath().getFileName().toString(), productMetadata.getFileMetadataList());
				}
				break;
			case HttpStatus.SC_ACCEPTED:
				productDownloadBody = retrieveDownloadDetailsBody(productURI.toURL());
				ProductDownloadResponse productDownloadResponse = xmlWithSchemaTransformer.deserializeAndInferSchema(productDownloadBody.getResponseBodyAsStream(), ProductDownloadResponse.class);					
				ProductDownloadResponseType productDownloadResponseCode = productDownloadResponse.getResponseCode();
				if(productDownloadResponseCode == ProductDownloadResponseType.ACCEPTED || productDownloadResponseCode == ProductDownloadResponseType.IN_PROGRESS) {
					long retryAfter = productDownloadResponse.getRetryAfter().longValue();
					LOGGER.info(String.format("Product %s not available at this time, retry after %s seconds", productURI.toString(), retryAfter));
					
					idleCheckExecutor = Executors.newSingleThreadScheduledExecutor();
					IdleCheckThread idleCheckThread = new IdleCheckThread(this);
					idleCheckExecutor.schedule(idleCheckThread, retryAfter, TimeUnit.SECONDS);
					
					productDownloadProgressMonitor.setStatus(EDownloadStatus.IDLE);
				}else{
					productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, String.format("Product download was accepted, but response code is %s", productDownloadResponseCode));
				}
				break;
			case HttpStatus.SC_PARTIAL_CONTENT:
				//Partial download is not relevant at this stage
				break;
			case HttpStatus.SC_SEE_OTHER:
				//TODO handle forwarded download
				break;
			case HttpStatus.SC_BAD_REQUEST:
				productDownloadBody = retrieveDownloadDetailsBody(productURI.toURL());
				try {
					BadRequestResponse badRequestResponse = xmlWithSchemaTransformer.deserializeAndInferSchema(productDownloadBody.getResponseBodyAsStream(), BadRequestResponse.class);					
					LOGGER.error(String.format("badRequestResponse: %s", badRequestResponse));
					productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, badRequestResponse.getResponseMessage());
				}catch(IOException | ParseException | SchemaNotFoundException ex) {
					LOGGER.error("HTTP response code 400 (Bad Request), unable to parse response details.", ex);
					productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, "HTTP response code 400 (Bad Request), unable to parse response details.");
				}
				break;
			case HttpStatus.SC_FORBIDDEN:
				//TODO: Expand error message to indicate JCL is not being used.
				productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, "Forbidden access.");
				break;
			case HttpStatus.SC_NOT_FOUND:
				productDownloadBody = retrieveDownloadDetailsBody(productURI.toURL());
				try {
					MissingProductResponse missingProductResponse = xmlWithSchemaTransformer.deserializeAndInferSchema(productDownloadBody.getResponseBodyAsStream(), MissingProductResponse.class);
					LOGGER.error(String.format("missingProductResponse: %s", missingProductResponse));
					productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, missingProductResponse.getResponseMessage());
				}catch(IOException | ParseException | SchemaNotFoundException ex) {
					LOGGER.error("HTTP response code 402 (Not Found), unable to parse response details.", ex);
					productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, "HTTP response code 402 (Not Found), unable to parse response details.");
				}
				break;
			default:
				productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, String.format("Unexpected response, HTTP response code %s",responseCode));
				break;
			}
		} catch (UmssoCLException | IOException | DMPluginException | ParseException | SchemaNotFoundException ex) {
			LOGGER.error("Exception occurred whilst retrieving download details.", ex);
			productDownloadProgressMonitor.setError(ex);
		} finally {
			if (productDownloadHeaders != null) {
				productDownloadHeaders.abort();
				productDownloadHeaders.releaseConnection();
			}
			if (productDownloadBody != null) {
				productDownloadBody.abort();
				productDownloadBody.releaseConnection();
			}
		}
	}
	
	private HttpMethodBase retrieveDownloadDetailsBody(URL productUrl) throws HttpException, IOException, UmssoCLException {
		return umSsoHttpClient.executeGetRequest(productUrl);
	}

	private FileDownloadMetadata getFileMetadataForMetalinkEntry(String fileDownloadLink, String fileName, Path metalinkDownloadDirectory) throws DMPluginException, IOException, UmssoCLException {
		long fileSize;

		URL fileDownloadLinkURL = new URL(fileDownloadLink);

		HttpMethodBase httpMethod = null; 
		try {
			httpMethod = umSsoHttpClient.executeHeadRequest(fileDownloadLinkURL);

			int metalinkFileResponseCode = httpMethod.getStatusCode();
			if(metalinkFileResponseCode == HttpStatus.SC_OK) {
				fileSize = httpMethod.getResponseContentLength();
				LOGGER.debug(String.format("metalink file content length = %s", fileSize));
			}else{
				throw new DMPluginException(String.format("Unable to retrieve metalink file details from file URL %s", fileDownloadLink));
			}
		} finally {
			if (httpMethod != null) {
				httpMethod.abort();
				httpMethod.releaseConnection();
			}
		}
		
		return new FileDownloadMetadata(fileDownloadLinkURL, fileName, fileSize, metalinkDownloadDirectory);
	}
	
	private void downloadProduct() {
		LOGGER.debug("Start of download product");
		List<FileDownloadMetadata> fileMetadataList = productMetadata.getFileMetadataList();
		int numberOfFilesInProduct = fileMetadataList.size();
		if(numberOfFilesInProduct > 0) {
			try {
				productDownloadProgressMonitor.setStatus(EDownloadStatus.RUNNING);
				String transferrerReadLengthProperty = pluginConfig.getProperty(KEY_TRANSFERRER_READ_LENGTH_IN_BYTES);
				int transferrerReadLength = Integer.parseInt(transferrerReadLengthProperty);
	
				if(fileDownloadExecutor != null && !fileDownloadExecutor.isShutdown() && !fileDownloadExecutor.isTerminated()) {
					productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, String.format("Internal Error with downloading products, unable to assign product %s to download threads.", productURI.toString()));
				}else{
					fileDownloadExecutor = Executors.newSingleThreadExecutor();
					for (FileDownloadMetadata fileDownloadMetadata : fileMetadataList) {
						if(!productDownloadProgressMonitor.isDownloadComplete(fileDownloadMetadata.getUuid())) {
							HttpFileDownloadRunnable httpFileDownloadRunnable = new HttpFileDownloadRunnable(fileDownloadMetadata, productDownloadProgressMonitor, umSsoHttpClient, transferrerReadLength);
							productDownloadProgressMonitor.getFileDownloadList().add(httpFileDownloadRunnable);
							fileDownloadExecutor.execute(httpFileDownloadRunnable);
						}
					}
					
					fileDownloadExecutor.shutdown();
	
					String downloadThreadTimeoutLengthInHoursProperty = pluginConfig.getProperty(KEY_DOWNLOAD_THREAD_TIMEOUT_LENGTH_IN_HOURS);
					Long downloadThreadTimeoutLengthInHours = Long.parseLong(downloadThreadTimeoutLengthInHoursProperty);
					boolean threadCompleted = fileDownloadExecutor.awaitTermination(downloadThreadTimeoutLengthInHours, TimeUnit.HOURS);
					if(!threadCompleted) {
						productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, String.format("Download for product %s timed out.", productURI.toString()));
						fileDownloadExecutor.shutdownNow();
					}else{
						LOGGER.debug(String.format("Threads completed for download %s", productURI.toString()));
						if(productDownloadProgressMonitor.getNumberOfCompletedFiles() == numberOfFilesInProduct) {
							tidyUpAfterCompletedDownload(numberOfFilesInProduct);
						}else{
							// The number of completed files does not equal the number of files in the product, therefore one of the following has happened:
							// * A pause or cancel command has been sent by the core, which has caused a termination of all product download threads.
							// * An error has occurred when downloading the products, which has caused a termination of all product download threads.
							// * The Download Manager is shutting down whilst the download is running, therefore the status was set to NOT_STARTED.
							// * A programmatic error has occurred, where the status is not PAUSED, CANCELLED or IN_ERROR
							EDownloadStatus statusWhenDownloadWasAborted = productDownloadProgressMonitor.getStatusWhenDownloadWasAborted();
							switch (statusWhenDownloadWasAborted) {
							case PAUSED:
							case IN_ERROR:
							case NOT_STARTED:
								productDownloadProgressMonitor.setStatus(statusWhenDownloadWasAborted);
								break;
							case CANCELLED:
								tidyUpAfterCancelledDownload();
								break;
							default:
								productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, "Product download threads have terminated unexpectedly, please contact support.");
								break;
							}
						}

						fileDownloadExecutor.shutdownNow();
					}
				}
			} catch (InterruptedException e) {
				productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, String.format("Download for product %s was interrupted.", productURI.toString()));
			}
		}else{
			productDownloadProgressMonitor.setStatus(EDownloadStatus.IN_ERROR, String.format("No files found for download of product %s.", productURI.toString()));
		}
	}
	
	/*
	 *  If the number of files has been determined, then pause each file download
	 *  Otherwise, pause the product download
	 */
	public EDownloadStatus pauseDownload() throws DMPluginException {
		ValidDownloadStatusForUserAction validDownloadStatusForUserAction = new ValidDownloadStatusForUserAction();
		if(!validDownloadStatusForUserAction.getValidDownloadStatusesToExecutePauseAction().contains(getStatus())) {
			throw new DMPluginException(String.format("Unable to pause download, status is %s", getStatus()));
		}
		
		stopIdleCheckIfActive();
	
		if(productDownloadProgressMonitor.getFileDownloadList().size() > 0) {
			productDownloadProgressMonitor.abortFileDownloads(EDownloadStatus.PAUSED);
		}else{
			productDownloadProgressMonitor.setStatus(EDownloadStatus.PAUSED);
		}

		return getStatus();
	}

	public EDownloadStatus resumeDownload() throws DMPluginException {
		ValidDownloadStatusForUserAction validDownloadStatusForUserAction = new ValidDownloadStatusForUserAction();
		if(!validDownloadStatusForUserAction.getValidDownloadStatusesToExecuteResumeAction().contains(getStatus())) {
			throw new DMPluginException(String.format("Unable to resume download, status is %s", getStatus()));
		}
		productDownloadProgressMonitor.setStatus(EDownloadStatus.NOT_STARTED);

		return getStatus();
	}

	public EDownloadStatus cancelDownload() throws DMPluginException {
		EDownloadStatus previousDownloadStatus = getStatus();
		ValidDownloadStatusForUserAction validDownloadStatusForUserAction = new ValidDownloadStatusForUserAction();
		if(!validDownloadStatusForUserAction.getValidDownloadStatusesToExecuteCancelAction().contains(previousDownloadStatus)) {
			throw new DMPluginException(String.format("Unable to cancel download, status is %s", getStatus()));
		}
		
		stopIdleCheckIfActive();

		switch (previousDownloadStatus) {
		case RUNNING:
		case NOT_STARTED:
			productDownloadProgressMonitor.abortFileDownloads(EDownloadStatus.CANCELLED);
			break;
		case PAUSED:
		case IDLE:
			tidyUpAfterCancelledDownload();
			break;
		default:
			throw new DMPluginException(String.format("Unable to cancel download, status is %s", previousDownloadStatus));
		}
		
		return getStatus();
	}
	
	private void stopIdleCheckIfActive() {
		if(idleCheckExecutor != null) {
			idleCheckExecutor.shutdownNow();
		}
	}

	private void tidyUpAfterCancelledDownload() {
		LOGGER.debug(String.format("Tidying up cancelled download %s", productURI.toString()));
		//delete files, both partial and complete
		if(productMetadata != null) {
			List<FileDownloadMetadata> fileMetadataList = productMetadata.getFileMetadataList();
			for (FileDownloadMetadata fileDownloadMetadata : fileMetadataList) {
				try {
					if(productDownloadProgressMonitor.isDownloadComplete(fileDownloadMetadata.getUuid())) {
						Files.deleteIfExists(fileDownloadMetadata.getCompletelyDownloadedPath());
					}else{
						Files.deleteIfExists(fileDownloadMetadata.getPartiallyDownloadedPath());
					}
				} catch (IOException e) {
					LOGGER.error(String.format("Unable to complete tidyup of file %s: %s", fileDownloadMetadata.getFileName(), e.getLocalizedMessage()));
				}
			}
		}
		productDownloadProgressMonitor.confirmCancelAfterTidyUp();
	}

	private void tidyUpAfterCompletedDownload(int numberOfFilesInProduct) {
		if(numberOfFilesInProduct > 1) { //product download is a metalink
			try {
				Files.move(productMetadata.getTempMetalinkDownloadDirectory(), productMetadata.getMetalinkDownloadDirectory(), StandardCopyOption.REPLACE_EXISTING);
				
				productDownloadProgressMonitor.setStatus(EDownloadStatus.COMPLETED);
			} catch (IOException e) {
				productDownloadProgressMonitor.setError(e);
			}
		}else{
			productDownloadProgressMonitor.setStatus(EDownloadStatus.COMPLETED);
		}
	}

	public EDownloadStatus getStatus() {
		return productDownloadProgressMonitor.getStatus();
	}

	public File[] getDownloadedFiles() {
		//check if the number of completed files equals the number of files identified as to be downloaded
		int numberOfFilesInProduct = productMetadata.getFileMetadataList().size();
		if(productDownloadProgressMonitor.getNumberOfCompletedFiles() == numberOfFilesInProduct) {
			if(numberOfFilesInProduct > 1) {
				return new File[]{productMetadata.getMetalinkDownloadDirectory().toFile()};
			}else if(numberOfFilesInProduct == 1) {
				return new File[]{productMetadata.getFileMetadataList().get(0).getCompletelyDownloadedPath().toFile()};
			}
		}
		return null;
	}

	/* This method is the last one called by the Download Manager on a IDownloadProcess instance.
	 * 		It is called by the Download Manager either:
	 * -	after the status COMPLETED, CANCELLED or IN_ERROR has been notified by the plugin to the Download Manager and the reference of downloaded files has been retrieved by the later
	 * -	when the Download Manager ends. In this second case, the plugin is expected to :
	 * 		-	if RUNNING 		: stop the download
	 * 		-	if RUNNING or PAUSED	: store onto disk the current download state (if possible) in order to restart it
	 */
	public void disconnect() throws DMPluginException {
		if(idleCheckExecutor != null) {
			//no elegant shutdown needs to be performed on the idle checker
			idleCheckExecutor.shutdownNow();
		}
		if(getStatus() == EDownloadStatus.RUNNING) {
			productDownloadProgressMonitor.abortFileDownloads(EDownloadStatus.NOT_STARTED);
			productDownloadProgressMonitor.setStatus(EDownloadStatus.NOT_STARTED);
		}
		try {
			if(fileDownloadExecutor != null) {
				fileDownloadExecutor.shutdown();
				fileDownloadExecutor.awaitTermination(10, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			LOGGER.error("Timeout occurred when attempting to shutdown download file threads");
		}
	}
}