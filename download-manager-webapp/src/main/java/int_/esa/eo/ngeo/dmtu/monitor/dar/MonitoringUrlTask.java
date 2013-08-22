package int_.esa.eo.ngeo.dmtu.monitor.dar;

import int_.esa.eo.ngeo.dmtu.controller.DARController;
import int_.esa.eo.ngeo.dmtu.exception.DataAccessRequestAlreadyExistsException;
import int_.esa.eo.ngeo.dmtu.exception.NonRecoverableException;
import int_.esa.eo.ngeo.dmtu.exception.ParseException;
import int_.esa.eo.ngeo.dmtu.exception.ServiceException;
import int_.esa.eo.ngeo.dmtu.manager.SettingsManager;
import int_.esa.eo.ngeo.dmtu.utils.HttpHeaderParser;
import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerRequestBuilder;
import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerResponseParser;
import int_.esa.eo.ngeo.dmtu.webserver.service.NgeoWebServerServiceInterface;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLList;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.UserOrder;
import int_.esa.umsso.UmSsoHttpClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

public class MonitoringUrlTask implements Runnable {
	private static final String NGEO_IICD_D_WS_DATE_REQUEST_FORMAT = "yyyy-MM-dd'T'HH:mm:ss zzz";

	private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringUrlTask.class);

	private NgeoWebServerRequestBuilder ngeoWebServerRequestBuilder;
	private NgeoWebServerResponseParser ngeoWebServerResponseParser;
	private NgeoWebServerServiceInterface ngeoWebServerService;
	private DARController darController;
	private TaskScheduler darMonitorScheduler;
 
	private String downloadManagerId;
	private URL monitoringServiceUrl;
	
	private int refreshPeriod;
	
	public MonitoringUrlTask(NgeoWebServerRequestBuilder ngeoWebServerRequestBuilder, NgeoWebServerResponseParser ngeoWebServerResponseParser, NgeoWebServerServiceInterface ngeoWebServerService,  DARController darController, TaskScheduler darMonitorScheduler, String downloadManagerId, URL monitoringServiceUrl) {
		this.ngeoWebServerRequestBuilder = ngeoWebServerRequestBuilder;
		this.ngeoWebServerResponseParser = ngeoWebServerResponseParser;
		this.ngeoWebServerService = ngeoWebServerService;
		this.darController = darController;
		this.darMonitorScheduler = darMonitorScheduler;
		this.downloadManagerId = downloadManagerId;
		this.monitoringServiceUrl = monitoringServiceUrl;
		String defaultRefreshPeriod = darController.getSetting(SettingsManager.KEY_IICD_D_WS_DEFAULT_REFRESH_PERIOD);
		if(defaultRefreshPeriod != null && !defaultRefreshPeriod.isEmpty()) {
			this.refreshPeriod = Integer.parseInt(defaultRefreshPeriod, 10);
		}else{
			this.refreshPeriod = 20;
		}
	}
	
	public void run() {
		LOGGER.debug("Starting MonitoringUrlTask");
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setParameter("http.useragent", "ngEO Download Manager Test Unit");

		String umSsoUsername = darController.getSetting(SettingsManager.KEY_SSO_USERNAME);
		String umSsoPassword = darController.getSetting(SettingsManager.KEY_SSO_PASSWORD);
		String downloadManagerSetTimeAsString = darController.getSetting(SettingsManager.KEY_NGEO_MONITORING_SERVICE_SET_TIME);

		GregorianCalendar downloadManagerSetTime = null;
		if(downloadManagerSetTimeAsString != null && !downloadManagerSetTimeAsString.isEmpty()) {
			downloadManagerSetTime = convertDateTimeAsStringToGregorianCalendar(downloadManagerSetTimeAsString);
		}
		
		UmSsoHttpClient umSsoHttpClient = new UmSsoHttpClient(umSsoUsername, umSsoPassword, "", -1, "", "", true);
		//XXX: This should be replaced with UM-SSO when implemented
		ngeoWebServerService.login(umSsoHttpClient, umSsoUsername, umSsoPassword);

		MonitoringURLRequ monitoringUrlRequest = ngeoWebServerRequestBuilder.buildMonitoringURLRequest(downloadManagerId, downloadManagerSetTime);
		UserOrder userOrder = null;
		HttpMethod method = null;
		try {
			method = ngeoWebServerService.monitoringURL(monitoringServiceUrl, umSsoHttpClient, monitoringUrlRequest);
			MonitoringURLResp monitoringUrlResponse = ngeoWebServerResponseParser.parseMonitoringURLResponse(monitoringServiceUrl, method);
			Date responseDate = new HttpHeaderParser().getDateFromResponseHTTPHeaders(method);

			darController.setSetting(SettingsManager.KEY_NGEO_MONITORING_SERVICE_SET_TIME, convertDateToString(responseDate));

//			Error error = monitoringUrlResponse.getError();
//			if(error != null) {
//				throw new WebServerServiceException(String.format("Error code %s (%s) when MonitoringURL operation. Error detail: %s",error.getErrorCode(), error.getErrorDescription().toString(), error.getErrorDetail()));
//			}

			userOrder = monitoringUrlResponse.getUserOrder();
			if(userOrder != null) {
				//XXX: Terradue's service currently does not supply a response with the UserOrder in a format which we can understand (non-conformance to schema)
				LOGGER.info(String.format("User order: %s",userOrder.toString()));
				darController.sendUserOrder(userOrder);
			}else{
				refreshPeriod = monitoringUrlResponse.getRefreshPeriod().intValue();
				darController.setSetting(SettingsManager.KEY_IICD_D_WS_DEFAULT_REFRESH_PERIOD, Integer.toString(refreshPeriod, 10));
				
				MonitoringURLList monitoringUrls = monitoringUrlResponse.getMonitoringURLList();
				List<String> monitoringUrlList = monitoringUrls.getMonitoringURL();
				LOGGER.debug(String.format("Monitoring URL has %s new DARs to monitor",monitoringUrlList.size()));
				for (String darMonitoringUrlString : monitoringUrlList) {
					URL darMonitoringUrl;
					try {
						darMonitoringUrl = new URL(darMonitoringUrlString);
						boolean darAdded = false;
						try {
							darAdded = darController.addDataAccessRequest(darMonitoringUrl);
						} catch (DataAccessRequestAlreadyExistsException e) {
							LOGGER.warn(String.format("Monitoring URL has %s already been added to the Download Manager.",darMonitoringUrl));
						}
						
						if(darAdded) {
							LOGGER.debug("Starting new dataAccessMonitoringTask from MonitoringUrlTask");
							DataAccessMonitoringTask dataAccessMonitoringTask = new DataAccessMonitoringTask(ngeoWebServerRequestBuilder, ngeoWebServerResponseParser, ngeoWebServerService, darController, darMonitorScheduler, downloadManagerId, darMonitoringUrl, refreshPeriod);
							darMonitorScheduler.schedule(dataAccessMonitoringTask, new Date());
						}
					} catch (MalformedURLException e) {
						LOGGER.error(String.format("Unable to parse ngEO Web Server DAR Monitoring URL %s",darMonitoringUrlString),e);
					}
				}
			}
		} catch (ParseException | ServiceException e) {
			LOGGER.error(String.format("Exception whilst calling Monitoring URL %s: %s", monitoringServiceUrl, e.getLocalizedMessage()), e);
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}

		if(userOrder == null) {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.SECOND, refreshPeriod);
	
			MonitoringUrlTask monitoringUrlTask = new MonitoringUrlTask(ngeoWebServerRequestBuilder, ngeoWebServerResponseParser, ngeoWebServerService, darController, darMonitorScheduler, downloadManagerId, monitoringServiceUrl);
			darMonitorScheduler.schedule(monitoringUrlTask, c.getTime());
		}
		LOGGER.debug("Finished MonitoringUrlTask");
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
