package int_.esa.eo.ngeo.dmtu.monitor.dar;

import int_.esa.eo.ngeo.dmtu.controller.DARController;
import int_.esa.eo.ngeo.dmtu.exception.NonRecoverableException;
import int_.esa.eo.ngeo.dmtu.exception.ParseException;
import int_.esa.eo.ngeo.dmtu.exception.ServiceException;
import int_.esa.eo.ngeo.dmtu.manager.SettingsManager;
import int_.esa.eo.ngeo.dmtu.model.DataAccessRequest;
import int_.esa.eo.ngeo.dmtu.utils.HttpHeaderParser;
import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerRequestBuilder;
import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerResponseParser;
import int_.esa.eo.ngeo.dmtu.webserver.service.NgeoWebServerServiceInterface;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessList;
import int_.esa.umsso.UmSsoHttpClient;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.httpclient.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

public class DataAccessMonitoringTask implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataAccessMonitoringTask.class);

	private NgeoWebServerRequestBuilder ngeoWebServerRequestBuilder;
	private NgeoWebServerResponseParser ngeoWebServerResponseParser;
	private NgeoWebServerServiceInterface ngeoWebServerService;
	private DARController darController;
	private TaskScheduler darMonitorScheduler;
 
	private String downloadManagerId;
	private URL darMonitoringUrl;
	private int refreshPeriod;
	
	public DataAccessMonitoringTask(NgeoWebServerRequestBuilder ngeoWebServerRequestBuilder, NgeoWebServerResponseParser ngeoWebServerResponseParser, NgeoWebServerServiceInterface ngeoWebServerService, DARController darController, TaskScheduler darMonitorScheduler, String downloadManagerId, URL darMonitoringUrl, int refreshPeriod) {
		this.ngeoWebServerRequestBuilder = ngeoWebServerRequestBuilder;
		this.ngeoWebServerResponseParser= ngeoWebServerResponseParser; 
		this.ngeoWebServerService = ngeoWebServerService;
		this.darController = darController;
		this.darMonitorScheduler = darMonitorScheduler;
		this.downloadManagerId = downloadManagerId;
		this.darMonitoringUrl = darMonitoringUrl;
		this.refreshPeriod = refreshPeriod;
	}
	
	public void run() {
		LOGGER.debug("Starting DataAccessMonitoringTask");

		String umSsoUsername = darController.getSetting(SettingsManager.KEY_SSO_USERNAME);
		String umSsoPassword = darController.getSetting(SettingsManager.KEY_SSO_PASSWORD);

		UmSsoHttpClient umSsoHttpClient = new UmSsoHttpClient(umSsoUsername, umSsoPassword, "", -1, "", "", true);
		//XXX: This should be replaced with UM-SSO when implemented
		ngeoWebServerService.login(umSsoHttpClient, umSsoUsername, umSsoPassword);

		DataAccessRequest dataAccessRequest = darController.getDataAccessRequestByMonitoringUrl(darMonitoringUrl);
		if(dataAccessRequest == null) {
			throw new NonRecoverableException(String.format("Unable to retrieve internal Data Access Request details: %s", darMonitoringUrl));
		}
		DataAccessMonitoringRequ dataAccessMonitoringRequest = ngeoWebServerRequestBuilder.buildDataAccessMonitoringRequ(downloadManagerId, dataAccessRequest, null);
		MonitoringStatus monitoringStatus = null;

		HttpMethod method = null;
		try {
			method = ngeoWebServerService.dataAccessMonitoring(darMonitoringUrl, umSsoHttpClient, dataAccessMonitoringRequest);
			DataAccessMonitoringResp dataAccessMonitoringResponse = ngeoWebServerResponseParser.parseDataAccessMonitoringResponse(darMonitoringUrl, method);
			Date responseDate = new HttpHeaderParser().getDateFromResponseHTTPHeaders(method);

//			Error error = dataAccessMonitoringResponse.getError();
//			if(error != null) {
//				throw new NonRecoverableException(String.format("Error code %s (%s) when DataAccessMonitoring operation. Error detail: %s",error.getErrorCode(), error.getErrorDescription().toString(), error.getErrorDetail()));
//			}

			monitoringStatus = dataAccessMonitoringResponse.getMonitoringStatus();
			ProductAccessList productAccessList = dataAccessMonitoringResponse.getProductAccessList();
			
			darController.updateDAR(darMonitoringUrl, monitoringStatus, responseDate, productAccessList);
		} catch (ParseException | ServiceException e) {
			LOGGER.error(String.format("Exception whilst calling DataAccessMonitoring %s: %s", darMonitoringUrl, e.getLocalizedMessage()), e);
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}
		
		if(monitoringStatus != null && (monitoringStatus == MonitoringStatus.IN_PROGRESS || monitoringStatus == MonitoringStatus.PAUSED)) {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.SECOND, refreshPeriod);
			
			DataAccessMonitoringTask dataAccessMonitoringTask = new DataAccessMonitoringTask(ngeoWebServerRequestBuilder, ngeoWebServerResponseParser, ngeoWebServerService, darController, darMonitorScheduler, downloadManagerId, darMonitoringUrl, refreshPeriod);
			darMonitorScheduler.schedule(dataAccessMonitoringTask, c.getTime());
		}
		LOGGER.debug("Ending DataAccessMonitoringTask");
	}
}
