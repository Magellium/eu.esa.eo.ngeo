package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadManager.plugin.utils.ProductResponseParser;
import int_.esa.eo.ngeo.downloadManager.plugin.utils.ProductResponseType;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.schema.ngeobadrequestresponse.BadRequestResponse;
import int_.esa.eo.ngeo.schema.ngeomissingproductresponse.MissingProductResponse;
import int_.esa.eo.ngeo.schema.ngeoproductdownloadresponse.ProductDownloadResponse;
import int_.esa.eo.ngeo.schema.ngeoproductdownloadresponse.ProductDownloadResponseType;
import int_.esa.umsso.UmSsoHttpClient;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.metalinker.FileType;
import org.metalinker.MetalinkType;
import org.metalinker.ResourcesType.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.pse.umsso.client.UmssoCLException;

public class HTTPDownloadProcess implements IDownloadProcess, DownloadProgressListener {
	/** May be incorrect; taken from http://stackoverflow.com/questions/5793606/how-to-filter-illegal-forbidden-filename-characters-from-users-keyboard-input-i */
	private static final char[] ILLEGAL_FILENAME_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};
	
	private static final String KEY_DOWNLOAD_THREAD_TIMEOUT_LENGTH_IN_HOURS = "DOWNLOAD_THREAD_TIMEOUT_LENGTH_IN_HOURS";
	private static final String KEY_TRANSFERRER_READ_LENGTH = "TRANSFERRER_READ_LENGTH";
	private static final String ENABLE_UMSSO_JCL_USE = "ENABLE_UMSSO_JCL_USE";

	private static final int DISPOSITION_SUBSTRING_START_OFFSET = 10;
	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPDownloadProcess.class);
	
	private static final String MIME_TYPE_METALINK = "application/metalink+xml";
	private static final double PERCENTAGE = 100.0;

	private ProductResponseParser productResponseParser;

	private URI productURI;
	
	private File downloadDir;
	
	private String metalinkDownloadDirectoryAsString;
	
	private long totalFileSize;

	private long totalFileDownloadedSize;
	
	private int percentageComplete;

	private EDownloadStatus downloadStatus;
	
	private List<IProductDownloadListener> productDownloadListeners;
	
	private String message;
	
	private ExecutorService fileDownloadExecutor;
	
	private ScheduledExecutorService idleCheckExecutor;
	
	private List<FileDetails> filesToDownloadList;
	
	private List<File> completedFileLocations;
	
	private UmSsoHttpClient umSsoHttpClient;
	
	private Properties pluginConfig;
	boolean enableUmssoJclUse;
		
	public HTTPDownloadProcess(URI productURI, File downloadDir, IProductDownloadListener productDownloadListener,
			String proxyLocation, int proxyPort, String proxyUser, String proxyPassword, String umssoUsername, String umssoPassword, Properties pluginConfig) {
		this.productURI = productURI;
		this.downloadDir = downloadDir;
		this.productDownloadListeners = new ArrayList<IProductDownloadListener>();
		this.productDownloadListeners.add(productDownloadListener);
		this.setDownloadStatus(EDownloadStatus.NOT_STARTED);
		this.productResponseParser = new ProductResponseParser();
		this.filesToDownloadList = new ArrayList<>();
		this.completedFileLocations = new ArrayList<>();
		this.pluginConfig = pluginConfig;

		String enableUmssoJclUseString = pluginConfig.getProperty(ENABLE_UMSSO_JCL_USE);
		enableUmssoJclUse = Boolean.parseBoolean(enableUmssoJclUseString);

		umSsoHttpClient = new UmSsoHttpClient(umssoUsername, umssoPassword, proxyLocation, proxyPort, proxyUser, proxyPassword, enableUmssoJclUse);
	}
	
	private String filterIllegalFileNameChars(String input) {
		String modified = input;
		for (char ch : ILLEGAL_FILENAME_CHARACTERS) {
			if (ch != '\\') {				
				modified = modified.replaceAll(new String(new char[]{'[', ch, ']'}), "");
			}
			else {
				modified = modified.replaceAll("[\\\\]", "");
			}
		}
		return modified;
	}
	
	public EDownloadStatus startDownload() throws DMPluginException {
		if(this.filesToDownloadList.size() == 0) {
			retrieveDownloadDetails();
		}
		if(getStatus() == EDownloadStatus.NOT_STARTED) {
			downloadProduct();
		}

		return getStatus();
	}
	
	public void retrieveDownloadDetails() {
		HttpMethod productDownloadHeaders = null;
		HttpMethod productDownloadBody = null;
		try {
			String productUrl = productURI.toURL().toString();
			LOGGER.debug("About to construct UmSsoHttpClient");
			//XXX: Since there is a bug with using Siemens' UM-SSO Java Client Library and HTTP HEAD, we use the GET method
			if(enableUmssoJclUse) {
				productDownloadHeaders = new GetMethod(productUrl);
			}else{
				productDownloadHeaders = new HeadMethod(productUrl);
			}
			umSsoHttpClient.executeHttpRequest(productDownloadHeaders);
			int responseCode = productDownloadHeaders.getStatusCode();
			switch (responseCode) {
			case HttpURLConnection.HTTP_OK: // 200
				String contentType = productDownloadHeaders.getResponseHeader("Content-Type").getValue();

				if(contentType.contains(MIME_TYPE_METALINK)) {
					productDownloadBody = retrieveDownloadDetailsBody(productUrl);
					MetalinkType metalink = productResponseParser.parse(productDownloadBody.getResponseBodyAsStream(), ProductResponseType.METALINK_3_0);					
					LOGGER.debug(String.format("metalink: %s", metalink));
					metalinkDownloadDirectoryAsString = "metalink" + UUID.randomUUID().toString().replaceAll("-", "");
					File tempMetalinkDownloadDirectory = Paths.get(downloadDir.getAbsolutePath(), "." + metalinkDownloadDirectoryAsString).toFile();

					List<FileType> fileList = metalink.getFiles().getFile();
					for (FileType fileType : fileList) {
						List<Url> urlList = fileType.getResources().getUrl();
						//Assumption: We are not handling parallel downloading at this point
						if(urlList.size() > 0) {
							Url firstUrlForFile = urlList.get(0);
							
							FileDetails fileDetails = getFileDetailsForMetalinkEntry(firstUrlForFile.getValue(), fileType.getName(), tempMetalinkDownloadDirectory);
							LOGGER.debug(String.format("metalink file download size %s", fileDetails.getDownloadSize()));
							totalFileSize += fileDetails.getDownloadSize();
							this.filesToDownloadList.add(fileDetails);
						}
					}
					LOGGER.debug(String.format("product contains %s files, total file size %s, ", fileList.size(), totalFileSize));
				}else{
					//download is a single file
					Header contentDispositionHeader = productDownloadHeaders.getResponseHeader("Content-Disposition");
					String disposition = contentDispositionHeader != null ? contentDispositionHeader.getValue() : null;
					Header contentLengthHeader = productDownloadHeaders.getResponseHeader("Content-Length");
					long fileSize = contentLengthHeader != null ? Long.valueOf(contentLengthHeader.getValue()) : -1; // The -1 is defends against the possibility that it is legal for servers to omit the Content-Length header 
					String fileName = generateFileName(disposition, productUrl);

					LOGGER.debug("Content-Type = " + contentType);
					LOGGER.debug("Content-Disposition = " + disposition);
					LOGGER.debug("Content-Length = " + fileSize);
					LOGGER.debug("fileName = " + fileName);
					
					FileDetails fileDetails = new FileDetails(productURI.toURL(), fileName, fileSize, downloadDir);
					totalFileSize = fileSize;
					this.filesToDownloadList.add(fileDetails);
					break;
				}
				
				setStatus(EDownloadStatus.NOT_STARTED);

				break;
			case HttpURLConnection.HTTP_ACCEPTED: // 202
				productDownloadBody = retrieveDownloadDetailsBody(productUrl);
				ProductDownloadResponse productDownloadResponse = productResponseParser.parse(productDownloadBody.getResponseBodyAsStream(), ProductResponseType.PRODUCT_ACCEPTED);					
				ProductDownloadResponseType productDownloadResponseCode = productDownloadResponse.getResponseCode();
				if(productDownloadResponseCode == ProductDownloadResponseType.ACCEPTED || productDownloadResponseCode == ProductDownloadResponseType.IN_PROGRESS) {
					long retryAfter = productDownloadResponse.getRetryAfter().longValue();
					LOGGER.info(String.format("Product %s not available at this time, retry after %s seconds", productURI.toString(), retryAfter));
					
					idleCheckExecutor = Executors.newSingleThreadScheduledExecutor();
					IdleCheckThread idleCheckThread = new IdleCheckThread(this);
					idleCheckExecutor.schedule(idleCheckThread, retryAfter, TimeUnit.SECONDS);
					
					setStatus(EDownloadStatus.IDLE);
				}else{
					setStatus(EDownloadStatus.IN_ERROR, String.format("Product download was accepted, but response code is %s", productDownloadResponseCode));
				}
				break;
			case HttpURLConnection.HTTP_PARTIAL: //206
				//Partial download is not relevant at this stage
				break;
			case HttpURLConnection.HTTP_SEE_OTHER: //303
				//TODO handle forwarded download
				break;
			case HttpURLConnection.HTTP_BAD_REQUEST: //400
				productDownloadBody = retrieveDownloadDetailsBody(productUrl);
				BadRequestResponse badRequestResponse = productResponseParser.parse(productDownloadBody.getResponseBodyAsStream(), ProductResponseType.BAD_REQUEST);					
				LOGGER.error(String.format("badRequestResponse: %s", badRequestResponse));
				setStatus(EDownloadStatus.IN_ERROR, badRequestResponse.getResponseMessage());
				break;
			case HttpURLConnection.HTTP_FORBIDDEN: //403
				//TODO: Expand error message to indicate JCL is not being used.
				setStatus(EDownloadStatus.IN_ERROR, "Forbidden access.");
				break;
			case HttpURLConnection.HTTP_NOT_FOUND: //402
				productDownloadBody = retrieveDownloadDetailsBody(productUrl);
				MissingProductResponse missingProductResponse = productResponseParser.parse(productDownloadBody.getResponseBodyAsStream(), ProductResponseType.MISSING_PRODUCT);					
				LOGGER.error(String.format("missingProductResponse: %s", missingProductResponse));
				setStatus(EDownloadStatus.IN_ERROR, missingProductResponse.getResponseMessage());
				break;
			default:
				setStatus(EDownloadStatus.IN_ERROR, String.format("Unexpected response, HTTP response code %s",responseCode));
				break;
			}
		} catch (UmssoCLException | IOException | DMPluginException ex) {
			setError(ex);
		} finally {
			if (productDownloadHeaders != null) {
				productDownloadHeaders.releaseConnection();
			}
			if (productDownloadBody != null) {
				productDownloadBody.releaseConnection();
			}
		}
	}
	
	private HttpMethod retrieveDownloadDetailsBody(String productUrl) throws HttpException, IOException, UmssoCLException {
		HttpMethod httpMethod = new GetMethod(productUrl);
		umSsoHttpClient.executeHttpRequest(httpMethod);
	
		return httpMethod;
	}

	private FileDetails getFileDetailsForMetalinkEntry(String fileDownloadLink, String fileName, File metalinkDownloadDirectory) throws DMPluginException, IOException, UmssoCLException {
		long fileSize;

		URL fileDownloadLinkURL = new URL(fileDownloadLink);

		GetMethod httpMethod = null; 
		try {
			httpMethod = new GetMethod(fileDownloadLinkURL.toString());
			umSsoHttpClient.executeHttpRequest(httpMethod);
						
			int metalinkFileResponseCode = httpMethod.getStatusCode();
			if(metalinkFileResponseCode == HttpURLConnection.HTTP_OK) {
				fileSize = Long.valueOf(httpMethod.getResponseHeader("Content-Length").getValue());
				LOGGER.debug(String.format("metalink file content length = %s", fileSize));
			}else{
				throw new DMPluginException(String.format("Unable to retrieve metalink file details from file URL %s", fileDownloadLink));
			}
		} finally {
			if (httpMethod != null) {
				httpMethod.releaseConnection();
			}
		}
		
		return new FileDetails(fileDownloadLinkURL, fileName, fileSize, metalinkDownloadDirectory);
	}
	
	private void downloadProduct() {
		LOGGER.debug("Start of download product");
		if(filesToDownloadList.size() > 0) {
			try {
				setStatus(EDownloadStatus.RUNNING);
	
				if(fileDownloadExecutor != null && !fileDownloadExecutor.isShutdown() && !fileDownloadExecutor.isTerminated()) {
					setStatus(EDownloadStatus.IN_ERROR, String.format("Internal Error with downloading products, unable to assign product %s to download threads.", productURI.toString()));
				}else{
					fileDownloadExecutor = Executors.newSingleThreadExecutor();
					for (FileDetails fileDetails : filesToDownloadList) {
						if(!fileDetails.isDownloadComplete()) {
							HttpDownloadThread httpDownloadThread = new HttpDownloadThread(fileDetails);
							//HTTPDownloadProcess needs to be the last listener, as it refers to the FileDetails in order to update the % complete (99% progress bug).
							httpDownloadThread.addProgressListener(fileDetails);
							httpDownloadThread.addProgressListener(this);
							fileDownloadExecutor.execute(httpDownloadThread);
						}
					}
					
					fileDownloadExecutor.shutdown();
	
					String downloadThreadTimeoutLengthInHoursProperty = pluginConfig.getProperty(KEY_DOWNLOAD_THREAD_TIMEOUT_LENGTH_IN_HOURS);
					Long downloadThreadTimeoutLengthInHours = Long.parseLong(downloadThreadTimeoutLengthInHoursProperty);
					boolean threadCompleted = fileDownloadExecutor.awaitTermination(downloadThreadTimeoutLengthInHours, TimeUnit.HOURS);
					if(!threadCompleted) {
						setStatus(EDownloadStatus.IN_ERROR, String.format("Download for product %s timed out.", productURI.toString()));
						fileDownloadExecutor.shutdownNow();
					}else{
						updateProductFileDownloadStatus();
						if(completedFileLocations.size() == filesToDownloadList.size()) {
							if(completedFileLocations.size() > 1) { //product download is a metalink
								try {
									Path tempMetalinkDownloadDirectory = Paths.get(downloadDir.getAbsolutePath(), "." + metalinkDownloadDirectoryAsString);
									Path metalinkDownloadDirectory = Paths.get(downloadDir.getAbsolutePath(), metalinkDownloadDirectoryAsString);

									Files.move(tempMetalinkDownloadDirectory, metalinkDownloadDirectory, StandardCopyOption.REPLACE_EXISTING);
									
									// Adjust completedFileLocations to account for the renaming of their parent directory
									File[] completedFileLocationsAsArray = metalinkDownloadDirectory.toFile().listFiles();
									if (completedFileLocationsAsArray.length != completedFileLocations.size()) {
										LOGGER.error(String.format("%s should contain %s files but actually contains %s.", metalinkDownloadDirectoryAsString, completedFileLocations.size(), completedFileLocationsAsArray.length));
									}
									completedFileLocations = Arrays.asList(completedFileLocationsAsArray);
									
									setStatus(EDownloadStatus.COMPLETED);
								} catch (IOException e) {
									setError(e);
								}
							}else{
								setStatus(EDownloadStatus.COMPLETED);
							}
						}
						if(getDownloadStatus() == EDownloadStatus.CANCELLED) {
							tidyUpAfterCancelledDownload();
						}
						fileDownloadExecutor.shutdownNow();
					}
				}
			} catch (InterruptedException e) {
				setStatus(EDownloadStatus.IN_ERROR, String.format("Download for product %s was interrupted.", productURI.toString()));
			}
		}else{
			setStatus(EDownloadStatus.IN_ERROR, String.format("No files found for download of product %s.", productURI.toString()));
		}
	}
	
	private void tidyUpAfterCancelledDownload() {
		//delete files, both partial and complete
		for (FileDetails fileDetails : filesToDownloadList) {
			try {
				if(fileDetails.isDownloadComplete()) {
					Files.deleteIfExists(Paths.get(fileDetails.getDownloadPath().getAbsolutePath(), fileDetails.getFileName()));
				}else{
					Files.deleteIfExists(Paths.get(fileDetails.getDownloadPath().getAbsolutePath(), getPartiallyDownloadedFileName(fileDetails.getFileName())));
				}
			} catch (IOException e) {
				LOGGER.error(String.format("Unable to complete tidyup of file %s", fileDetails.getFileName()));
			}
		}
		setPercentageComplete(0);
		setTotalFileDownloadedSize(0);
		notifyProgressListeners();
	}

	public EDownloadStatus pauseDownload() throws DMPluginException {
		// Pause can only be set if the download is running, idle or in the download queue (NOT_STARTED)
		if(getDownloadStatus() != EDownloadStatus.IDLE && getDownloadStatus() != EDownloadStatus.RUNNING && getDownloadStatus() != EDownloadStatus.NOT_STARTED) {
			throw new DMPluginException(String.format("Unable to pause download, status is %s", getDownloadStatus()));
		}
		setStatus(EDownloadStatus.PAUSED);

		return getStatus();
	}

	public EDownloadStatus resumeDownload() throws DMPluginException {
		if(getDownloadStatus() == EDownloadStatus.RUNNING || getDownloadStatus() == EDownloadStatus.COMPLETED || getDownloadStatus() == EDownloadStatus.IDLE) {
			throw new DMPluginException(String.format("Unable to resume download, status is %s", getDownloadStatus()));
		}
		setStatus(EDownloadStatus.NOT_STARTED);

		return getStatus();
	}

	public EDownloadStatus cancelDownload() throws DMPluginException {
		// Cancel can only be set if the download is running, paused, idle or NOT_STARTED
		EDownloadStatus downloadStatus = getDownloadStatus();
		if(downloadStatus != EDownloadStatus.RUNNING && downloadStatus != EDownloadStatus.PAUSED && downloadStatus != EDownloadStatus.IDLE && downloadStatus != EDownloadStatus.NOT_STARTED) {
			throw new DMPluginException(String.format("Unable to cancel download, status is %s", downloadStatus));
		}
		setStatus(EDownloadStatus.CANCELLED);
		if(downloadStatus == EDownloadStatus.PAUSED) {
			tidyUpAfterCancelledDownload();
		}
		
		return getStatus();
	}

	public EDownloadStatus getStatus() {
		return getDownloadStatus();
	}

	public File[] getDownloadedFiles() {
		//check if the number of completed files equals the number of files identified as to be downloaded
		if(completedFileLocations.size() == filesToDownloadList.size()) {
			return completedFileLocations.toArray(new File[completedFileLocations.size()]);
		}else{
			return null;
		}
	}

	/* TODO This method is the last one called by the Download Manager on a IDownloadProcess instance.
	 * 		It is called by the Download Manager either:
	 * -	after the status COMPLETED, CANCELLED or IN_ERROR has been notified by the plugin to the Download Manager and the reference of downloaded files has been retrieved by the later
	 * -	when the Download Manager ends. In this second case, the plugin is expected to :
	 * 		-	if RUNNING 		: stop the download
	 * 		-	if RUNNING or PAUSED	: store onto disk the current download state (if possible) in order to restart it
	 */
	public void disconnect() throws DMPluginException {
		if(idleCheckExecutor != null) {
			idleCheckExecutor.shutdown();
		}
		if(getStatus() == EDownloadStatus.RUNNING) {
			setStatus(EDownloadStatus.NOT_STARTED);
		}
		try {
			if(idleCheckExecutor != null) {
				fileDownloadExecutor.shutdown();
				fileDownloadExecutor.awaitTermination(10, TimeUnit.SECONDS);
			}
			notifyProgressListeners(); //called to ensure the number of bytes is correct
		} catch (InterruptedException e) {
			LOGGER.error("Timeout occurred when attempting to shutdown download file threads");
		}
	}
	
	public void setStatus(EDownloadStatus downloadStatus) {
		setStatus(downloadStatus, null);
	}

	public void setStatus(EDownloadStatus downloadStatus, String message) {
		this.setDownloadStatus(downloadStatus);
		this.message = message;
		notifyProgressListeners();
	}
	
	private String generateFileName(String disposition, String actualDownloadUrl) {
		String fileName = null;
		if (disposition != null && disposition.indexOf("filename=") > 0) {
			// Extract file name from header field
			int index = disposition.indexOf("filename=");
			if (index > 0) {
				fileName = disposition.substring(index + DISPOSITION_SUBSTRING_START_OFFSET,disposition.length() - 1);
			}
		} else {
			// Try to extract a file name from the URL
			String unfilteredCandidateName = actualDownloadUrl.substring(actualDownloadUrl.lastIndexOf('/') + 1,actualDownloadUrl.length());
			String filteredCandidateName = filterIllegalFileNameChars(unfilteredCandidateName);
			if (filteredCandidateName.length() == 0) {
				fileName = UUID.randomUUID().toString();
				LOGGER.warn(String.format("Resorting to use of UUID %s as a file name, to avoid zero-length name that would result from filtering illegal chars from %s", fileName, unfilteredCandidateName));
			}
			else {
				fileName = filteredCandidateName;
			}
		}
		return fileName;
	}
	
	public void notifySomeBytesTransferred(long numberOfBytes) {
		updateProductFileDownloadStatus();
		notifyProgressListeners();
	}
	
	public void updateProductFileDownloadStatus() {
		long totalBytesDownloaded = 0;
		for (FileDetails fileDetails : filesToDownloadList) {
			totalBytesDownloaded += fileDetails.getDownloadedSize();
		}
		setTotalFileDownloadedSize(totalBytesDownloaded);

		//update percentage complete
		int newPercentageComplete = (int) Math.floor((getTotalFileDownloadedSize() * PERCENTAGE) / totalFileSize);
		this.setPercentageComplete(newPercentageComplete);
	}
	
	private void notifyProgressListeners() {
		for (IProductDownloadListener productDownloadListener : productDownloadListeners) {
			productDownloadListener.progress(getPercentageComplete(), getTotalFileDownloadedSize(), getDownloadStatus(), message);
		}
	}
	
	public synchronized long getTotalFileDownloadedSize() {
		return totalFileDownloadedSize;
	}

	public synchronized void setTotalFileDownloadedSize(long totalFileDownloadedSize) {
		this.totalFileDownloadedSize = totalFileDownloadedSize;
	}
	
	public synchronized int getPercentageComplete() {
		return percentageComplete;
	}

	public synchronized void setPercentageComplete(int percentageComplete) {
		this.percentageComplete = percentageComplete;
	}
	
	public synchronized EDownloadStatus getDownloadStatus() {
		return downloadStatus;
	}

	public synchronized void setDownloadStatus(EDownloadStatus downloadStatus) {
		this.downloadStatus = downloadStatus;
	}

	private void setError(Exception ex) {
		setStatus(EDownloadStatus.IN_ERROR, ex.getLocalizedMessage());
		LOGGER.error(ex.getLocalizedMessage(), ex); 
	}
	
	private String getPartiallyDownloadedFileName(String fileName) {
		 return String.format(".%s", fileName);
	}

	/**
	 * Thread using HTTP protocol to download a part of file
	 */
	private class HttpDownloadThread implements Runnable {
		private FileDetails fileDetails;
		Set<DownloadProgressListener> progressListeners = new HashSet<>();
		
		public HttpDownloadThread(FileDetails fileDetails) {
			this.fileDetails = fileDetails;
		}
		
		public void addProgressListener(DownloadProgressListener listener) {
			progressListeners.add(listener);
		}
		
		public void run() {
			FileChannel destination = null;
			FileDetails fileDetails = this.fileDetails;
			
			GetMethod method = new GetMethod(fileDetails.getFileURL().toString());
			
			try {
				Path downloadPath = Paths.get(fileDetails.getDownloadPath().getAbsolutePath(), getPartiallyDownloadedFileName(fileDetails.getFileName()));
				Path downloadPathParent = downloadPath.getParent();
				if (!Files.exists(downloadPathParent)) {
					Files.createDirectories(downloadPathParent);
				}
				destination = (FileChannel) Files.newByteChannel(downloadPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				long currentFileSize = destination.size();
				
				// Has the file been partially downloaded already?
				if (currentFileSize > 0) {
					LOGGER.debug(String.format("Current size of file: %s", currentFileSize));
					fileDetails.setDownloadedSize(currentFileSize);
					updateProductFileDownloadStatus();
				}

				if (fileDetails.getDownloadedSize() > 0) { //file has been partially downloaded (XXX: Why are we not re-using the earlier expression that tests for this?)
					method.addRequestHeader("Range", String.format("bytes=%s-%s", fileDetails.getDownloadedSize(), fileDetails.getDownloadSize()));
				}
				
				umSsoHttpClient.executeHttpRequest(method);
				
				int responseCode = method.getStatusCode();
				switch (responseCode) {
				case HttpURLConnection.HTTP_OK:
				case HttpURLConnection.HTTP_PARTIAL:
					
					String transferrerReadLengthProperty = pluginConfig.getProperty(KEY_TRANSFERRER_READ_LENGTH);
					int transferrerReadLength = Integer.parseInt(transferrerReadLengthProperty);

					Transferrer transferrer = new Transferrer(HTTPDownloadProcess.this, transferrerReadLength);
					transferrer.doTransfer(destination, method.getResponseBodyAsStream(), fileDetails, progressListeners);
					
					EDownloadStatus downloadStatus = getDownloadStatus();
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
						Path completedPath = Paths.get(fileDetails.getDownloadPath().getAbsolutePath(), fileDetails.getFileName());
						Files.move(downloadPath, completedPath, StandardCopyOption.REPLACE_EXISTING);
						completedFileLocations.add(completedPath.toFile());
						fileDetails.setDownloadComplete(true);
						break;
					}
					break;
				default:
					setStatus(EDownloadStatus.IN_ERROR, String.format("Response code for download of file is %s.", responseCode));
					break;
				}
			} catch (UmssoCLException | IOException | DMPluginException ex) {
				setError(ex);
				LOGGER.error("\n\n\n=============DEAD DOWNLOAD THREAD========================", ex);
			} catch (Throwable throwable) {
				LOGGER.error("\n\n\n=============DEAD DOWNLOAD THREAD========================", throwable);
				setError(new DMPluginException(throwable));
			} finally {
				if (method != null) {
					method.releaseConnection();
				}
			}
		}		
	}

	private class IdleCheckThread implements Runnable {
		HTTPDownloadProcess downloadProcess;

		public IdleCheckThread(HTTPDownloadProcess downloadProcess) {
			this.downloadProcess = downloadProcess;
		}
		
		public void run() {
			downloadProcess.retrieveDownloadDetails();
		}
	}
}