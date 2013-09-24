package int_.esa.eo.ngeo.dmtu.webserver.service;

import int_.esa.eo.ngeo.dmtu.exception.ServiceException;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLRequ;
import int_.esa.umsso.UmSsoHttpClient;

import java.net.URL;

import org.apache.commons.httpclient.HttpMethod;

public interface NgeoWebServerServiceInterface {
	HttpMethod registrationMgmt(URL ngEOWebServerUrl, UmSsoHttpClient umSsoHttpClient, DMRegistrationMgmntRequ registrationMgmntRequest) throws ServiceException;
	HttpMethod monitoringURL(URL ngEOWebServerUrl, UmSsoHttpClient umSsoHttpClient, MonitoringURLRequ monitoringUrlRequest) throws ServiceException;
	HttpMethod dataAccessMonitoring(URL ngEOWebServerUrl, UmSsoHttpClient umSsoHttpClient, DataAccessMonitoringRequ dataAccessMonitoringRequest) throws ServiceException;
	//XXX: temporary method to integrate with Terradue's ngEO Web Server implementation
	void login(UmSsoHttpClient umSsoHttpClient, String umSsoUsername, String umSsopassword);
}
