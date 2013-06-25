package int_.esa.eo.ngeo.dmtu.webserver.service;

import int_.esa.eo.ngeo.dmtu.exception.ParseException;
import int_.esa.eo.ngeo.dmtu.exception.ServiceException;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLRequ;

import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;

public interface NgeoWebServerServiceInterface {
	HttpMethod registrationMgmt(URL ngEOWebServerUrl, HttpClient httpClient, DMRegistrationMgmntRequ registrationMgmntRequest) throws ParseException, ServiceException;
	HttpMethod monitoringURL(URL ngEOWebServerUrl, HttpClient httpClient, MonitoringURLRequ monitoringUrlRequest) throws ParseException, ServiceException;
	HttpMethod dataAccessMonitoring(URL ngEOWebServerUrl, HttpClient httpClient, DataAccessMonitoringRequ dataAccessMonitoringRequest) throws ParseException, ServiceException;
	//XXX: temporary method to integrate with Terradue's ngEO Web Server implementation
	void login(HttpClient httpClient, String umSsoUsername, String umSsopassword);
}
