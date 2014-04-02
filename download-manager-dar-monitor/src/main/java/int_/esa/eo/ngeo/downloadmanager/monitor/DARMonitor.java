package int_.esa.eo.ngeo.downloadmanager.monitor;

import int_.esa.eo.ngeo.downloadmanager.controller.DARMonitorController;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.exception.WebServerServiceException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.observer.WebServerMonitoringObserver;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.webserver.NgeoWebServerServiceHelper;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class DARMonitor implements WebServerMonitoringObserver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DARMonitor.class);

    private SettingsManager settingsManager;
    private DARMonitorController darMonitorController;
    private ThreadPoolTaskScheduler darMonitorScheduler;
    private NgeoWebServerServiceHelper ngeoWebServerServiceHelper;
    
    private WebServerMonitoring webServerMonitoring;

    public DARMonitor(SettingsManager settingsManager, DARMonitorController darMonitorController, ThreadPoolTaskScheduler darMonitorScheduler, NgeoWebServerServiceHelper ngeoWebServerServiceHelper) {
        this.settingsManager = settingsManager;
        this.darMonitorController = darMonitorController;
        this.darMonitorScheduler = darMonitorScheduler;
        this.ngeoWebServerServiceHelper = ngeoWebServerServiceHelper;
    }
    
    /*
     * Initialise the DAR Monitor
     */
    public void init() {
        String defaultRefreshPeriod = settingsManager.getSetting(NonUserModifiableSetting.IICD_D_WS_REFRESH_PERIOD);
        if(defaultRefreshPeriod != null && !defaultRefreshPeriod.isEmpty()) {
            this.webServerMonitoring = new WebServerMonitoring(Integer.parseInt(defaultRefreshPeriod, 10));
        }else{
            this.webServerMonitoring = new WebServerMonitoring(20);
        }
        

        initWebServerMonitoringMap();
        startWebServerMonitoring();
    }
    
    private void initWebServerMonitoringMap() {
        boolean isAlreadyRegistered = Boolean.parseBoolean(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_REGISTERED));
        if (!isAlreadyRegistered) {
            LOGGER.info("Download manager is not registered, so monitoring for DARs will not occur.");
        }else{
            LOGGER.debug("Load monitoring URLs into monitoring map");
            
            String darMonitoringUrls = settingsManager.getSetting(NonUserModifiableSetting.NGEO_WEB_SERVER_DAR_MONITORING_URLS);
            String[] darMonitoringUrlsArray = darMonitoringUrls.split(";");
            if(darMonitoringUrlsArray.length == 0) {
                LOGGER.warn("No urls available for monitoring.");
            }else{
                for (String darMonitoringUrl : darMonitoringUrlsArray) {
                    URL monitoringServiceUrl;
                    try {
                        monitoringServiceUrl = new URL(darMonitoringUrl);
                    } catch (MalformedURLException e) {
                        throw new NonRecoverableException(String.format("Unable to parse monitoring service URL %s", darMonitoringUrl),e);
                    }

                    WebServerMonitoringDetails ngEOWebServerMonitoring = new WebServerMonitoringDetails(monitoringServiceUrl, webServerMonitoring.getInitialRefreshPeriod());
                    webServerMonitoring.getWebServerMonitoringMap().put(darMonitoringUrl, ngEOWebServerMonitoring);
                }
            }
            
            LOGGER.debug("Load products from previously discovered DARs...");
            List<DataAccessRequest> monitoredDataAccessRequests = darMonitorController.getDataAccessRequestStatus(false).getDataAccessRequests();
            LOGGER.debug(String.format("%s in progress / paused DARs found.", monitoredDataAccessRequests.size()));

            for (DataAccessRequest dataAccessRequest : monitoredDataAccessRequests) {
                if(dataAccessRequest.isMonitored() && dataAccessRequest.getMonitoringStatus() != MonitoringStatus.COMPLETED) {
                    //add dar URLs to web server monitoring object
                    WebServerMonitoringDetails ngEOWebServerMonitoring = webServerMonitoring.getWebServerMonitoringMap().get(dataAccessRequest.getWebServerMonitoringURL());
                    if(ngEOWebServerMonitoring == null) {
                        LOGGER.warn(String.format("Web server monitoring URLs have changed, DAR %s cannot be monitored", dataAccessRequest.getDarURL()));
                    }else{
                        try {
                            ngEOWebServerMonitoring.getProductMonitoringUrls().add(new URL(dataAccessRequest.getDarURL()));
                        }catch(MalformedURLException ex) {
                            LOGGER.error(String.format("Unable to add dar for monitoring, %s is malformed.", dataAccessRequest.getDarURL()));
                        }
                    }
                }
            }
        }        
    }
   
    /**
     * Preconditions:
     * <ul>
     *      <li>DM has been set-up, i.e. the "First Startup configuration" has been carried out.</li>
     *      <li>DM is not already registered.</li>
     * </ul>
     * @throws WebServerServiceException 
     * @throw {@link NonRecoverableException} if:
     * <ul>
     *      <li>DM has not been set-up</li>
     *      <li>DM is already registered.</li>
     *      <li>other1</li>
     *      <li>other2</li>
     * </ul>
     */
    public void registerDownloadManager() throws WebServerServiceException {
        boolean isAlreadyRegistered = Boolean.parseBoolean(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_REGISTERED));
        if (isAlreadyRegistered) {
            throw new NonRecoverableException("This download manager is already registered!");
        }

        boolean setup = Boolean.parseBoolean(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_SETUP));
        if (!setup) {
            throw new NonRecoverableException("Download manager cannot be registered before the \"First Startup configuration\" has been carried out");
        }

        String downloadManagerId = UUID.randomUUID().toString().replaceAll("-", "");
        settingsManager.setNonUserModifiableSetting(NonUserModifiableSetting.DM_ID, downloadManagerId);

        String ngEOWebServers = settingsManager.getSetting(NonUserModifiableSetting.NGEO_WEB_SERVER_REGISTRATION_URLS);
        String downloadManagerFriendlyName = settingsManager.getSetting(UserModifiableSetting.DM_FRIENDLY_NAME);

        String[] ngEOWebServerArray = ngEOWebServers.split(";");
        if(ngEOWebServerArray.length == 0) {
            throw new NonRecoverableException("No web servers are available for registration");
        }

        List<URL> monitoringUrlList = new ArrayList<>();
        for (String ngEOWebServer : ngEOWebServerArray) {
            try {
                URL ngEOWebServerUrl = new URL(ngEOWebServer);

                DMRegistration dmRegistration = new DMRegistration(ngEOWebServerUrl, downloadManagerId, downloadManagerFriendlyName, ngeoWebServerServiceHelper);
                String darMonitoringUrl = dmRegistration.register();
                URL monitoringServiceUrl;
                try {
                    monitoringServiceUrl = new URL(darMonitoringUrl);
                } catch (MalformedURLException e) {
                    throw new NonRecoverableException(String.format("Unable to parse monitoring service URL %s", darMonitoringUrl),e);
                }
                monitoringUrlList.add(monitoringServiceUrl);
                WebServerMonitoringDetails ngEOWebServerMonitoring = new WebServerMonitoringDetails(monitoringServiceUrl, webServerMonitoring.getInitialRefreshPeriod());
                webServerMonitoring.getWebServerMonitoringMap().put(darMonitoringUrl, ngEOWebServerMonitoring);
            } catch (MalformedURLException e) {
                LOGGER.error(String.format("Unable to parse ngEO Web Server URL %s", ngEOWebServer),e);
            }
        }

        if(monitoringUrlList.isEmpty()) {
            throw new NonRecoverableException(String.format("Unable to register with specified web servers: %s", ngEOWebServers));
        }
        if(monitoringUrlList.size() != ngEOWebServerArray.length) {
            LOGGER.warn(String.format("Number of registered Web Servers (%s) does not match the number specified in configuration (%s)", monitoringUrlList.size(), ngEOWebServerArray.length));
        }
        
        settingsManager.setNonUserModifiableSetting(NonUserModifiableSetting.DM_IS_REGISTERED, "true");
        settingsManager.setNonUserModifiableSetting(NonUserModifiableSetting.NGEO_WEB_SERVER_DAR_MONITORING_URLS, StringUtils.join(monitoringUrlList.iterator(), ";"));
    }

    @SuppressWarnings("unchecked")
    public void startWebServerMonitoring() {
        for (WebServerMonitoringDetails webServerMonitoringDetails : webServerMonitoring.getWebServerMonitoringMap().values()) {
            WebServerMonitoringTask webServerMonitoringTask = new WebServerMonitoringTask(webServerMonitoringDetails, settingsManager, darMonitorController, ngeoWebServerServiceHelper);
            webServerMonitoringTask.registerObserver(this);
            ScheduledFuture<WebServerMonitoringTask> monitoringTask = darMonitorScheduler.schedule(webServerMonitoringTask, new Date());
            webServerMonitoring.getWebServerMonitoringTaskMap().put(webServerMonitoringDetails.getDarMonitoringUrl().toString(), monitoringTask);
        }
    }

    private void stopWebServerMonitoring() {
        HashMap<String, ScheduledFuture<WebServerMonitoringTask>> webServerMonitoringTaskMap = webServerMonitoring.getWebServerMonitoringTaskMap();
        for (ScheduledFuture<WebServerMonitoringTask> monitoringTask : webServerMonitoringTaskMap.values()) {
            monitoringTask.cancel(true);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void scheduleWebServerMonitoring(WebServerMonitoringDetails webServerMonitoringDetails) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.SECOND, webServerMonitoringDetails.getRefreshPeriod());

        WebServerMonitoringTask webServerMonitoringTask = new WebServerMonitoringTask(webServerMonitoringDetails, settingsManager, darMonitorController, ngeoWebServerServiceHelper);
        webServerMonitoringTask.registerObserver(this);
        ScheduledFuture<WebServerMonitoringTask> monitoringTask = darMonitorScheduler.schedule(webServerMonitoringTask, c.getTime());
        webServerMonitoring.getWebServerMonitoringTaskMap().put(webServerMonitoringDetails.getDarMonitoringUrl().toString(), monitoringTask);
    }
    
    public void restartMonitoringAfterChangeOfCredentials() {
        stopWebServerMonitoring();
        startWebServerMonitoring();
    }

    public void shutdown() {
        LOGGER.info("Shutting down DAR Monitor Scheduler");
        darMonitorScheduler.shutdown();
    }
}
