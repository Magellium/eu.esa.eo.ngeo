package int_.esa.eo.ngeo.downloadmanager.download;

import int_.esa.eo.ngeo.downloadmanager.callback.CallbackCommandExecutor;
import int_.esa.eo.ngeo.downloadmanager.dar.DataAccessRequestManager;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.exception.DownloadOperationException;
import int_.esa.eo.ngeo.downloadmanager.exception.DownloadProcessCreationException;
import int_.esa.eo.ngeo.downloadmanager.exception.NoPluginAvailableException;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.exception.NotificationException;
import int_.esa.eo.ngeo.downloadmanager.exception.ProductNotFoundException;
import int_.esa.eo.ngeo.downloadmanager.http.ConnectionPropertiesSynchronizedUmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpConnectionSettings;
import int_.esa.eo.ngeo.downloadmanager.log.ProductTerminationLog;
import int_.esa.eo.ngeo.downloadmanager.model.ActiveProducts;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.model.ProductProgress;
import int_.esa.eo.ngeo.downloadmanager.notifications.NotificationManager;
import int_.esa.eo.ngeo.downloadmanager.observer.DownloadObserver;
import int_.esa.eo.ngeo.downloadmanager.observer.ProductObserver;
import int_.esa.eo.ngeo.downloadmanager.observer.SettingsObserver;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPlugin;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;
import int_.esa.eo.ngeo.downloadmanager.plugin.PluginManager;
import int_.esa.eo.ngeo.downloadmanager.plugin.ProductDownloadListener;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.apache.http.client.utils.URIBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadMonitor implements ProductObserver, DownloadObserver, SettingsObserver {
    private PluginManager pluginManager;
    private SettingsManager settingsManager;
    private ConnectionPropertiesSynchronizedUmSsoHttpClient connectionPropertiesSynchronizedUmSsoHttpClient;
    private DataAccessRequestManager dataAccessRequestManager;
    private NotificationManager notificationManager;

    private DownloadScheduler downloadScheduler;

    private ActiveProducts activeProducts;

    private ProductTerminationLog productTerminationLog;

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadMonitor.class);
    private List<UserModifiableSetting> userModifiableSettingsToObserve;

    public DownloadMonitor(PluginManager pluginManager, SettingsManager settingsManager, ConnectionPropertiesSynchronizedUmSsoHttpClient connectionPropertiesSynchronizedUmSsoHttpClient, DataAccessRequestManager dataAccessRequestManager, NotificationManager notificationManager) {
        this.pluginManager = pluginManager;
        this.settingsManager = settingsManager;
        this.connectionPropertiesSynchronizedUmSsoHttpClient = connectionPropertiesSynchronizedUmSsoHttpClient;
        this.dataAccessRequestManager = dataAccessRequestManager;
        this.notificationManager = notificationManager;
        
        dataAccessRequestManager.registerObserver(this);
        this.productTerminationLog = new ProductTerminationLog();
        activeProducts = new ActiveProducts();

        userModifiableSettingsToObserve = new ArrayList<>();
        userModifiableSettingsToObserve.add(UserModifiableSetting.NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS);
    }

    @Override
    public void updateToUserModifiableSettings(List<UserModifiableSetting> userModifiableSetting) {
        if(userModifiableSetting != null && !Collections.disjoint(userModifiableSettingsToObserve, userModifiableSetting)) {
            LOGGER.debug("Number of concurrent download threads have changed, so update the number of concurrent threads in the Download Scheduler.");
            int numberOfConcurrentDownloads = Integer.parseInt(settingsManager.getSetting(UserModifiableSetting.NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS));
            downloadScheduler.setConcurrentDownloads(numberOfConcurrentDownloads);
        }
    }

    public void initDowloadPoolAndWatchSettingsManager() {
        String noOfParallelProductDownloadThreads = settingsManager.getSetting(UserModifiableSetting.NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS);
        int concurrentProductDownloadThreads = Integer.parseInt(noOfParallelProductDownloadThreads);
        LOGGER.info(String.format("Creating thread pool for %s concurrent product download threads", concurrentProductDownloadThreads));
        downloadScheduler = new DownloadScheduler(concurrentProductDownloadThreads);
        settingsManager.registerObserver(this);
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
                downloadScheduler.scheduleProductDownload(downloadProcess, product);
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
            URL url = new URL(product.getProductAccessUrl());
            URIBuilder builder = new URIBuilder();
            builder.setHost(url.getHost())
                    .setPort(url.getPort())
                    .setPath(url.getPath())
                    .setQuery(url.getQuery())
                    .setScheme(url.getProtocol());
            
            URI uri = builder.build();
            
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

            UmSsoHttpConnectionSettings httpConnectionSettings = connectionPropertiesSynchronizedUmSsoHttpClient.getUmSsoHttpClient().getUmSsoHttpConnectionSettings();

            return downloadPlugin.createDownloadProcess(uri, downloadPath, httpConnectionSettings.getUmssoUsername(), httpConnectionSettings.getUmssoPassword(), productDownloadListener, httpConnectionSettings.getProxyHost(), httpConnectionSettings.getProxyPort(), httpConnectionSettings.getProxyUser(), httpConnectionSettings.getProxyPassword());
        } catch (NoPluginAvailableException | URISyntaxException | DMPluginException | MalformedURLException ex) {
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
            downloadScheduler.scheduleProductDownload(downloadProcess, product);
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
     * Disconnect and remove the process for a completed or cancelled product.
     * The disconnect ensures that we tidy up any left over threads / resources, any failure of the disconnect does not matter
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
        
        try {
            notificationManager.sendProductTerminationNotification(product);
        }catch(NotificationException ex) {
            LOGGER.error("Unable to send email", ex);
        }
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
        Product product = activeProducts.getProduct(productUuid);
        try {
            downloadScheduler.scheduleProductDownload(downloadProcess, product);

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
        downloadScheduler.changeProductPriority(product);

        return true;
    }

    public void shutdown() {
        LOGGER.info("Shutting down Download Monitor.");

        for (IDownloadProcess downloadProcess : activeProducts.getDownloadProcesses()) {
            try {
                downloadProcess.disconnect();
            } catch (DMPluginException e) {
                LOGGER.error("Unable to disconnect download process, threads may still be open.");
            }
        }

        downloadScheduler.shutdown();
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

    @Override
    public void updateToNonUserModifiableSettings(List<NonUserModifiableSetting> nonUserModifiableSettings) {
        //There are no non-user modifiable settings which this class is interested in
    }
}