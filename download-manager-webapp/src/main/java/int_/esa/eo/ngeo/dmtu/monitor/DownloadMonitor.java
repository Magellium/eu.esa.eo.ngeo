package int_.esa.eo.ngeo.dmtu.monitor;

import int_.esa.eo.ngeo.dmtu.callback.CallbackCommandExecutor;
import int_.esa.eo.ngeo.dmtu.exception.DownloadOperationException;
import int_.esa.eo.ngeo.dmtu.exception.DownloadProcessCreationException;
import int_.esa.eo.ngeo.dmtu.exception.NoPluginAvailableException;
import int_.esa.eo.ngeo.dmtu.exception.NonRecoverableException;
import int_.esa.eo.ngeo.dmtu.log.ProductTerminationLog;
import int_.esa.eo.ngeo.dmtu.manager.DataAccessRequestManager;
import int_.esa.eo.ngeo.dmtu.manager.PluginManager;
import int_.esa.eo.ngeo.dmtu.manager.SettingsManager;
import int_.esa.eo.ngeo.dmtu.model.Product;
import int_.esa.eo.ngeo.dmtu.model.ProductProgress;
import int_.esa.eo.ngeo.dmtu.observer.DownloadObserver;
import int_.esa.eo.ngeo.dmtu.observer.ProductObserver;
import int_.esa.eo.ngeo.dmtu.plugin.ProductDownloadListener;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPlugin;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class DownloadMonitor implements ProductObserver, DownloadObserver, ApplicationListener<ContextClosedEvent> {
	@Autowired
	private PluginManager pluginManager;

	@Autowired
	private SettingsManager settingsManager;
	
	private DataAccessRequestManager dataAccessRequestManager;
	
	private Map<String, IDownloadProcess> downloadProcessList;

	private ExecutorService productDownloadExecutor;
	
	private Map<String,Product> productToDownloadList;

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

	@PostConstruct
	public void initDowloadPool() {
		String noOfParallelProductDownloadThreads = settingsManager.getSetting("NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS");
		int concurrentProductDownloadThreads = Integer.parseInt(noOfParallelProductDownloadThreads);
		LOGGER.info(String.format("Creating thread pool for %s concurrent product download threads", concurrentProductDownloadThreads));
		productDownloadExecutor = Executors.newFixedThreadPool(concurrentProductDownloadThreads);
	}
	
	@Override
	public void update(Product product) {
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
			String proxyUrl = settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_URL);
			String proxyPortString = settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_PORT);
			int proxyPort;
			if (proxyPortString == null || proxyPortString.isEmpty()) {
				proxyPort = -1;
			}else{
				proxyPort = Integer.parseInt(proxyPortString);
			}
			String proxyUsername = settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_USERNAME);
			String proxyPassword = settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_PASSWORD);
			
			return downloadPlugin.createDownloadProcess(uri, downloadPath, umSSOUsername, umSSOPassword, productDownloadListener, proxyUrl, proxyPort, proxyUsername, proxyPassword);
		} catch (NoPluginAvailableException | URISyntaxException | DMPluginException ex) {
			LOGGER.error(String.format("Error whilst creating download process of product %s: %s", product.getProductAccessUrl(), ex.getLocalizedMessage()));
			product.getProductProgress().setStatus(EDownloadStatus.IN_ERROR);
			product.getProductProgress().setMessage(ex.getLocalizedMessage());
			
			throw new DownloadProcessCreationException(ex.getLocalizedMessage(), ex);
		}
	}
	
	@Override
	public void updateProgress(String productUuid, ProductProgress productProgress) {
		Product product = productToDownloadList.get(productUuid);
		EDownloadStatus previouslyKnownStatus = product.getProductProgress().getStatus();
		EDownloadStatus newStatus = productProgress.getStatus();
		product.setProductProgress(productProgress);

		if(previouslyKnownStatus != newStatus) {
			LOGGER.debug(String.format("Status has changed from %s to %s, updating to database", previouslyKnownStatus, newStatus));
			if(newStatus == EDownloadStatus.RUNNING) {
				product.setStartOfActualDownload(new Date());
			}
			
			dataAccessRequestManager.persistProductStatusChange(productUuid);
		}

		if(previouslyKnownStatus == EDownloadStatus.IDLE && newStatus == EDownloadStatus.NOT_STARTED) {
			IDownloadProcess downloadProcess;
			try {
				downloadProcess = getDownloadProcess(productUuid);
				DownloadThread downloadThread = new DownloadThread(downloadProcess);
				productDownloadExecutor.execute(downloadThread);
			} catch (DownloadOperationException e) {
				LOGGER.error("Unable to set the previously idle process to not started.", e);
			}
		}
		if(productProgress.getStatus() == EDownloadStatus.COMPLETED || productProgress.getStatus() == EDownloadStatus.CANCELLED || productProgress.getStatus() == EDownloadStatus.IN_ERROR) {
			product.setStopOfDownload(new Date());
			dataAccessRequestManager.persistProductStatusChange(productUuid);
			productTerminationLog.notifyProductDownloadTermination(product);
		}
		
		if(productProgress.getStatus() == EDownloadStatus.COMPLETED) {
			// We implement the call-back mechanism here, i.e. before we lose the list of downloaded files
			try {
				String productDownloadCompleteCommand = settingsManager.getSetting(SettingsManager.KEY_PRODUCT_DOWNLOAD_COMPLETE_COMMAND);
				CallbackCommandExecutor callbackExecutor = new CallbackCommandExecutor();
				callbackExecutor.invokeCallbackCommandOnProductFiles(productDownloadCompleteCommand, getDownloadProcess(productUuid).getDownloadedFiles());
			} catch (DownloadOperationException e) { // This catch block should be unreachable
				LOGGER.error("Unable to invoke post-download callback on product " + productUuid, e);
			}
		}
		if(productProgress.getStatus() == EDownloadStatus.COMPLETED || productProgress.getStatus() == EDownloadStatus.CANCELLED) {
			IDownloadProcess downloadProcess = downloadProcessList.get(productUuid);
			//This disconnect ensures that we tidy up any left over threads / resources
			try {
				downloadProcess.disconnect();
			} catch (DMPluginException e) {
				//Any disconnect that fails doesn't matter - the process should be handled by the garbage collector once removed from this monitor
			}
			
			downloadProcessList.remove(productUuid);
			productToDownloadList.remove(productUuid);
		}
		
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

	@Override
	public void onApplicationEvent(ContextClosedEvent arg0) {
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
	
	public void cancelAutomatedDownloadsWithStatuses(List<EDownloadStatus> statusesToCancel) {
		for (Entry<String, IDownloadProcess> downloadProcessEntry: downloadProcessList.entrySet()) {
			String productUuid = downloadProcessEntry.getKey();
			IDownloadProcess downloadProcess = downloadProcessEntry.getValue();
			if(!dataAccessRequestManager.isProductDownloadManual(productUuid) && statusesToCancel.contains(downloadProcess.getStatus())) {
				try {
					downloadProcess.cancelDownload();
				} catch (DMPluginException e) {
					LOGGER.error(String.format("Unable to cancel product download with UUID %s. Reason: %s.", productUuid, e.getLocalizedMessage()));
				}
			}
		}
		
	}
}