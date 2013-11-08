package int_.esa.eo.ngeo.dmtu.monitor;

import int_.esa.eo.ngeo.dmtu.callback.CallbackCommandExecutor;
import int_.esa.eo.ngeo.dmtu.exception.DownloadOperationException;
import int_.esa.eo.ngeo.dmtu.exception.DownloadProcessCreationException;
import int_.esa.eo.ngeo.dmtu.log.ProductTerminationLog;
import int_.esa.eo.ngeo.dmtu.manager.DataAccessRequestManager;
import int_.esa.eo.ngeo.dmtu.observer.DownloadObserver;
import int_.esa.eo.ngeo.dmtu.observer.ProductObserver;
import int_.esa.eo.ngeo.dmtu.plugin.ProductDownloadListener;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.exception.NoPluginAvailableException;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductProgress;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPlugin;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;
import int_.esa.eo.ngeo.downloadmanager.plugin.PluginManager;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DownloadMonitor implements ProductObserver, DownloadObserver {
	@Autowired
	private PluginManager pluginManager;

	@Autowired
	private SettingsManager settingsManager;
	
	private DataAccessRequestManager dataAccessRequestManager;
	
	private Map<String, IDownloadProcess> downloadProcessList;

	private ExecutorService productDownloadExecutor;
	
	private Map<String, Product> productToDownloadList;

	private ProductTerminationLog productTerminationLog;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadMonitor.class);

	@Autowired
	public DownloadMonitor(DataAccessRequestManager dataAccessRequestManager) {
		this.dataAccessRequestManager = dataAccessRequestManager;
		dataAccessRequestManager.registerObserver(this);
		this.downloadProcessList = new ConcurrentHashMap<>();
		this.productToDownloadList = new ConcurrentHashMap<>();
		this.productTerminationLog = new ProductTerminationLog();
	}

	public void initDowloadPool() {
		String noOfParallelProductDownloadThreads = settingsManager.getSetting("NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS");
		int concurrentProductDownloadThreads = Integer.parseInt(noOfParallelProductDownloadThreads);
		LOGGER.info(String.format("Creating thread pool for %s concurrent product download threads", concurrentProductDownloadThreads));
		productDownloadExecutor = Executors.newFixedThreadPool(concurrentProductDownloadThreads);
	}
	
	@Override
	public void newProduct(Product product) {
		LOGGER.debug("Product update has occurred");
		productToDownloadList.put(product.getUuid(), product);
		
		try {
			IDownloadProcess downloadProcess = createDownloadProcess(product);
		
			switch (product.getProductProgress().getStatus()) {
			case NOT_STARTED:
			case IDLE:
			case RUNNING:
				DownloadThread downloadThread = new DownloadThread(downloadProcess);
				productDownloadExecutor.execute(downloadThread);
				break;
			default:
				break;
			}
			
			downloadProcessList.put(product.getUuid(), downloadProcess);
		}catch(DownloadProcessCreationException e) {
			//This exception does not need to be rethrown, as the product has already been set in error.
		}
	}

	@Override
	public void updateProductDownloadStatus(Product product, EDownloadStatus downloadStatus) {
		String productUuid = product.getUuid();
		try {
			switch (downloadStatus) {
			case NOT_STARTED:
				resumeProductDownload(productUuid);
				break;
			case PAUSED:
				pauseProductDownload(productUuid);
				break;
			case CANCELLED:
				cancelProductDownload(productUuid);
				break;
			default:
				break;
			}
		} catch (DownloadOperationException e) {
			LOGGER.error(String.format("Unable to change status of product %s to %s", product.getProductAccessUrl(), downloadStatus));
		}
	}	

	private IDownloadProcess createDownloadProcess(Product product) throws DownloadProcessCreationException {
		IDownloadPlugin downloadPlugin;
		try {
			URI uri = new URI(product.getProductAccessUrl());
			
			downloadPlugin = pluginManager.determinePlugin(product.getProductAccessUrl());

			LOGGER.debug("Plugin determined to use: " + downloadPlugin);
			
			ProductDownloadListener productDownloadListener = new ProductDownloadListener(product.getUuid());
			productDownloadListener.registerObserver(this);
			
			File downloadPath;
			String downloadDirectory = product.getDownloadDirectory();
			if(downloadDirectory != null) {
				downloadPath = Paths.get(settingsManager.getSetting(SettingsManager.KEY_BASE_DOWNLOAD_FOLDER_ABSOLUTE), downloadDirectory).toFile();
			}else{
				downloadPath = Paths.get(settingsManager.getSetting(SettingsManager.KEY_BASE_DOWNLOAD_FOLDER_ABSOLUTE)).toFile();
			}
			
			String umSSOUsername = settingsManager.getSetting(SettingsManager.KEY_SSO_USERNAME);
			String umSSOPassword = settingsManager.getSetting(SettingsManager.KEY_SSO_PASSWORD);
			String proxyHost = settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_HOST);
			String proxyPortString = settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_PORT);
			int proxyPort;
			if (proxyPortString == null || proxyPortString.isEmpty()) {
				proxyPort = -1;
			}else{
				proxyPort = Integer.parseInt(proxyPortString);
			}
			String proxyUsername = settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_USERNAME);
			String proxyPassword = settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_PASSWORD);
			
			return downloadPlugin.createDownloadProcess(uri, downloadPath, umSSOUsername, umSSOPassword, productDownloadListener, proxyHost, proxyPort, proxyUsername, proxyPassword);
		} catch (NoPluginAvailableException | URISyntaxException | DMPluginException ex) {
			LOGGER.error(String.format("Error whilst creating download process of product %s: %s", product.getProductAccessUrl(), ex.getLocalizedMessage()));
			product.getProductProgress().setStatus(EDownloadStatus.IN_ERROR);
			product.getProductProgress().setMessage(ex.getLocalizedMessage());
			
			throw new DownloadProcessCreationException(ex.getLocalizedMessage(), ex);
		}
	}
	
	@Override
	public void updateProductDetails(String productUuid, String productName, Integer numberOfFiles, Long overallSize) {
		Product product = productToDownloadList.get(productUuid);
		product.setStartOfFirstDownloadRequest(new Timestamp(new Date().getTime()));
		product.setProductName(productName);
		product.setNumberOfFiles(numberOfFiles);
		product.setOverallSize(overallSize);

		dataAccessRequestManager.persistProductStatusChange(product);
	}

	@Override
	public void updateProgress(String productUuid, ProductProgress productProgress) {
		Product product = productToDownloadList.get(productUuid);
		EDownloadStatus previouslyKnownStatus = product.getProductProgress().getStatus();
		EDownloadStatus newStatus = productProgress.getStatus();
		product.setProductProgress(productProgress);
		IDownloadProcess downloadProcess = null;
		try {
			downloadProcess = getDownloadProcess(productUuid);
		} catch (DownloadOperationException e) {
			LOGGER.error("Unable to retrieve download process for product.", e);
		}

		if(previouslyKnownStatus != newStatus) {
			LOGGER.debug(String.format("Status has changed from %s to %s, updating to database", previouslyKnownStatus, newStatus));
			if(newStatus == EDownloadStatus.RUNNING) {
				product.setStartOfActualDownload(new Timestamp(new Date().getTime()));
			}
			
			dataAccessRequestManager.persistProductStatusChange(product);
		}

		if(previouslyKnownStatus == EDownloadStatus.IDLE && newStatus == EDownloadStatus.NOT_STARTED) {
			DownloadThread downloadThread = new DownloadThread(downloadProcess);
			productDownloadExecutor.execute(downloadThread);
		}
		
		//perform actions based on terminal statuses
		switch (newStatus) {
		case IN_ERROR:
			setTerminalStateOfProduct(product);
			break;
		case CANCELLED:
			setTerminalStateOfProduct(product);
			disconnectAndRemoveProcess(downloadProcess, product);
			break;
		case COMPLETED:
			getCompletedDownloadPathFromProcess(downloadProcess, product);
			setTerminalStateOfProduct(product);

			executeCallbackMechanism(product);
			disconnectAndRemoveProcess(downloadProcess, product);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 	Disconnect and remove the process for a completed or cancelled product.
	 *  The disconnect ensures that we tidy up any left over threads / resources, any failure of the disconnect does not matter
	 *  
	 * @param downloadProcess
	 * @param product
	 */
	private void disconnectAndRemoveProcess(IDownloadProcess downloadProcess, Product product) {
		try {
			downloadProcess.disconnect();
		} catch (DMPluginException e) { }
		
		downloadProcessList.remove(product.getUuid());
		productToDownloadList.remove(product.getUuid());
	}
	
	/**
	 * Set the completed download path for the product before we lose the list of downloaded files
	 * i.e. when the process is removed.
	 * 
	 * @param downloadProcess
	 * @param product
	 */
	private void getCompletedDownloadPathFromProcess(IDownloadProcess downloadProcess, Product product) {
		File[] downloadedFiles = downloadProcess.getDownloadedFiles();
		if(downloadedFiles != null && downloadedFiles.length > 0) {
			product.setCompletedDownloadPath(downloadedFiles[0].getAbsolutePath());
		}else{
			LOGGER.error("Product has been completed, but the completed download path cannot be retrieved.");
		}
	}
	
	private void executeCallbackMechanism(Product product) {
		String productDownloadCompleteCommand = settingsManager.getSetting(SettingsManager.KEY_PRODUCT_DOWNLOAD_COMPLETE_COMMAND);
		CallbackCommandExecutor callbackExecutor = new CallbackCommandExecutor();
		callbackExecutor.invokeCallbackCommandOnProductFiles(productDownloadCompleteCommand, product.getCompletedDownloadPath());
	}
	
	private void setTerminalStateOfProduct(Product product) {
		product.setStopOfDownload(new Timestamp(new Date().getTime()));
		dataAccessRequestManager.persistProductStatusChange(product);
		DataAccessRequest dataAccessRequest = dataAccessRequestManager.findDataAccessRequestByProduct(product);
		productTerminationLog.notifyProductDownloadTermination(product, dataAccessRequest);
	}

	/**
	 * Thread to download a file
	 */
	protected class DownloadThread implements Runnable {
		private final IDownloadProcess downloadProcess;
		
		public DownloadThread(IDownloadProcess downloadProcess) {
			this.downloadProcess = downloadProcess;
		}
		
		@Override
		public void run() {
			try {
				LOGGER.debug("Starting download thread");

				downloadProcess.startDownload();
			} catch (DMPluginException e) {
				throw new NonRecoverableException("Unable to start download.", e);
			}
		}
	}

	public boolean pauseProductDownload(String productUuid) throws DownloadOperationException {
		IDownloadProcess downloadProcess = getDownloadProcess(productUuid);
		try {
			downloadProcess.pauseDownload();
		} catch (DMPluginException e) {
			throw new DownloadOperationException(String.format("Unable to pause product download, plugin response: %s", e.getLocalizedMessage()));
		}
		return true;
	}
	
	public boolean resumeProductDownload(String productUuid) throws DownloadOperationException {
		IDownloadProcess downloadProcess = getDownloadProcess(productUuid);
		DownloadThread downloadThread = new DownloadThread(downloadProcess);
		try {
			productDownloadExecutor.execute(downloadThread);

			downloadProcess.resumeDownload();
		} catch (DMPluginException e) {
			throw new DownloadOperationException(String.format("Unable to pause product download, plugin response: %s", e.getLocalizedMessage()));
		}
		return true;
	}
	
	public boolean cancelProductDownload(String productUuid) throws DownloadOperationException {
		IDownloadProcess downloadProcess = getDownloadProcess(productUuid);
		try {
			downloadProcess.cancelDownload();
		} catch (DMPluginException e) {
			throw new DownloadOperationException(String.format("Unable to pause product download, plugin response: %s", e.getLocalizedMessage()));
		}
		return true;
	}

	private IDownloadProcess getDownloadProcess(String productUuid) throws DownloadOperationException {
		IDownloadProcess downloadProcess = downloadProcessList.get(productUuid);
		if(downloadProcess == null) {
			throw new DownloadOperationException(String.format("Unable to find product with UUID %s. This product may have already been completed.", productUuid));
		}
		return downloadProcess;
	}

	public void shutdown() {
		LOGGER.info("Shutting down Download Monitor.");

		productDownloadExecutor.shutdown();		

		for (IDownloadProcess downloadProcess : downloadProcessList.values()) {
			try {
				downloadProcess.disconnect();
			} catch (DMPluginException e) {
				LOGGER.error("Unable to disconnect download process, threads may still be open.");
			}
		}
	}
	
	public boolean cancelDownloadsWithStatuses(List<EDownloadStatus> statusesToCancel, boolean includeManualDownloads) throws DownloadOperationException {
		boolean downloadsCancelledCompletely = true;
		for (Entry<String, IDownloadProcess> downloadProcessEntry: downloadProcessList.entrySet()) {
			String productUuid = downloadProcessEntry.getKey();
			IDownloadProcess downloadProcess = downloadProcessEntry.getValue();
			if((includeManualDownloads || !dataAccessRequestManager.isProductDownloadManual(productUuid)) && statusesToCancel.contains(downloadProcess.getStatus())) {
				try {
					downloadProcess.cancelDownload();
				} catch (DMPluginException e) {
					LOGGER.error(String.format("Unable to cancel product download with UUID %s. Reason: %s.", productUuid, e.getLocalizedMessage()));
					downloadsCancelledCompletely = false;
				}
			}
		}
		if(!downloadsCancelledCompletely) {
			throw new DownloadOperationException("Unable to send cancel request to all applicable downloads.");
		}
		return downloadsCancelledCompletely;
	}
}