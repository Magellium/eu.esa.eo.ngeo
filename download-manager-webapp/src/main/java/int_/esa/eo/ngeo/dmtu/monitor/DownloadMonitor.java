package int_.esa.eo.ngeo.dmtu.monitor;

import int_.esa.eo.ngeo.dmtu.exception.DownloadOperationException;
import int_.esa.eo.ngeo.dmtu.exception.DownloadProcessCreationException;
import int_.esa.eo.ngeo.dmtu.exception.NoPluginAvailableException;
import int_.esa.eo.ngeo.dmtu.exception.NonRecoverableException;
import int_.esa.eo.ngeo.dmtu.manager.DataAccessRequestManager;
import int_.esa.eo.ngeo.dmtu.manager.PluginManager;
import int_.esa.eo.ngeo.dmtu.manager.SettingsManager;
import int_.esa.eo.ngeo.dmtu.model.Product;
import int_.esa.eo.ngeo.dmtu.model.ProductProgress;
import int_.esa.eo.ngeo.dmtu.observer.DownloadObserver;
import int_.esa.eo.ngeo.dmtu.observer.ProductObserver;
import int_.esa.eo.ngeo.dmtu.os.command.OSCommandExecutor;
import int_.esa.eo.ngeo.dmtu.os.command.OSCommandExecutor.CommandResultHandler;
import int_.esa.eo.ngeo.dmtu.plugin.ProductDownloadListener;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPlugin;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.commons.exec.CommandLine;
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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadMonitor.class);
	private static final Logger PRODUCT_DOWNLOAD_TERMINATION_NOTIFICATION_LOGGER = LoggerFactory.getLogger("ProductDownloadTermination");

	private static final String FILESET_MACRO_REF = "${FILESET}";
	private static final String FILE_MACRO_REF    = "${FILE}";

	private static final boolean EXEC_IN_BACKGROUND = true;

	private static final int SUCCESSFUL_OS_COMMAND_EXIT_CODE = 0;

	@Autowired
	public DownloadMonitor(DataAccessRequestManager dataAccessRequestManager) {
		this.dataAccessRequestManager = dataAccessRequestManager;
		dataAccessRequestManager.registerObserver(this);
		this.downloadProcessList = new HashMap<>();
		this.productToDownloadList = new HashMap<String, Product>();
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
			notifyProductDownloadTermination(product);
		}
		
		if(productProgress.getStatus() == EDownloadStatus.COMPLETED) {
			
			
			// We implement the call-back mechanism here, i.e. before we lose the list of downloaded files
			try {
				invokeAnyCallbackCommand(getDownloadProcess(productUuid).getDownloadedFiles());
			} catch (DownloadOperationException e) { // This catch block should be unreachable
				LOGGER.error("Unable to invoke post-download callback on product " + productUuid, e);
			}
			
			
			downloadProcessList.remove(productUuid);	
			productToDownloadList.remove(productUuid);
		}
	}
	
	private void invokeAnyCallbackCommand(File[] downloadedFiles) {
		String unresolvedCommand = settingsManager.getSetting(SettingsManager.KEY_PRODUCT_DOWNLOAD_COMPLETE_COMMAND);
		if (unresolvedCommand == null || unresolvedCommand.isEmpty()) {
			LOGGER.debug("No post-download call-back command has been defined");
			return; // Nothing to do
		}
		if (unresolvedCommand.contains(FILESET_MACRO_REF)) {
			// TODO: Execute the command once, passing in each and every pathname. (How should the pathnames be separated?)
			throw new UnsupportedOperationException(String.format("TODO: Execute the command \"%s\" once, replacing %s with each and every pathname. (How should the pathnames be separated?)",
					unresolvedCommand, FILESET_MACRO_REF));
		}
		else if(unresolvedCommand.contains(FILE_MACRO_REF)) {
			for (File file : downloadedFiles) {
				LOGGER.debug(String.format("Unresolved call-back command = %s", unresolvedCommand));

				CommandLine commandLine = new CommandLine(getOSShellName()); // FIXME: Specification of the shell wrapper needs to cope with MacOS and Linux/Unix (and maybe Win95...)
				commandLine.addArgument("/c");
				commandLine.addArgument(unresolvedCommand);

				// build up the command line using a 'java.io.File'
				Map<String, Object> map = new HashMap<>();
				map.put("FILE", file);
				
				commandLine.setSubstitutionMap(map);
				
				OSCommandExecutor osCommandExecutor = new OSCommandExecutor();
				CommandResultHandler resultHandler = null;
				try {
					resultHandler = osCommandExecutor.execute(commandLine, 60000, EXEC_IN_BACKGROUND, SUCCESSFUL_OS_COMMAND_EXIT_CODE);
					resultHandler.waitFor();
				}
				catch (InterruptedException | IOException e) {
					LOGGER.error(String.format("Error invoking command \"%s\": %s", commandLine.toString(), e.getMessage()));
				}
				
				LOGGER.debug(String.format("Process exit code for command \"%s\" = %s", commandLine.toString(), resultHandler.getExitValue()));
			}			
		} 
		
	}

	private String getOSShellName() {		
		String osName = System.getProperty("os.name");
		return osName.toLowerCase().startsWith("windows") ? "cmd.exe" : "bash";
	}

	private String quotePathNameAccordingToOS(String absolutePath) {
		return String.format("\"%s\"", absolutePath); // TODO: Handle scenarios where the OS is other than Windows.
	}

	private void notifyProductDownloadTermination(Product product) {
		StringBuilder productNotificationString = new StringBuilder();
		ProductProgress productProgress = product.getProductProgress();
		EDownloadStatus productDownloadStatus = productProgress.getStatus();
		productNotificationString.append(String.format("\"%s\"", productDownloadStatus));
		productNotificationString.append(String.format(",\"%s\"", product.getProductAccessUrl()));
		productNotificationString.append(String.format(",\"%s\"", "")); //TODO: Name of the data access request the product download belongs to
		long numberOfBytesDownloaded;
		if(productDownloadStatus == EDownloadStatus.IN_ERROR) {
			numberOfBytesDownloaded = 0;
		}else{
			numberOfBytesDownloaded = productProgress.getDownloadedSize();
		}
		productNotificationString.append(String.format(",\"%s\"", numberOfBytesDownloaded));
		productNotificationString.append(String.format(",\"%s\"", "")); //TODO: Start date/time of first download request (i.e. when the initial http get request is made)
		productNotificationString.append(String.format(",\"%s\"", "")); //TODO: Start Date/Time of actual download (if the download is delayed, this time is different from the previous one)
		productNotificationString.append(String.format(",\"%s\"", "")); //TODO: Stop Date/Time of download

		if(productDownloadStatus == EDownloadStatus.COMPLETED) {
			productNotificationString.append(String.format(",\"%s\"", "")); //TODO: The Path of the saved product (if completed)
		}else{
			productNotificationString.append(",");
		}
		
		if(productDownloadStatus == EDownloadStatus.IN_ERROR) {
			productNotificationString.append(String.format(",\"%s\"", productProgress.getMessage()));
		}else{
			productNotificationString.append(",");
		}
		
		
		PRODUCT_DOWNLOAD_TERMINATION_NOTIFICATION_LOGGER.info(productNotificationString.toString());
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
}