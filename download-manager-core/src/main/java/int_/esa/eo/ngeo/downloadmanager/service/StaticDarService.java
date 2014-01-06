package int_.esa.eo.ngeo.downloadmanager.service;

import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.http.ConnectionPropertiesSynchronizedUmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpRequestAndResponse;

import java.io.IOException;
import java.net.URL;

import com.siemens.pse.umsso.client.UmssoCLException;

public class StaticDarService implements StaticDarServiceInterface {
	private ConnectionPropertiesSynchronizedUmSsoHttpClient connectionPropertiesSynchronizedUmSsoHttpClient;
	
	public StaticDarService(ConnectionPropertiesSynchronizedUmSsoHttpClient umSsoClientSingleton) {
		this.connectionPropertiesSynchronizedUmSsoHttpClient = umSsoClientSingleton;
	}
	
	@Override
	public UmSsoHttpRequestAndResponse getStaticDar(URL staticDarUrl) throws ServiceException {
		try {
			return connectionPropertiesSynchronizedUmSsoHttpClient.getUmSsoHttpClient().executeGetRequest(staticDarUrl);
		} catch (UmssoCLException | IOException e) {
			throw new ServiceException(e);
		}
	}
}
