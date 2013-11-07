package int_.esa.eo.ngeo.dmtu.monitor.dar;

import int_.esa.eo.ngeo.dmtu.controller.DARController;
import int_.esa.eo.ngeo.dmtu.controller.MonitoringController;
import int_.esa.eo.ngeo.dmtu.exception.ServiceException;
import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerRequestBuilder;
import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerResponseParser;
import int_.esa.eo.ngeo.dmtu.webserver.service.NgeoWebServerServiceInterface;
import int_.esa.eo.ngeo.downloadmanager.ResponseHeaderParser;
import int_.esa.eo.ngeo.downloadmanager.UmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessList;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.impl.cookie.DateParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.siemens.pse.umsso.client.UmssoHttpPost;
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
	
	public DataAccessMonitoringTask(NgeoWebServerRequestBuilder ngeoWebServerRequestBuilder, NgeoWebServerResponseParser ngeoWebServerResponseParser, NgeoWebServerServiceInterface ngeoWebServerService, DARController darController, MonitoringController monitoringController, TaskScheduler darMonitorScheduler, String downloadManagerId, URL darMonitoringUrl, int refreshPeriod) {
		this.ngeoWebServerRequestBuilder = ngeoWebServerRequestBuilder;
		this.ngeoWebServerResponseParser= ngeoWebServerResponseParser; 
		this.ngeoWebServerService = ngeoWebServerService;
		this.darController = darController;
		this.monitoringController = monitoringController;
		this.darMonitorScheduler = darMonitorScheduler;
		this.downloadManagerId = downloadManagerId;
		this.darMonitoringUrl = darMonitoringUrl;
		this.refreshPeriod = refreshPeriod;
	}
	
	public void run() {
		LOGGER.debug("Starting DataAccessMonitoringTask");

		UmSsoHttpClient umSsoHttpClient = new SSOClientBuilder().buildSSOClientFromSettings(monitoringController);

		/* 
		 * XXX: This should be removed once the Web Client no longer relies on the hooky Web Server 
		 * login procedure to identify the user
		 * The path of this login procedure is /ngeo/login?username=<u>&password=<p>
		 */
		String umSsoUsername = monitoringController.getSetting(SettingsManager.KEY_SSO_USERNAME);
		String umSsoPassword = monitoringController.getSetting(SettingsManager.KEY_SSO_PASSWORD);
		ngeoWebServerService.login(umSsoHttpClient, umSsoUsername, umSsoPassword);

		DataAccessRequest dataAccessRequest = darController.getDataAccessRequestByMonitoringUrl(darMonitoringUrl);
		if(dataAccessRequest == null) {
			throw new NonRecoverableException(String.format("Unable to retrieve internal Data Access Request details: %s", darMonitoringUrl));
		}
		DataAccessMonitoringRequ dataAccessMonitoringRequest = ngeoWebServerRequestBuilder.buildDataAccessMonitoringRequ(downloadManagerId, dataAccessRequest, null);
		MonitoringStatus monitoringStatus = null;

		UmssoHttpPost request = null;
		try {
			request = ngeoWebServerService.dataAccessMonitoring(darMonitoringUrl, umSsoHttpClient, dataAccessMonitoringRequest);
			UmssoHttpResponse response = umSsoHttpClient.getUmssoHttpResponse(request);

			DataAccessMonitoringResp dataAccessMonitoringResponse = ngeoWebServerResponseParser.parseDataAccessMonitoringResponse(darMonitoringUrl, response);
			Date responseDate = new ResponseHeaderParser().searchForResponseDate(response);

//			Error error = dataAccessMonitoringResponse.getError();
//			if(error != null) {
//				throw new NonRecoverableException(String.format("Error code %s (%s) when DataAccessMonitoring operation. Error detail: %s",error.getErrorCode(), error.getErrorDescription().toString(), error.getErrorDetail()));
//			}

			monitoringStatus = dataAccessMonitoringResponse.getMonitoringStatus();
			ProductAccessList productAccessList = dataAccessMonitoringResponse.getProductAccessList();
			
			darController.updateDAR(darMonitoringUrl, monitoringStatus, responseDate, productAccessList);
		} catch (ParseException | ServiceException | DateParseException e) {
			LOGGER.error(String.format("Exception whilst calling DataAccessMonitoring %s: %s", darMonitoringUrl, e.getLocalizedMessage()), e);
		} finally {
			if (request != null) {
				request.releaseConnection();
			}
		}
		
		if(monitoringStatus != null && (monitoringStatus == MonitoringStatus.IN_PROGRESS || monitoringStatus == MonitoringStatus.PAUSED)) {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.SECOND, refreshPeriod);
			
			DataAccessMonitoringTask dataAccessMonitoringTask = new DataAccessMonitoringTask(ngeoWebServerRequestBuilder, ngeoWebServerResponseParser, ngeoWebServerService, darController, monitoringController, darMonitorScheduler, downloadManagerId, darMonitoringUrl, refreshPeriod);
			darMonitorScheduler.schedule(dataAccessMonitoringTask, c.getTime());
		}
		LOGGER.debug("Ending DataAccessMonitoringTask");
	}
}
