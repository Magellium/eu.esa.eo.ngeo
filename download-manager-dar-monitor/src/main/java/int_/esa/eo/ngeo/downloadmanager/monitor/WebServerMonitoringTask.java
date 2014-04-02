package int_.esa.eo.ngeo.downloadmanager.monitor;

import int_.esa.eo.ngeo.downloadmanager.builder.DataAccessRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.controller.DARMonitorController;
import int_.esa.eo.ngeo.downloadmanager.exception.DataAccessRequestAlreadyExistsException;
import int_.esa.eo.ngeo.downloadmanager.exception.DownloadOperationException;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.http.ResponseHeaderParser;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpRequestAndResponse;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.observer.WebServerMonitoringObserver;
import int_.esa.eo.ngeo.downloadmanager.observer.WebServerMonitoringSubject;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.util.WebServerDateParser;
import int_.esa.eo.ngeo.downloadmanager.webserver.NgeoWebServerServiceHelper;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.Error;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLList;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.UserOrder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.impl.cookie.DateParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

public class WebServerMonitoringTask implements Runnable, WebServerMonitoringSubject {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerMonitoringTask.class);

    private final WebServerMonitoringDetails webServerMonitoringDetails;
    private final DARMonitorController darMonitorController;
    private final SettingsManager settingsManager;
    
    private NgeoWebServerServiceHelper ngeoWebServerServiceHelper;
    private UmSsoHttpRequestAndResponse webServerRequestAndResponse;
    private String downloadManagerId;
    
    private List<WebServerMonitoringObserver> observers;

    public WebServerMonitoringTask(WebServerMonitoringDetails webServerMonitoringDetails, SettingsManager settingsManager, DARMonitorController darMonitorController, NgeoWebServerServiceHelper ngeoWebServerServiceHelper) {
        this.webServerMonitoringDetails = webServerMonitoringDetails;
        this.settingsManager = settingsManager;
        this.darMonitorController = darMonitorController;
        this.observers = new ArrayList<>();
        this.ngeoWebServerServiceHelper = ngeoWebServerServiceHelper;
    }
    
    @Override
    public void run() {
        webServerRequestAndResponse = null;
     
        downloadManagerId = settingsManager.getSetting(NonUserModifiableSetting.DM_ID);
        
        
        LOGGER.debug("Starting WebServerMonitoringTask, check if DAR monitoring is still running.");
        if(darMonitorController.isDarMonitoringRunning()) {
            UserOrder userOrder = monitorForNewDars();
            monitorForNewProducts();
            
            /* 
             * Even if an error has ocurred when attempting to call the monitoring service, still schedule the next monitoring request.
             */
            if(userOrder == null) {
                notifyObservers(webServerMonitoringDetails);
            }
            LOGGER.debug("Finished WebServerMonitoringTask");
        }else{
            LOGGER.info("Stop command has been sent, either by the Web Server or manually by the user. Monitoring for new DARs is terminated.");
        }
    }

    @Override
    public void registerObserver(WebServerMonitoringObserver o) {
        this.observers.add(o);
    }

    @Override
    public void notifyObservers(WebServerMonitoringDetails webServerMonitoringDetails) {
        for (WebServerMonitoringObserver o : observers) {
            o.scheduleWebServerMonitoring(webServerMonitoringDetails);
        }
    }
    
    public UserOrder monitorForNewDars() {
        String downloadManagerSetTimeAsString = settingsManager.getSetting(NonUserModifiableSetting.NGEO_MONITORING_SERVICE_SET_TIME);

        WebServerDateParser webServerDateParser = new WebServerDateParser();
        GregorianCalendar downloadManagerSetTime = null;
        if(downloadManagerSetTimeAsString != null && !downloadManagerSetTimeAsString.isEmpty()) {
            downloadManagerSetTime = webServerDateParser.convertDateTimeAsStringToGregorianCalendar(downloadManagerSetTimeAsString);
        }
        MonitoringURLRequ monitoringUrlRequest = ngeoWebServerServiceHelper.getRequestBuilder().buildMonitoringURLRequest(downloadManagerId, downloadManagerSetTime);

        UserOrder userOrder = null;
        MonitoringURLResp monitoringUrlResponse = null;

        try {
            webServerRequestAndResponse = ngeoWebServerServiceHelper.getService().monitoringURL(webServerMonitoringDetails.getDarMonitoringUrl(), monitoringUrlRequest);
            UmssoHttpResponse response = webServerRequestAndResponse.getResponse();
            monitoringUrlResponse = ngeoWebServerServiceHelper.getResponseParser().parseMonitoringURLResponse(webServerMonitoringDetails.getDarMonitoringUrl(), response);

            Date responseDate = new ResponseHeaderParser().searchForResponseDate(response.getHeaders());

            settingsManager.setNonUserModifiableSetting(NonUserModifiableSetting.NGEO_MONITORING_SERVICE_SET_TIME, webServerDateParser.convertDateToString(responseDate));
        } catch (ParseException | ServiceException | DateParseException e) {
            LOGGER.error(String.format("%s whilst calling Monitoring URL %s: %s", e.getClass().getName(), webServerMonitoringDetails.getDarMonitoringUrl(), e.getLocalizedMessage()), e);
        } finally {
            if(webServerRequestAndResponse != null) {
                webServerRequestAndResponse.cleanupHttpResources();
            }
        }

        if(monitoringUrlResponse != null) {
            Error error = monitoringUrlResponse.getError();
            if (error != null) {
                LOGGER.error(String.format(
                        "Error code %s (%s) when MonitoringURL operation. Error detail: %s",
                        error.getErrorCode(), error.getErrorDescription().toString(),
                        error.getErrorDetail()));
            }else{
                userOrder = monitoringUrlResponse.getUserOrder();
                if(userOrder != null) {
                    LOGGER.info(String.format("User order: %s",userOrder.toString()));
                    try {
                        darMonitorController.sendUserOrder(userOrder);
                    } catch (DownloadOperationException e) {
                        LOGGER.error(String.format("Exception whilst sending user order %s", userOrder.toString()), e);
                    }
                }else{
                    int refreshPeriod = monitoringUrlResponse.getRefreshPeriod().intValue();
                    webServerMonitoringDetails.setRefreshPeriod(refreshPeriod);
                    settingsManager.setNonUserModifiableSetting(NonUserModifiableSetting.IICD_D_WS_REFRESH_PERIOD, Integer.toString(refreshPeriod, 10));

                    MonitoringURLList monitoringUrls = monitoringUrlResponse.getMonitoringURLList();
                    List<String> monitoringUrlList = monitoringUrls.getMonitoringURLs();
                    LOGGER.debug(String.format("Monitoring URL has %s new DARs to monitor",monitoringUrlList.size()));
                    for (String darMonitoringUrlString : monitoringUrlList) {
                        try {
                            boolean darAdded = false;
                            try {
                                DataAccessRequest newDar = new DataAccessRequestBuilder().buildDAR(darMonitoringUrlString, null, true);
                                darAdded = darMonitorController.addDataAccessRequest(newDar);
                            } catch (DataAccessRequestAlreadyExistsException e) {
                                LOGGER.warn(String.format("Monitoring URL has %s already been added to the Download Manager.", darMonitoringUrlString));
                            }
    
                            if(darAdded) {
                                URL darMonitoringUrl = new URL(darMonitoringUrlString);
                                webServerMonitoringDetails.getProductMonitoringUrls().add(darMonitoringUrl);
                            }
                        } catch (MalformedURLException e) {
                            LOGGER.error(String.format("Unable to parse ngEO Web Server DAR Monitoring URL %s", darMonitoringUrlString),e);
                        }
                    }
                }
            }
        }
        return userOrder;
    }
    
    public void monitorForNewProducts() {
        LOGGER.debug("Start monitoring known DARs for new products.");
        //iterate through a copy of the product monitoring URLs
        List<URL> productMonitoringUrlList = new ArrayList<>(webServerMonitoringDetails.getProductMonitoringUrls());
        for (URL productMonitoringUrl : productMonitoringUrlList) {
            String productMonitoringUrlAsString = productMonitoringUrl.toString();
            LOGGER.debug(String.format("Starting DataAccessMonitoringTask: %s", productMonitoringUrl.toString()));

            DataAccessRequest dataAccessRequest = darMonitorController.getDataAccessRequestByMonitoringUrl(productMonitoringUrlAsString);
            if(dataAccessRequest == null) {
                throw new NonRecoverableException(String.format("Unable to retrieve internal Data Access Request details: %s", productMonitoringUrl));
            }
            DataAccessMonitoringRequ dataAccessMonitoringRequest = ngeoWebServerServiceHelper.getRequestBuilder().buildDataAccessMonitoringRequ(downloadManagerId, dataAccessRequest, null);
            MonitoringStatus monitoringStatus = null;

            webServerRequestAndResponse = null;
            try {
                webServerRequestAndResponse = ngeoWebServerServiceHelper.getService().dataAccessMonitoring(productMonitoringUrl, dataAccessMonitoringRequest);
                UmssoHttpResponse response = webServerRequestAndResponse.getResponse();

                DataAccessMonitoringResp dataAccessMonitoringResponse = ngeoWebServerServiceHelper.getResponseParser().parseDataAccessMonitoringResponse(productMonitoringUrl, response);
                Date responseDate = new ResponseHeaderParser().searchForResponseDate(response.getHeaders());

                Error error = dataAccessMonitoringResponse.getError();
                if (error != null) {
                    LOGGER.error(String.format(
                            "Error code %s (%s) when MonitoringURL operation. Error detail: %s",
                            error.getErrorCode(), error.getErrorDescription().toString(),
                            error.getErrorDetail()));
                }else{
                    monitoringStatus = dataAccessMonitoringResponse.getMonitoringStatus();

                    DataAccessRequest updateDar = new DataAccessRequest();
                    updateDar.setDarURL(productMonitoringUrlAsString);
                    updateDar.setMonitored(true);
                    
                    darMonitorController.updateDataAccessRequest(updateDar, dataAccessMonitoringResponse, responseDate, ProductPriority.NORMAL);

                    //remove URL from the list which are in a terminal state, therefore stops the monitoring
                    if(monitoringStatus != null && (monitoringStatus == MonitoringStatus.CANCELLED || monitoringStatus == MonitoringStatus.COMPLETED)) {
                        webServerMonitoringDetails.getProductMonitoringUrls().remove(productMonitoringUrl);
                    }
                }
            } catch (ParseException | ServiceException | DateParseException e) {
                LOGGER.error(String.format("%s whilst calling DataAccessMonitoring %s: %s", e.getClass().getName(), productMonitoringUrlAsString, e.getLocalizedMessage()));
                LOGGER.debug("DataAccessMonitoring exception Stack trace:", e);
            } finally {
                if (webServerRequestAndResponse != null) {
                    webServerRequestAndResponse.cleanupHttpResources();
                }
            }
            LOGGER.debug("Ending DataAccessMonitoringTask");
        }
    }
}
