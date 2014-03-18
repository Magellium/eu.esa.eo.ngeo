package int_.esa.eo.ngeo.dmtu.monitor.dar;

import int_.esa.eo.ngeo.dmtu.controller.DARController;
import int_.esa.eo.ngeo.dmtu.controller.MonitoringController;
import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerRequestBuilder;
import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerResponseParser;
import int_.esa.eo.ngeo.dmtu.webserver.service.NgeoWebServerServiceInterface;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.http.ResponseHeaderParser;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpRequestAndResponse;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.impl.cookie.DateParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

public class DataAccessMonitoringTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataAccessMonitoringTask.class);

    private NgeoWebServerRequestBuilder ngeoWebServerRequestBuilder;
    private NgeoWebServerResponseParser ngeoWebServerResponseParser;
    private NgeoWebServerServiceInterface ngeoWebServerService;
    private DARController darController;
    private MonitoringController monitoringController;
    private TaskScheduler darMonitorScheduler;

    private String downloadManagerId;
    private URL darMonitoringUrl;
    private int refreshPeriod;

    public DataAccessMonitoringTask(NgeoWebServerRequestBuilder ngeoWebServerRequestBuilder, NgeoWebServerResponseParser ngeoWebServerResponseParser, NgeoWebServerServiceInterface ngeoWebServerService, DARController darController, MonitoringController monitoringController, TaskScheduler darMonitorScheduler, String downloadManagerId, String darMonitoringUrlString, int refreshPeriod) throws MalformedURLException {
        this.ngeoWebServerRequestBuilder = ngeoWebServerRequestBuilder;
        this.ngeoWebServerResponseParser= ngeoWebServerResponseParser; 
        this.ngeoWebServerService = ngeoWebServerService;
        this.darController = darController;
        this.monitoringController = monitoringController;
        this.darMonitorScheduler = darMonitorScheduler;

        this.downloadManagerId = downloadManagerId;
        this.darMonitoringUrl = new URL(darMonitoringUrlString);
        this.refreshPeriod = refreshPeriod;
    }

    public void run() {
        LOGGER.debug("Starting DataAccessMonitoringTask");

        DataAccessRequest dataAccessRequest = darController.getDataAccessRequestByMonitoringUrl(darMonitoringUrl.toString());
        if(dataAccessRequest == null) {
            throw new NonRecoverableException(String.format("Unable to retrieve internal Data Access Request details: %s", darMonitoringUrl));
        }
        DataAccessMonitoringRequ dataAccessMonitoringRequest = ngeoWebServerRequestBuilder.buildDataAccessMonitoringRequ(downloadManagerId, dataAccessRequest, null);
        MonitoringStatus monitoringStatus = null;

        UmSsoHttpRequestAndResponse webServerRequestAndResponse = null;
        try {
            webServerRequestAndResponse = ngeoWebServerService.dataAccessMonitoring(darMonitoringUrl, dataAccessMonitoringRequest);
            UmssoHttpResponse response = webServerRequestAndResponse.getResponse();

            DataAccessMonitoringResp dataAccessMonitoringResponse = ngeoWebServerResponseParser.parseDataAccessMonitoringResponse(darMonitoringUrl, response);
            Date responseDate = new ResponseHeaderParser().searchForResponseDate(response.getHeaders());

            //  Error error = dataAccessMonitoringResponse.getError();
            //  if(error != null) {
            //      throw new NonRecoverableException(String.format("Error code %s (%s) when DataAccessMonitoring operation. Error detail: %s",error.getErrorCode(), error.getErrorDescription().toString(), error.getErrorDetail()));
            //  }

            monitoringStatus = dataAccessMonitoringResponse.getMonitoringStatus();

            DataAccessRequest updateDar = new DataAccessRequest();
            updateDar.setDarURL(darMonitoringUrl.toString());
            updateDar.setMonitored(true);
            
            darController.updateDataAccessRequest(updateDar, dataAccessMonitoringResponse, responseDate, ProductPriority.NORMAL);
        } catch (ParseException | ServiceException | DateParseException e) {
            LOGGER.error(String.format("%s whilst calling DataAccessMonitoring %s: %s", e.getClass().getName(), darMonitoringUrl, e.getLocalizedMessage()));
            LOGGER.debug("DataAccessMonitoring exception Stack trace:", e);
            scheduleDataAccessMonitoringTask();
        } finally {
            if (webServerRequestAndResponse != null) {
                webServerRequestAndResponse.cleanupHttpResources();
            }
        }

        if(monitoringStatus != null && (monitoringStatus == MonitoringStatus.IN_PROGRESS || monitoringStatus == MonitoringStatus.PAUSED)) {
            scheduleDataAccessMonitoringTask();
        }
        LOGGER.debug("Ending DataAccessMonitoringTask");
    }

    private void scheduleDataAccessMonitoringTask() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.SECOND, refreshPeriod);

        try {
            DataAccessMonitoringTask dataAccessMonitoringTask = new DataAccessMonitoringTask(ngeoWebServerRequestBuilder, ngeoWebServerResponseParser, ngeoWebServerService, darController, monitoringController, darMonitorScheduler, downloadManagerId, darMonitoringUrl.toString(), refreshPeriod);
            darMonitorScheduler.schedule(dataAccessMonitoringTask, c.getTime());
        } catch (MalformedURLException e) {
            LOGGER.error(String.format("Unable to parse ngEO Web Server DAR Monitoring URL %s", darMonitoringUrl.toString()),e);
        }
    }
}
