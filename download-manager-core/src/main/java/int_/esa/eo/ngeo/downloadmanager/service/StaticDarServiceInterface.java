package int_.esa.eo.ngeo.downloadmanager.service;

import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpRequestAndResponse;

import java.net.URL;

public interface StaticDarServiceInterface {
	UmSsoHttpRequestAndResponse getStaticDar(URL staticDarUrl) throws ServiceException;
}
