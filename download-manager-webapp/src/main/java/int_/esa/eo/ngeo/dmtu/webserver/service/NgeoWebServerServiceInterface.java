package int_.esa.eo.ngeo.dmtu.webserver.service;

import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpRequestAndResponse;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLRequ;

import java.net.URL;

public interface NgeoWebServerServiceInterface {
	UmSsoHttpRequestAndResponse registrationMgmt(URL ngEOWebServerUrl, DMRegistrationMgmntRequ registrationMgmntRequest) throws ServiceException;
	UmSsoHttpRequestAndResponse monitoringURL(URL ngEOWebServerUrl, MonitoringURLRequ monitoringUrlRequest) throws ServiceException;
	UmSsoHttpRequestAndResponse dataAccessMonitoring(URL ngEOWebServerUrl, DataAccessMonitoringRequ dataAccessMonitoringRequest) throws ServiceException;
}
