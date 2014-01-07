package int_.esa.eo.ngeo.dmtu.webserver.service;

import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.SchemaNotFoundException;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.http.ConnectionPropertiesSynchronizedUmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpRequestAndResponse;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.transform.XMLWithSchemaTransformer;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLRequ;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.siemens.pse.umsso.client.UmssoCLException;
import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

@Service
public class NgeoWebServerService implements NgeoWebServerServiceInterface {
	@Autowired
	private XMLWithSchemaTransformer xmlWithSchemaTransformer;
	
	@Autowired
	private SettingsManager settingsManager;
	
	@Autowired
	private ConnectionPropertiesSynchronizedUmSsoHttpClient connectionPropertiesSynchronizedUmSsoHttpClient;

	private static final Logger LOGGER = LoggerFactory.getLogger(NgeoWebServerService.class);
	private final int BYTE_ARRAY_SIZE = 2000;

	@Override
	public UmSsoHttpRequestAndResponse registrationMgmt(URL ngEOWebServerUrl, DMRegistrationMgmntRequ registrationMgmntRequest) throws ServiceException {
		return sendNgeoWebServerRequest(ngEOWebServerUrl, registrationMgmntRequest);
	}

	@Override
	public UmSsoHttpRequestAndResponse monitoringURL(URL ngEOWebServerUrl, MonitoringURLRequ monitoringUrlRequest) throws ServiceException {
		return sendNgeoWebServerRequest(ngEOWebServerUrl, monitoringUrlRequest);
	}

	@Override
	public UmSsoHttpRequestAndResponse dataAccessMonitoring(URL ngEOWebServerUrl, DataAccessMonitoringRequ dataAccessMonitoringRequest) throws ServiceException {
		return sendNgeoWebServerRequest(ngEOWebServerUrl, dataAccessMonitoringRequest);
	}

	private UmSsoHttpRequestAndResponse sendNgeoWebServerRequest(URL ngEOWebServerUrl, Object requestObject) throws ServiceException {
		attemptLoginUsingNonUmssoCredentials();
		
		try {
		    ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_ARRAY_SIZE);
		    xmlWithSchemaTransformer.serializeAndInferSchema(requestObject, baos);
		    LOGGER.debug(String.format("ngEO Web Server request (%s):%n %s", ngEOWebServerUrl.toString(), baos.toString()));

		    return connectionPropertiesSynchronizedUmSsoHttpClient.getUmSsoHttpClient().executePostRequest(ngEOWebServerUrl, baos, "application/xml", "application/xml");
		} catch (UmssoCLException | IOException | SchemaNotFoundException | ParseException e) {
			throw new ServiceException(e);
		}
	}

	private boolean displayNonUmssoLoginWarning = true;

	/*
	 * non-UM-SSO method to integrate with ngEO Web Server. This should only be used when UM-SSO is not available.
	 */
	private void attemptLoginUsingNonUmssoCredentials() {
		String nonUmssoLoginUrl = settingsManager.getSetting(NonUserModifiableSetting.NGEO_WEB_SERVER_NON_UMSSO_LOGIN_URL);
		if(StringUtils.isNotBlank(nonUmssoLoginUrl)) {
			UmSsoHttpRequestAndResponse loginRequestAndResponse = null;
			try {
			    LOGGER.debug(String.format("Performing login to Web Server using non-UM-SSO credentials: %s", nonUmssoLoginUrl));
				URL loginUrl = new URL(nonUmssoLoginUrl);
	
				loginRequestAndResponse = connectionPropertiesSynchronizedUmSsoHttpClient.getUmSsoHttpClient().executeGetRequest(loginUrl);
			    UmssoHttpResponse loginResponse = loginRequestAndResponse.getResponse();
			    
				StringWriter writer = new StringWriter();
				IOUtils.copy(new ByteArrayInputStream(loginResponse.getBody()), writer, "UTF-8");
				String responseString = writer.toString();
				LOGGER.debug(String.format("Login response: %s", responseString));
		    } catch(Exception ex) {
		    	if(displayNonUmssoLoginWarning) {
		    		LOGGER.warn(String.format("Cannot perform non-UM-SSO login to ngEO Web Server.%n" +
		    				"URL used was %s.%n" +
		    				"Exception: %s%n" +
		    				"This warning will only be displayed the first time the non-UM-SSO login is attempted.", nonUmssoLoginUrl, ex.getMessage()));
		    		
		    		displayNonUmssoLoginWarning = false;
		    	}
	    	}finally{
	    		if(loginRequestAndResponse != null) {
	    			loginRequestAndResponse.cleanupHttpResources();
	    		}
	    	}
		}
	}
}
