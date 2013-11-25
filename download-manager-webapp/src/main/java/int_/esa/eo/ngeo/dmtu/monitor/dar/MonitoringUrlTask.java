package int_.esa.eo.ngeo.dmtu.monitor.dar;

import int_.esa.eo.ngeo.dmtu.controller.DARController;
import int_.esa.eo.ngeo.dmtu.controller.MonitoringController;
import int_.esa.eo.ngeo.dmtu.exception.DataAccessRequestAlreadyExistsException;
import int_.esa.eo.ngeo.dmtu.exception.DownloadOperationException;
import int_.esa.eo.ngeo.dmtu.exception.ServiceException;
import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerRequestBuilder;
import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerResponseParser;
import int_.esa.eo.ngeo.dmtu.webserver.service.NgeoWebServerServiceInterface;
import int_.esa.eo.ngeo.downloadmanager.ResponseHeaderParser;
import int_.esa.eo.ngeo.downloadmanager.UmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.builder.SSOClientBuilder;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLList;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.UserOrder;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.impl.cookie.DateParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.siemens.pse.umsso.client.UmssoHttpPost;
import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

public class MonitoringUrlTask implements Runnable {
	private static final String NGEO_IICD_D_WS_DATE_REQUEST_FORMAT = "yyyy-MM-dd'T'HH:mm:ss zzz";

	private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringUrlTask.class);

	private NgeoWebServerRequestBuilder ngeoWebServerRequestBuilder;
	private NgeoWebServerResponseParser ngeoWebServerResponseParser;
	private NgeoWebServerServiceInterface ngeoWebServerService;
	private DARController darController;
	private MonitoringController monitoringController;
	private TaskScheduler darMonitorScheduler;
	private SettingsManager settingsManager;
 
	private String downloadManagerId;
	private URL monitoringServiceUrl;
	
	private int refreshPeriod;
	
	public MonitoringUrlTask(NgeoWebServerRequestBuilder ngeoWebServerRequestBuilder, NgeoWebServerResponseParser ngeoWebServerResponseParser, NgeoWebServerServiceInterface ngeoWebServerService,  DARController darController, MonitoringController monitoringController, TaskScheduler darMonitorScheduler, SettingsManager settingsManager, String downloadManagerId, URL monitoringServiceUrl) {
		this.ngeoWebServerRequestBuilder = ngeoWebServerRequestBuilder;
		this.ngeoWebServerResponseParser = ngeoWebServerResponseParser;
		this.ngeoWebServerService = ngeoWebServerService;
		this.darController = darController;
		this.monitoringController = monitoringController;
		this.darMonitorScheduler = darMonitorScheduler;
		this.downloadManagerId = downloadManagerId;
		this.monitoringServiceUrl = monitoringServiceUrl;
		this.settingsManager = settingsManager; 
		String defaultRefreshPeriod = settingsManager.getSetting(NonUserModifiableSetting.IICD_D_WS_REFRESH_PERIOD);
		if(defaultRefreshPeriod != null && !defaultRefreshPeriod.isEmpty()) {
			this.refreshPeriod = Integer.parseInt(defaultRefreshPeriod, 10);
		}else{
			this.refreshPeriod = 20;
		}
	}
	
	public void run() {
		LOGGER.debug("Starting MonitoringUrlTask");
		if(monitoringController.isDarMonitoringRunning()) {
			String downloadManagerSetTimeAsString = settingsManager.getSetting(NonUserModifiableSetting.NGEO_MONITORING_SERVICE_SET_TIME);
	
			GregorianCalendar downloadManagerSetTime = null;
			if(downloadManagerSetTimeAsString != null && !downloadManagerSetTimeAsString.isEmpty()) {
				downloadManagerSetTime = convertDateTimeAsStringToGregorianCalendar(downloadManagerSetTimeAsString);
			}
			
			UmSsoHttpClient umSsoHttpClient = new SSOClientBuilder().buildSSOClientFromSettings(settingsManager);

			MonitoringURLRequ monitoringUrlRequest = ngeoWebServerRequestBuilder.buildMonitoringURLRequest(downloadManagerId, downloadManagerSetTime);
			UserOrder userOrder = null;
			UmssoHttpPost request = null;
			MonitoringURLResp monitoringUrlResponse = null;
			
			try {
				request = ngeoWebServerService.monitoringURL(monitoringServiceUrl, umSsoHttpClient, monitoringUrlRequest);
				UmssoHttpResponse response = umSsoHttpClient.getUmssoHttpResponse(request);
				monitoringUrlResponse = ngeoWebServerResponseParser.parseMonitoringURLResponse(monitoringServiceUrl, response);
				Date responseDate = new ResponseHeaderParser().searchForResponseDate(response);
	
				settingsManager.setSetting(NonUserModifiableSetting.NGEO_MONITORING_SERVICE_SET_TIME, convertDateToString(responseDate));
			} catch (ParseException | ServiceException | DateParseException e) {
				LOGGER.error(String.format("%s whilst calling Monitoring URL %s: %s", e.getClass().getName(), monitoringServiceUrl, e.getLocalizedMessage()), e);
			} finally {
				if(request != null) {
					request.reset();
				}
			}

//			Error error = monitoringUrlResponse.getError();
//			if(error != null) {
//				throw new WebServerServiceException(String.format("Error code %s (%s) when MonitoringURL operation. Error detail: %s",error.getErrorCode(), error.getErrorDescription().toString(), error.getErrorDetail()));
//			}

			if(monitoringUrlResponse != null) {
				userOrder = monitoringUrlResponse.getUserOrder();
				if(userOrder != null) {
					LOGGER.info(String.format("User order: %s",userOrder.toString()));
					try {
						monitoringController.sendUserOrder(userOrder);
					} catch (DownloadOperationException e) {
						LOGGER.error(String.format("Exception whilst sending user order %s", userOrder.toString()), e);					}
				}else{
					refreshPeriod = monitoringUrlResponse.getRefreshPeriod().intValue();
					settingsManager.setSetting(NonUserModifiableSetting.IICD_D_WS_REFRESH_PERIOD, Integer.toString(refreshPeriod, 10));
					
					MonitoringURLList monitoringUrls = monitoringUrlResponse.getMonitoringURLList();
					List<String> monitoringUrlList = monitoringUrls.getMonitoringURLs();
					LOGGER.debug(String.format("Monitoring URL has %s new DARs to monitor",monitoringUrlList.size()));
					for (String darMonitoringUrlString : monitoringUrlList) {
						URL darMonitoringUrl;
						try {
							darMonitoringUrl = new URL(darMonitoringUrlString);
							boolean darAdded = false;
							try {
								darAdded = darController.addDataAccessRequest(darMonitoringUrl, true);
							} catch (DataAccessRequestAlreadyExistsException e) {
								LOGGER.warn(String.format("Monitoring URL has %s already been added to the Download Manager.",darMonitoringUrl));
							}
							
							if(darAdded) {
								LOGGER.debug("Starting new dataAccessMonitoringTask from MonitoringUrlTask");
								DataAccessMonitoringTask dataAccessMonitoringTask = new DataAccessMonitoringTask(ngeoWebServerRequestBuilder, ngeoWebServerResponseParser, ngeoWebServerService, darController, monitoringController, darMonitorScheduler, settingsManager, downloadManagerId, darMonitoringUrl, refreshPeriod);
								darMonitorScheduler.schedule(dataAccessMonitoringTask, new Date());
							}
						} catch (MalformedURLException e) {
							LOGGER.error(String.format("Unable to parse ngEO Web Server DAR Monitoring URL %s",darMonitoringUrlString),e);
						}
					}
				}
			}
	
			/* 
			 * Even if an error has ocurred when attempting to call the monitoring service, still schedule the next monitoring request.
			 */
			if(userOrder == null) {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.SECOND, refreshPeriod);
		
				MonitoringUrlTask monitoringUrlTask = new MonitoringUrlTask(ngeoWebServerRequestBuilder, ngeoWebServerResponseParser, ngeoWebServerService, darController, monitoringController, darMonitorScheduler, settingsManager, downloadManagerId, monitoringServiceUrl);
				darMonitorScheduler.schedule(monitoringUrlTask, c.getTime());
			}
			LOGGER.debug("Finished MonitoringUrlTask");
		}else{
			LOGGER.debug("Stop command has been sent, either by the Web Server or manually by the user. Monitoring for new DARs is terminated.");
		}
	}

	//TODO: does this need to be moved into a parse date util class?
	private GregorianCalendar convertDateTimeAsStringToGregorianCalendar(String downloadManagerSetTimeAsString) {
		TimeZone utc = TimeZone.getTimeZone("UTC");

		DateFormat df = new SimpleDateFormat(NGEO_IICD_D_WS_DATE_REQUEST_FORMAT);
		df.setTimeZone(utc);
		Date finalTime = null;

		try {
		    finalTime = df.parse(downloadManagerSetTimeAsString);            
		} catch (java.text.ParseException e) {
		    throw new NonRecoverableException(e);
		}

		GregorianCalendar calendar = new GregorianCalendar(utc);
		calendar.setTime(finalTime);
		
		return calendar;
	}
	
	private String convertDateToString(Date date) {
		DateFormat df = new SimpleDateFormat(NGEO_IICD_D_WS_DATE_REQUEST_FORMAT);
		return df.format(date);
	}
}
