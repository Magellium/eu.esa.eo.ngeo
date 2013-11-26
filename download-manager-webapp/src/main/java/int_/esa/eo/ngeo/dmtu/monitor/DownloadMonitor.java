package int_.esa.eo.ngeo.dmtu.monitor;

import int_.esa.eo.ngeo.dmtu.callback.CallbackCommandExecutor;
import int_.esa.eo.ngeo.dmtu.exception.DownloadOperationException;
import int_.esa.eo.ngeo.dmtu.exception.DownloadProcessCreationException;
import int_.esa.eo.ngeo.dmtu.exception.ProductNotFoundException;
import int_.esa.eo.ngeo.dmtu.manager.DataAccessRequestManager;
import int_.esa.eo.ngeo.dmtu.model.ActiveProducts;
import int_.esa.eo.ngeo.dmtu.observer.DownloadObserver;
import int_.esa.eo.ngeo.dmtu.observer.ProductObserver;
import int_.esa.eo.ngeo.dmtu.plugin.ProductDownloadListener;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.exception.NoPluginAvailableException;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.log.ProductTerminationLog;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.model.ProductProgress;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPlugin;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;
import int_.esa.eo.ngeo.downloadmanager.plugin.PluginManager;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
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
	
	private ExecutorService productDownloadExecutor;
	private BlockingQueue<Runnable> downloadQueue;
	
	private ActiveProducts activeProducts;

	private ProductTerminationLog productTerminationLog;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadMonitor.class);

	@Autowired
	public DownloadMonitor(DataAccessRequestManager dataAccessRequestManager) {
		this.dataAccessRequestManager = dataAccessRequestManager;
		dataAccessRequestManager.registerObserver(this);
		this.productTerminationLog = new ProductTerminationLog();
		activeProducts = new ActiveProducts();
	}

	public void initDowloadPool() {
		String noOfParallelProductDownloadThreads = settingsManager.getSetting(UserModifiableSetting.NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS);
		int concurrentProductDownloadThreads = Integer.parseInt(noOfParallelProductDownloadThreads);
		LOGGER.info(String.format("Creating thread pool for %s concurrent product download threads", concurrentProductDownloadThreads));
        downloadQueue = new ArrayBlockingQueue<>(200);
		productDownloadExecutor = new ThreadPoolExecutor(concurrentProductDownloadThreads, concurrentProductDownloadThreads, 0L, TimeUnit.MILLISECONDS, downloadQueue);
	}
	
	@Override
	public void newProduct(Product product) {
		LOGGER.debug("Product update has occurred");
		
		try {
			IDownloadProcess downloadProcess = createDownloadProcess(product);
		
			switch (product.getProductProgress().getStatus()) {
			case NOT_STARTED:
			case IDLE:
			case RUNNING:
				ProductDownloadThread productDownloadThread = new ProductDownloadThread(downloadProcess);
				productDownloadExecutor.execute(productDownloadThread);
				break;
			default:
				break;
			}
			
			activeProducts.addProduct(product, downloadProcess);
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
		} catch (DownloadOperationException | ProductNotFoundException e) {
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
				downloadPath = Paths.get(settingsManager.getSetting(UserModifiableSetting.BASE_DOWNLOAD_FOLDER_ABSOLUTE), downloadDirectory).toFile();
			}else{
				downloadPath = Paths.get(settingsManager.getSetting(UserModifiableSetting.BASE_DOWNLOAD_FOLDER_ABSOLUTE)).toFile();
			}
			
			String umSSOUsername = settingsManager.getSetting(UserModifiableSetting.SSO_USERNAME);
			String umSSOPassword = settingsManager.getSetting(UserModifiableSetting.SSO_PASSWORD);
			String proxyHost = settingsManager.getSetting(UserModifiableSetting.WEB_PROXY_HOST);
			String proxyPortString = settingsManager.getSetting(UserModifiableSetting.WEB_PROXY_PORT);
			int proxyPort;
			if (StringUtils.isEmpty(proxyPortString)) {
				proxyPort = -1;
			}else{
				proxyPort = Integer.parseInt(proxyPortString);
			}
			String proxyUsername = settingsManager.getSetting(UserModifiableSetting.WEB_PROXY_USERNAME);
			String proxyPassword = settingsManager.getSetting(UserModifiableSetting.WEB_PROXY_PASSWORD);
			
			return downloadPlugin.createDownloadProcess(uri, downloadPath, umSSOUsername, umSSOPassword, productDownloadListener, proxyHost, proxyPort, proxyUsername, proxyPassword);
		} catch (NoPluginAvailableException | URISyntaxException | DMPluginException ex) {
			LOGGER.error(String.format("Error whilst creating download process of product %s: %s", product.getProductAccessUrl(), ex.getLocalizedMessage()));
			product.getProductProgress().setStatus(EDownloadStatus.IN_ERROR);
			product.getProductProgress().setMessage(ex.getLocalizedMessage());
			
			throw new DownloadProcessCreationException(ex.getLocalizedMessage(), ex);
		}
	}
	
	@Override
	public void updateProductDetails(String productUuid, String productName, Integer numberOfFiles, Long totalFileSize) {
		Product product;
		try {
			product = activeProducts.getProduct(productUuid);
			product.setStartOfFirstDownloadRequest(new Timestamp(new Date().getTime()));
			product.setProductName(productName);
			product.setNumberOfFiles(numberOfFiles);
			product.setTotalFileSize(totalFileSize);

			dataAccessRequestManager.persistProduct(product);
		} catch (ProductNotFoundException e) {
			throw new NonRecoverableException("Unable to retrieve download process for product.", e);
		}
	}

	@Override
	public void updateProgress(String productUuid, ProductProgress productProgress) {
		Product product = null;
		try {
			product = activeProducts.getProduct(productUuid);
		} catch (ProductNotFoundException e1) {
			throw new NonRecoverableException("Unable to retrieve download process for product.", e1);
		}
		
		EDownloadStatus previouslyKnownStatus = product.getProductProgress().getStatus();
		EDownloadStatus newStatus = productProgress.getStatus();
		product.setProductProgress(productProgress);
		IDownloadProcess downloadProcess = null;
		try {
			downloadProcess = activeProducts.getDownloadProcess(productUuid);
		} catch (ProductNotFoundException e) {
			LOGGER.error("Unable to retrieve download process for product.", e);
		}

		if(previouslyKnownStatus != newStatus) {
			LOGGER.debug(String.format("Status has changed from %s to %s, updating to database", previouslyKnownStatus, newStatus));
			if(newStatus == EDownloadStatus.RUNNING) {
				product.setStartOfActualDownload(new Timestamp(new Date().getTime()));
			}
			
			dataAccessRequestManager.persistProduct(product);
		}

		if(previouslyKnownStatus == EDownloadStatus.IDLE && newStatus == EDownloadStatus.NOT_STARTED) {
			ProductDownloadThread productDownloadThread = new ProductDownloadThread(downloadProcess);
			productDownloadExecutor.execute(productDownloadThread);
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
		
		activeProducts.removeProduct(product);
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
		String productDownloadCompleteCommand = settingsManager.getSetting(UserModifiableSetting.PRODUCT_DOWNLOAD_COMPLETE_COMMAND);
		CallbackCommandExecutor callbackExecutor = new CallbackCommandExecutor();
		callbackExecutor.invokeCallbackCommandOnProductFiles(productDownloadCompleteCommand, product.getCompletedDownloadPath());
	}
	
	private void setTerminalStateOfProduct(Product product) {
		product.setStopOfDownload(new Timestamp(new Date().getTime()));
		dataAccessRequestManager.persistProduct(product);
		DataAccessRequest dataAccessRequest = dataAccessRequestManager.findDataAccessRequestByProduct(product);
		productTerminationLog.notifyProductDownloadTermination(product, dataAccessRequest);
	}

	public boolean pauseProductDownload(String productUuid) throws DownloadOperationException, ProductNotFoundException {
		IDownloadProcess downloadProcess = activeProducts.getDownloadProcess(productUuid);
		try {
			downloadProcess.pauseDownload();
		} catch (DMPluginException e) {
			throw new DownloadOperationException(String.format("Unable to pause product download, plugin response: %s", e.getLocalizedMessage()));
		}
		return true;
	}
	
	public boolean resumeProductDownload(String productUuid) throws DownloadOperationException, ProductNotFoundException {
		IDownloadProcess downloadProcess = activeProducts.getDownloadProcess(productUuid);
		ProductDownloadThread productDownloadThread = new ProductDownloadThread(downloadProcess);
		try {
			productDownloadExecutor.execute(productDownloadThread);

			downloadProcess.resumeDownload();
		} catch (DMPluginException e) {
			throw new DownloadOperationException(String.format("Unable to pause product download, plugin response: %s", e.getLocalizedMessage()));
		}
		return true;
	}
	
	public boolean cancelProductDownload(String productUuid) throws DownloadOperationException, ProductNotFoundException {
		IDownloadProcess downloadProcess = activeProducts.getDownloadProcess(productUuid);
		try {
			downloadProcess.cancelDownload();
		} catch (DMPluginException e) {
			throw new DownloadOperationException(String.format("Unable to pause product download, plugin response: %s", e.getLocalizedMessage()));
		}
		return true;
	}
	
	public boolean changeProductPriority(String productUuid, ProductPriority productPriority) throws ProductNotFoundException {
		Product product = activeProducts.getProduct(productUuid);

		product.setPriority(productPriority);
		
		dataAccessRequestManager.persistProduct(product);

		return true;
	}
	
	public void shutdown() {
		LOGGER.info("Shutting down Download Monitor.");

		productDownloadExecutor.shutdown();		

		for (IDownloadProcess downloadProcess : activeProducts.getDownloadProcesses()) {
			try {
				downloadProcess.disconnect();
			} catch (DMPluginException e) {
				LOGGER.error("Unable to disconnect download process, threads may still be open.");
			}
		}
	}
	
	public boolean cancelDownloadsWithStatuses(List<EDownloadStatus> statusesToCancel, boolean includeManualDownloads) throws DownloadOperationException {
		boolean downloadsCancelledCompletely = true;
		for (Entry<String, IDownloadProcess> downloadProcessEntry: activeProducts.getDownloadProcessList().entrySet()) {
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
			throw new DownloadOperationException("Unable to send cancel request to all applicable downloads. Note that some products may be cancelled.");
		}
		return downloadsCancelledCompletely;
	}
}