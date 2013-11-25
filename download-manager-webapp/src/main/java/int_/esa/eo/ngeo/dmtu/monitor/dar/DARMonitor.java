package int_.esa.eo.ngeo.dmtu.monitor.dar;

import int_.esa.eo.ngeo.dmtu.controller.DARController;
import int_.esa.eo.ngeo.dmtu.controller.MonitoringController;
import int_.esa.eo.ngeo.dmtu.exception.ServiceException;
import int_.esa.eo.ngeo.dmtu.exception.WebServerServiceException;
import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerRequestBuilder;
import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerResponseParser;
import int_.esa.eo.ngeo.dmtu.webserver.service.NgeoWebServerServiceInterface;
import int_.esa.eo.ngeo.downloadmanager.UmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.builder.SSOClientBuilder;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.siemens.pse.umsso.client.UmssoHttpPost;
import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

@Component
public class DARMonitor implements ApplicationListener<ContextClosedEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DARMonitor.class);

	@Autowired
	private NgeoWebServerRequestBuilder ngeoWebServerRequestBuilder;

	@Autowired
	private NgeoWebServerResponseParser ngeoWebServerResponseParser;
	
	@Autowired
	private NgeoWebServerServiceInterface ngeoWebServerService;

	@Autowired
	private DARController darController;

	@Autowired
	private MonitoringController monitoringController;

	@Autowired
	private SettingsManager settingsManager;

	@Autowired
	private ThreadPoolTaskScheduler darMonitorScheduler;
	
	/**
	 * Preconditions:
	 * <ul>
	 * 		<li>DM has been set-up, i.e. the "First Startup configuration" has been carried out.</li>
	 * 		<li>DM is not already registered.</li>
	 * </ul>
	 * @throws WebServerServiceException 
	 * @throw {@link NonRecoverableException} if:
	 * <ul>
	 * 		<li>DM has not been set-up</li>
	 * 		<li>DM is already registered.</li>
	 * 		<li>other1</li>
	 * 		<li>other2</li>
	 * </ul>
	 */
	public void registerDownloadManager() throws WebServerServiceException {
		boolean isAlreadyRegistered = Boolean.parseBoolean(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_REGISTERED));
		if (isAlreadyRegistered) throw new NonRecoverableException("This download manager is already registered!");
		
		boolean setup = Boolean.parseBoolean(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_SETUP));
		if (!setup) throw new NonRecoverableException("Download manager cannot be registered before the \"First Startup configuration\" has been carried out");
		
		LOGGER.info("Registering download manager.");

		String downloadManagerId = UUID.randomUUID().toString().replaceAll("-", "");
		settingsManager.setSetting(NonUserModifiableSetting.DM_ID, downloadManagerId);
		String downloadManagerFriendlyName = settingsManager.getSetting(UserModifiableSetting.DM_FRIENDLY_NAME);
		String ngEOWebServer = settingsManager.getSetting(NonUserModifiableSetting.NGEO_WEB_SERVER_REGISTRATION_URLS);
		URL ngEOWebServerUrl;
		try {
			ngEOWebServerUrl = new URL(ngEOWebServer);
		} catch (MalformedURLException e) {
			throw new WebServerServiceException(String.format("Unable to parse ngEO Web Server URL %s",ngEOWebServer),e);
		}

		UmSsoHttpClient umSsoHttpClient = new SSOClientBuilder().buildSSOClientFromSettings(settingsManager);

		DMRegistrationMgmntRequ registrationMgmntRequest = ngeoWebServerRequestBuilder.buildDMRegistrationMgmntRequest(downloadManagerId, downloadManagerFriendlyName);
		UmssoHttpPost request = null;
		try {
			request = ngeoWebServerService.registrationMgmt(ngEOWebServerUrl, umSsoHttpClient, registrationMgmntRequest);
			UmssoHttpResponse response = umSsoHttpClient.getUmssoHttpResponse(request);

			DMRegistrationMgmntResp registrationMgmtResponse = ngeoWebServerResponseParser.parseDMRegistrationMgmntResponse(ngEOWebServerUrl, response);
		
			String montoringServiceUrl = registrationMgmtResponse.getMonitoringServiceUrl();
			settingsManager.setSetting(NonUserModifiableSetting.DM_IS_REGISTERED, "true");
			settingsManager.setSetting(NonUserModifiableSetting.NGEO_WEB_SERVER_DAR_MONITORING_URLS, montoringServiceUrl);
			LOGGER.info(String.format("Registration complete, monitoring service URL: %s", montoringServiceUrl));
		} catch (ParseException | ServiceException e) {
			throw new WebServerServiceException(String.format("Exception occurred whilst attempting to register the Download Manager: %s", e.getLocalizedMessage()));
		} finally {
			if (request != null) {
				request.releaseConnection();
			}
		}
	}

	public void monitorForDARs() {
		boolean isAlreadyRegistered = Boolean.parseBoolean(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_REGISTERED));
		if (!isAlreadyRegistered) {
			LOGGER.info("Download manager is not registered, so monitoring for DARs will not occur.");
		}else{
			LOGGER.debug("monitoring for DARs...");
			String downloadManagerId = settingsManager.getSetting(NonUserModifiableSetting.DM_ID);
			String monitoringService = settingsManager.getSetting(NonUserModifiableSetting.NGEO_WEB_SERVER_DAR_MONITORING_URLS);
			URL monitoringServiceUrl;
			try {
				monitoringServiceUrl = new URL(monitoringService);
			} catch (MalformedURLException e) {
				throw new NonRecoverableException(String.format("Unable to parse monitoring service URL %s",monitoringService),e);
			}
	
			MonitoringUrlTask monitoringUrlTask = new MonitoringUrlTask(ngeoWebServerRequestBuilder, ngeoWebServerResponseParser, ngeoWebServerService, darController, monitoringController, darMonitorScheduler, settingsManager, downloadManagerId, monitoringServiceUrl);
			darMonitorScheduler.schedule(monitoringUrlTask, new Date());
		}
	}
	
	public void monitorForProductsFromLoadedDARs() {
		boolean isAlreadyRegistered = Boolean.parseBoolean(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_REGISTERED));
		if (!isAlreadyRegistered) {
			LOGGER.info("Download manager is not registered, so monitoring for products will not occur.");
		}else{
			LOGGER.debug("monitoring for Products from previously discovered DARs...");
			List<DataAccessRequest> dataAccessRequests = darController.getDataAccessRequestStatus(false).getDataAccessRequests();
			LOGGER.debug(String.format("%s in progress / paused DARs found.", dataAccessRequests.size()));
			
			String downloadManagerId = settingsManager.getSetting(NonUserModifiableSetting.DM_ID);
			String defaultRefreshPeriod = settingsManager.getSetting(NonUserModifiableSetting.IICD_D_WS_REFRESH_PERIOD);
			int refreshPeriod;
			if(defaultRefreshPeriod != null && !defaultRefreshPeriod.isEmpty()) {
				refreshPeriod = Integer.parseInt(defaultRefreshPeriod, 10);
			}else{
				refreshPeriod = 20;
			}

			for (DataAccessRequest dataAccessRequest : dataAccessRequests) {
				if(dataAccessRequest.isMonitored() && dataAccessRequest.getMonitoringStatus() != MonitoringStatus.COMPLETED) {
					URL darMonitoringUrl;
					try {
						darMonitoringUrl = new URL(dataAccessRequest.getDarURL());
						DataAccessMonitoringTask dataAccessMonitoringTask = new DataAccessMonitoringTask(ngeoWebServerRequestBuilder, ngeoWebServerResponseParser, ngeoWebServerService, darController, monitoringController, darMonitorScheduler, settingsManager, downloadManagerId, darMonitoringUrl , refreshPeriod);
						darMonitorScheduler.schedule(dataAccessMonitoringTask, new Date());
					} catch (MalformedURLException e) {
						LOGGER.error(String.format("Unable to parse ngEO Web Server DAR Monitoring URL %s", dataAccessRequest.getDarURL()),e);
					}
				}
			}
		}
	}
	
	@Override
	public void onApplicationEvent(ContextClosedEvent arg0) {
		LOGGER.info("Shutting down DAR Monitor Scheduler");
		darMonitorScheduler.shutdown();	
	}
}
