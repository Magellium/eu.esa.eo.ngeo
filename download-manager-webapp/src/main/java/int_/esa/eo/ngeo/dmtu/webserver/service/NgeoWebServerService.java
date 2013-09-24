package int_.esa.eo.ngeo.dmtu.webserver.service;

import int_.esa.eo.ngeo.dmtu.exception.ServiceException;
import int_.esa.eo.ngeo.dmtu.manager.SettingsManager;
import int_.esa.eo.ngeo.dmtu.utils.HttpHeaderParser;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.SchemaNotFoundException;
import int_.esa.eo.ngeo.downloadmanager.transform.XMLWithSchemaTransformer;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLRequ;
import int_.esa.umsso.UmSsoHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.siemens.pse.umsso.client.UmssoCLException;

@Component
public class NgeoWebServerService implements NgeoWebServerServiceInterface {
	@Autowired
	private XMLWithSchemaTransformer xmlWithSchemaTransformer;

	@Autowired
	private SettingsManager settingsManager;

	@Autowired
	private HttpHeaderParser httpUtils;

	private static final Logger LOGGER = LoggerFactory.getLogger(NgeoWebServerService.class);
	private final int BYTE_ARRAY_SIZE = 2000;

	//XXX: temporary method to integrate with Terradue's ngEO Web Server implementation
	public void login(UmSsoHttpClient umSsoHttpClient, String umSsoUsername, String umSsoPassword) {
		HttpMethod loginMethod = null;
		String loginUrlString = settingsManager.getSetting("TEMP_WEBS_LOGIN_URL");
	    try {
			URL loginUrl = new URL(loginUrlString);
		    LOGGER.debug(String.format("loginUrl: %s", loginUrl));

		    loginMethod = umSsoHttpClient.executeGetRequest(loginUrl);

			StringWriter writer = new StringWriter();
			IOUtils.copy(loginMethod.getResponseBodyAsStream(), writer, "UTF-8");
			String responseString = writer.toString();
			LOGGER.debug(String.format("Login response: %s", responseString));
	    } catch(Exception ex) {
    		LOGGER.warn(String.format("Cannot perform the login that's temporarily necessary because of the non-UM-SSO-conformant nature of the ngEO Web Server's security. URL used was %s.%nYou can ignore this warning if you are using the mock web server.", loginUrlString));
    	} finally {
    		if(loginMethod != null)
    			loginMethod.releaseConnection();
	    }
	}
	
	@Override
	public HttpMethod registrationMgmt(URL ngEOWebServerUrl, UmSsoHttpClient umSsoHttpClient, DMRegistrationMgmntRequ registrationMgmntRequest) throws ServiceException {
		return sendNgeoWebServerRequest(ngEOWebServerUrl, umSsoHttpClient, registrationMgmntRequest);
	}

	@Override
	public HttpMethod monitoringURL(URL ngEOWebServerUrl, UmSsoHttpClient umSsoHttpClient, MonitoringURLRequ monitoringUrlRequest) throws ServiceException {
		return sendNgeoWebServerRequest(ngEOWebServerUrl, umSsoHttpClient, monitoringUrlRequest);
	}

	@Override
	public HttpMethod dataAccessMonitoring(URL ngEOWebServerUrl, UmSsoHttpClient umSsoHttpClient, DataAccessMonitoringRequ dataAccessMonitoringRequest) throws ServiceException {
		return sendNgeoWebServerRequest(ngEOWebServerUrl, umSsoHttpClient, dataAccessMonitoringRequest);
	}

	public HttpMethod sendNgeoWebServerRequest(URL ngEOWebServerUrl, UmSsoHttpClient umSsoHttpClient, Object requestObject) throws ServiceException {
	    try {
		    ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_ARRAY_SIZE);
		    xmlWithSchemaTransformer.serializeAndInferSchema(requestObject, baos);
		    LOGGER.debug(String.format("DataAccessMonitoringRequ (%s):%n %s", ngEOWebServerUrl.toString(), baos.toString()));

		    return umSsoHttpClient.executePostRequest(ngEOWebServerUrl, baos, "application/xml", "application/xml");
		} catch (UmssoCLException | IOException | SchemaNotFoundException | ParseException e) {
			throw new ServiceException(e);
		}

	}
}
