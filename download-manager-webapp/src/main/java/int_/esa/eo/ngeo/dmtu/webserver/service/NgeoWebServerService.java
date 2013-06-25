package int_.esa.eo.ngeo.dmtu.webserver.service;

import int_.esa.eo.ngeo.dmtu.exception.ParseException;
import int_.esa.eo.ngeo.dmtu.exception.ServiceException;
import int_.esa.eo.ngeo.dmtu.http.HttpUtils;
import int_.esa.eo.ngeo.dmtu.jaxb.JaxbUtils;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLRequ;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NgeoWebServerService implements NgeoWebServerServiceInterface {
	@Autowired
	private JaxbUtils jaxbUtils;

	@Autowired
	private HttpUtils httpUtils;

	private static final Logger LOGGER = LoggerFactory.getLogger(NgeoWebServerService.class);
	private final int BYTE_ARRAY_SIZE = 2000;

	//XXX: temporary method to integrate with Terradue's ngEO Web Server implementation
	public void login(HttpClient httpClient, String umSsoUsername, String umSsoPassword) {
		GetMethod loginMethod = null;
	    try {
		    String loginUrl = String.format("http://5.9.173.44/ngeo/login?format=xml&username=%s&password=%s", umSsoUsername, umSsoPassword);
		    LOGGER.debug(String.format("loginUrl: %s", loginUrl));
			loginMethod = new GetMethod(loginUrl);
		    int httpResponseCode = httpClient.executeMethod(loginMethod);
			LOGGER.debug(String.format("response code: %s", httpResponseCode));
			StringWriter writer = new StringWriter();
			IOUtils.copy(loginMethod.getResponseBodyAsStream(), writer, "UTF-8");
			String responseString = writer.toString();
			LOGGER.debug(String.format("Login response: %s", responseString));
	    } catch(Exception ex) {
    		ex.printStackTrace();
    		LOGGER.error("Can not perform login");
    	} finally {
    		if(loginMethod != null)
    			loginMethod.releaseConnection();
	    }
	}
	
	@Override
	public HttpMethod registrationMgmt(URL ngEOWebServerUrl, HttpClient httpClient, DMRegistrationMgmntRequ registrationMgmntRequest) throws ParseException, ServiceException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_ARRAY_SIZE);
	    jaxbUtils.serializeAndInferSchema(registrationMgmntRequest, baos);
	    LOGGER.debug(String.format("DMRegistrationMgmntRequ (%s):%n %s", ngEOWebServerUrl.toString(), baos.toString()));

	    return httpUtils.postToService(ngEOWebServerUrl, httpClient, baos, "application/xml", "application/xml");
	}

	@Override
	public HttpMethod monitoringURL(URL ngEOWebServerUrl, HttpClient httpClient, MonitoringURLRequ monitoringUrlRequest) throws ParseException, ServiceException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_ARRAY_SIZE);
	    jaxbUtils.serializeAndInferSchema(monitoringUrlRequest, baos);
	    LOGGER.debug(String.format("MonitoringURLRequ (%s):%n %s", ngEOWebServerUrl.toString(), baos.toString()));

	    return httpUtils.postToService(ngEOWebServerUrl, httpClient, baos, "application/xml", "application/xml");
	}

	@Override
	public HttpMethod dataAccessMonitoring(URL ngEOWebServerUrl, HttpClient httpClient, DataAccessMonitoringRequ dataAccessMonitoringRequest) throws ParseException, ServiceException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_ARRAY_SIZE);
	    jaxbUtils.serializeAndInferSchema(dataAccessMonitoringRequest, baos);
	    LOGGER.debug(String.format("DataAccessMonitoringRequ (%s):%n %s", ngEOWebServerUrl.toString(), baos.toString()));

	    return httpUtils.postToService(ngEOWebServerUrl, httpClient, baos, "application/xml", "application/xml");
	}
	
}
