package int_.esa.eo.ngeo.downloadmanager.monitor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.exception.WebServerServiceException;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpRequestAndResponse;
import int_.esa.eo.ngeo.downloadmanager.webserver.NgeoWebServerServiceHelper;
import int_.esa.eo.ngeo.downloadmanager.webserver.builder.NgeoWebServerRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.webserver.builder.NgeoWebServerResponseParser;
import int_.esa.eo.ngeo.downloadmanager.webserver.service.NgeoWebServerServiceInterface;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntResp;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

public class DMRegistrationTest {
    private DMRegistration dmRegistration;
    NgeoWebServerServiceHelper ngeoWebServerServiceHelper;
    
    private URL webServerRegisterUrl;
    private static final String WEB_SERVER_MONITORING_URL = "http://localhost:8080/download-manager-mock-web-server/monitor";
    private static final String DOWNLOAD_MANAGER_ID = "80dcf46b4f4d4daaae606f4a605f06b5";
    private static final String DOWNLOAD_MANAGER_FRIENDLY_NAME = "Test Download Manager";
    
    @Before
    public void setup() throws MalformedURLException {
        webServerRegisterUrl = new URL("http://localhost:8080/download-manager-mock-web-server/register");
        
        NgeoWebServerRequestBuilder builder = mock(NgeoWebServerRequestBuilder.class);
        NgeoWebServerServiceInterface service = mock(NgeoWebServerServiceInterface.class);
        NgeoWebServerResponseParser parser = mock(NgeoWebServerResponseParser.class);
        
        ngeoWebServerServiceHelper = new NgeoWebServerServiceHelper(builder, service, parser);
    }
    
    @Test
    public void testRegister() throws MalformedURLException, ServiceException, ParseException {
        dmRegistration = new DMRegistration(webServerRegisterUrl, DOWNLOAD_MANAGER_ID, DOWNLOAD_MANAGER_FRIENDLY_NAME, ngeoWebServerServiceHelper);
        
        DMRegistrationMgmntRequ registrationMgmntRequest = new DMRegistrationMgmntRequ();
        registrationMgmntRequest.setDownloadManagerId(DOWNLOAD_MANAGER_ID);
        registrationMgmntRequest.setDownloadManagerFriendlyName(DOWNLOAD_MANAGER_FRIENDLY_NAME);
        
        when(ngeoWebServerServiceHelper.getRequestBuilder().buildDMRegistrationMgmntRequest(DOWNLOAD_MANAGER_ID, DOWNLOAD_MANAGER_FRIENDLY_NAME)).thenReturn(registrationMgmntRequest);
        
        UmSsoHttpRequestAndResponse webServerRequestAndResponse = mock(UmSsoHttpRequestAndResponse.class);
        UmssoHttpResponse umssoHttpResponse = mock(UmssoHttpResponse.class);
        when(webServerRequestAndResponse.getResponse()).thenReturn(umssoHttpResponse);
        
        when(ngeoWebServerServiceHelper.getService().registrationMgmt(webServerRegisterUrl, registrationMgmntRequest)).thenReturn(webServerRequestAndResponse);

        DMRegistrationMgmntResp registrationMgmtResponse = new DMRegistrationMgmntResp();
        registrationMgmtResponse.setMonitoringServiceUrl(WEB_SERVER_MONITORING_URL);
        
        when(ngeoWebServerServiceHelper.getResponseParser().parseDMRegistrationMgmntResponse(webServerRegisterUrl, umssoHttpResponse)).thenReturn(registrationMgmtResponse);
        assertEquals(WEB_SERVER_MONITORING_URL, dmRegistration.register());
    }
    
    @Test
    public void testRegisterServiceException() throws ServiceException {
        dmRegistration = new DMRegistration(webServerRegisterUrl, DOWNLOAD_MANAGER_ID, DOWNLOAD_MANAGER_FRIENDLY_NAME, ngeoWebServerServiceHelper);
        
        DMRegistrationMgmntRequ registrationMgmntRequest = new DMRegistrationMgmntRequ();
        registrationMgmntRequest.setDownloadManagerId(DOWNLOAD_MANAGER_ID);
        registrationMgmntRequest.setDownloadManagerFriendlyName(DOWNLOAD_MANAGER_FRIENDLY_NAME);
        
        when(ngeoWebServerServiceHelper.getRequestBuilder().buildDMRegistrationMgmntRequest(DOWNLOAD_MANAGER_ID, DOWNLOAD_MANAGER_FRIENDLY_NAME)).thenReturn(registrationMgmntRequest);
        
        ServiceException serviceException = new ServiceException("Test error");
        when(ngeoWebServerServiceHelper.getService().registrationMgmt(webServerRegisterUrl, registrationMgmntRequest)).thenThrow(serviceException);

        try {
            dmRegistration.register();
        }catch(WebServerServiceException ex) {
            assertEquals(String.format("Exception occurred whilst attempting to register the Download Manager: %s", serviceException.getLocalizedMessage()), ex.getLocalizedMessage());
        }
    }

    @Test
    public void testRegisterParseException() throws ParseException, ServiceException {
        dmRegistration = new DMRegistration(webServerRegisterUrl, DOWNLOAD_MANAGER_ID, DOWNLOAD_MANAGER_FRIENDLY_NAME, ngeoWebServerServiceHelper);
        
        DMRegistrationMgmntRequ registrationMgmntRequest = new DMRegistrationMgmntRequ();
        registrationMgmntRequest.setDownloadManagerId(DOWNLOAD_MANAGER_ID);
        registrationMgmntRequest.setDownloadManagerFriendlyName(DOWNLOAD_MANAGER_FRIENDLY_NAME);
        
        when(ngeoWebServerServiceHelper.getRequestBuilder().buildDMRegistrationMgmntRequest(DOWNLOAD_MANAGER_ID, DOWNLOAD_MANAGER_FRIENDLY_NAME)).thenReturn(registrationMgmntRequest);
        
        UmSsoHttpRequestAndResponse webServerRequestAndResponse = mock(UmSsoHttpRequestAndResponse.class);
        UmssoHttpResponse umssoHttpResponse = mock(UmssoHttpResponse.class);
        when(webServerRequestAndResponse.getResponse()).thenReturn(umssoHttpResponse);
        
        when(ngeoWebServerServiceHelper.getService().registrationMgmt(webServerRegisterUrl, registrationMgmntRequest)).thenReturn(webServerRequestAndResponse);

        ParseException parseException = new ParseException("Test error");
        when(ngeoWebServerServiceHelper.getResponseParser().parseDMRegistrationMgmntResponse(webServerRegisterUrl, umssoHttpResponse)).thenThrow(parseException);

        try {
            dmRegistration.register();
        }catch(WebServerServiceException ex) {
            assertEquals(String.format("Exception occurred whilst attempting to register the Download Manager: %s", parseException.getLocalizedMessage()), ex.getLocalizedMessage());
        }
    }
}
