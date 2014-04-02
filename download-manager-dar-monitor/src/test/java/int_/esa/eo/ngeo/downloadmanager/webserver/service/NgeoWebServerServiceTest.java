package int_.esa.eo.ngeo.downloadmanager.webserver.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.SchemaNotFoundException;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.http.ConnectionPropertiesSynchronizedUmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpRequestAndResponse;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.transform.XMLWithSchemaTransformer;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessSubsetting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Before;
import org.junit.Test;

import com.siemens.pse.umsso.client.UmssoCLException;
import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

public class NgeoWebServerServiceTest {
    private static final String DOWNLOAD_MANAGER_ID = "80dcf46b4f4d4daaae606f4a605f06b5";
    private static final String DOWNLOAD_MANAGER_FRIENDLY_NAME = "Test Download Manager";

    private URL webServerRegisterUrl, monitoringUrl, darMonitoringUrl;
    private NgeoWebServerService ngeoWebServerService;
    private XMLWithSchemaTransformer xmlWithSchemaTransformer;
    private SettingsManager settingsManager;
    private ConnectionPropertiesSynchronizedUmSsoHttpClient connectionPropertiesSynchronizedUmSsoHttpClient;    

    @Before
    public void setup() throws MalformedURLException {
        webServerRegisterUrl = new URL("http://localhost:8080/download-manager-mock-web-server/register");
        monitoringUrl = new URL("http://localhost:8080/download-manager-mock-web-server/monitor");
        darMonitoringUrl = new URL("http://localhost:8080/download-manager-mock-web-server/testDAR");
        
        xmlWithSchemaTransformer = mock(XMLWithSchemaTransformer.class);
        settingsManager = mock(SettingsManager.class);
        connectionPropertiesSynchronizedUmSsoHttpClient = mock(ConnectionPropertiesSynchronizedUmSsoHttpClient.class);
        
        ngeoWebServerService = new NgeoWebServerService(xmlWithSchemaTransformer, settingsManager, connectionPropertiesSynchronizedUmSsoHttpClient);
    }
    
    @Test
    public void registrationMgmtTest() throws ServiceException, ParseException, SchemaNotFoundException, IOException, UmssoCLException {
        DMRegistrationMgmntRequ registrationMgmntRequest = new DMRegistrationMgmntRequ();
        registrationMgmntRequest.setDownloadManagerId(DOWNLOAD_MANAGER_ID);
        registrationMgmntRequest.setDownloadManagerFriendlyName(DOWNLOAD_MANAGER_FRIENDLY_NAME);
        
        UmSsoHttpClient umSsoHttpClient = mock(UmSsoHttpClient.class);
        when(connectionPropertiesSynchronizedUmSsoHttpClient.getUmSsoHttpClient()).thenReturn(umSsoHttpClient);
        
        String responseString = "registration xml request";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(responseString.getBytes());
        
        when(xmlWithSchemaTransformer.serializeAndInferSchema(registrationMgmntRequest)).thenReturn(byteArrayOutputStream);

        UmssoHttpResponse response = mock(UmssoHttpResponse.class);
        when(response.getBody()).thenReturn("registration response".getBytes());
        UmSsoHttpRequestAndResponse requestAndResponse = new UmSsoHttpRequestAndResponse(mock(HttpRequestBase.class), response);
        when(umSsoHttpClient.executePostRequest(webServerRegisterUrl, byteArrayOutputStream, "application/xml", "application/xml")).thenReturn(requestAndResponse);
        
        
        UmSsoHttpRequestAndResponse umSsoHttpRequestAndResponse = ngeoWebServerService.registrationMgmt(webServerRegisterUrl, registrationMgmntRequest);
        assertNotNull(umSsoHttpRequestAndResponse);
        assertNotNull(umSsoHttpRequestAndResponse.getResponse());
        assertEquals("registration response", new String(umSsoHttpRequestAndResponse.getResponse().getBody()));
    }
    
    @Test
    public void monitoringUrlTest() throws ServiceException, ParseException, SchemaNotFoundException, IOException, UmssoCLException {
        MonitoringURLRequ monitoringUrlRequest = new MonitoringURLRequ();
        monitoringUrlRequest.setDownloadManagerId(DOWNLOAD_MANAGER_ID);
        
        UmSsoHttpClient umSsoHttpClient = mock(UmSsoHttpClient.class);
        when(connectionPropertiesSynchronizedUmSsoHttpClient.getUmSsoHttpClient()).thenReturn(umSsoHttpClient);
        
        String responseString = "monitoring request";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(responseString.getBytes());
        
        when(xmlWithSchemaTransformer.serializeAndInferSchema(monitoringUrlRequest)).thenReturn(byteArrayOutputStream);

        UmssoHttpResponse response = mock(UmssoHttpResponse.class);
        when(response.getBody()).thenReturn("monitoring response".getBytes());
        UmSsoHttpRequestAndResponse requestAndResponse = new UmSsoHttpRequestAndResponse(mock(HttpRequestBase.class), response);
        when(umSsoHttpClient.executePostRequest(monitoringUrl, byteArrayOutputStream, "application/xml", "application/xml")).thenReturn(requestAndResponse);
        
        
        UmSsoHttpRequestAndResponse umSsoHttpRequestAndResponse = ngeoWebServerService.monitoringURL(monitoringUrl, monitoringUrlRequest);
        assertNotNull(umSsoHttpRequestAndResponse);
        assertNotNull(umSsoHttpRequestAndResponse.getResponse());
        assertEquals("monitoring response", new String(umSsoHttpRequestAndResponse.getResponse().getBody()));
    }
    
    @Test
    public void dataAccessMonitoringTest() throws ServiceException, ParseException, SchemaNotFoundException, IOException, UmssoCLException {
        DataAccessMonitoringRequ dataAccessMonitoringRequest = new DataAccessMonitoringRequ();
        dataAccessMonitoringRequest.setDownloadManagerId(DOWNLOAD_MANAGER_ID);
        ProductAccessSubsetting productAccessSubsetting = new ProductAccessSubsetting();
        productAccessSubsetting.setReadyProductsOrAll(ProductAccessStatus.READY);
        dataAccessMonitoringRequest.setProductAccessSubsetting(productAccessSubsetting);
        
        UmSsoHttpClient umSsoHttpClient = mock(UmSsoHttpClient.class);
        when(connectionPropertiesSynchronizedUmSsoHttpClient.getUmSsoHttpClient()).thenReturn(umSsoHttpClient);
        
        String responseString = "dar monitoring request";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(responseString.getBytes());
        
        when(xmlWithSchemaTransformer.serializeAndInferSchema(dataAccessMonitoringRequest)).thenReturn(byteArrayOutputStream);

        UmssoHttpResponse response = mock(UmssoHttpResponse.class);
        when(response.getBody()).thenReturn("DAR monitoring response".getBytes());
        UmSsoHttpRequestAndResponse requestAndResponse = new UmSsoHttpRequestAndResponse(mock(HttpRequestBase.class), response);
        when(umSsoHttpClient.executePostRequest(darMonitoringUrl, byteArrayOutputStream, "application/xml", "application/xml")).thenReturn(requestAndResponse);
        
        
        UmSsoHttpRequestAndResponse umSsoHttpRequestAndResponse = ngeoWebServerService.dataAccessMonitoring(darMonitoringUrl, dataAccessMonitoringRequest);
        assertNotNull(umSsoHttpRequestAndResponse);
        assertNotNull(umSsoHttpRequestAndResponse.getResponse());
        assertEquals("DAR monitoring response", new String(umSsoHttpRequestAndResponse.getResponse().getBody()));
    }    

    @Test
    public void serviceExceptionTest() throws ParseException, SchemaNotFoundException, IOException, UmssoCLException {
        DMRegistrationMgmntRequ registrationMgmntRequest = new DMRegistrationMgmntRequ();
        registrationMgmntRequest.setDownloadManagerId(DOWNLOAD_MANAGER_ID);
        registrationMgmntRequest.setDownloadManagerFriendlyName(DOWNLOAD_MANAGER_FRIENDLY_NAME);
        
        UmSsoHttpClient umSsoHttpClient = mock(UmSsoHttpClient.class);
        when(connectionPropertiesSynchronizedUmSsoHttpClient.getUmSsoHttpClient()).thenReturn(umSsoHttpClient);
        
        String responseString = "registration xml request";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(responseString.getBytes());
        
        when(xmlWithSchemaTransformer.serializeAndInferSchema(registrationMgmntRequest)).thenReturn(byteArrayOutputStream);

        UmssoCLException umssoCLException = new UmssoCLException("Unable to send HTTP request");
        when(umSsoHttpClient.executePostRequest(webServerRegisterUrl, byteArrayOutputStream, "application/xml", "application/xml")).thenThrow(umssoCLException);
        
        try {
            ngeoWebServerService.registrationMgmt(webServerRegisterUrl, registrationMgmntRequest);
            fail("Registration call should throw a service exception.");
        } catch (ServiceException ex) {
            assertNotNull(ex.getCause());
            assertEquals("Unable to send HTTP request", ex.getCause().getLocalizedMessage());
        }
    }
    
    @Test
    public void nonUmssoCredentialsTest() throws ParseException, SchemaNotFoundException, MalformedURLException, UmssoCLException, IOException {
        String nonUmssoLoginUrl = "http://non-umsso-url.com/";
        when(settingsManager.getSetting(NonUserModifiableSetting.NGEO_WEB_SERVER_NON_UMSSO_LOGIN_URL)).thenReturn(nonUmssoLoginUrl);
        when(xmlWithSchemaTransformer.serializeAndInferSchema(any(DMRegistrationMgmntRequ.class))).thenThrow(new ParseException("throw away exception"));
        
        UmSsoHttpClient umSsoHttpClient = mock(UmSsoHttpClient.class);
        when(connectionPropertiesSynchronizedUmSsoHttpClient.getUmSsoHttpClient()).thenReturn(umSsoHttpClient);
        
        UmssoHttpResponse response = mock(UmssoHttpResponse.class);
        when(response.getBody()).thenReturn("non-um-sso registration response".getBytes());
        UmSsoHttpRequestAndResponse requestAndResponse = new UmSsoHttpRequestAndResponse(mock(HttpRequestBase.class), response);
        when(umSsoHttpClient.executeGetRequest(new URL("http://non-umsso-url.com/"))).thenReturn(requestAndResponse);

        try {
           ngeoWebServerService.registrationMgmt(webServerRegisterUrl, new DMRegistrationMgmntRequ());
           fail("Registration call should throw throw away service exception.");
        }catch(ServiceException ex) {
            assertNotNull(ex.getCause());
            assertEquals("throw away exception", ex.getCause().getLocalizedMessage());
        }
    }

    @Test
    public void nonUmssoCredentialsMalformedUrlTest() throws ParseException, SchemaNotFoundException, MalformedURLException, UmssoCLException, IOException {
        String nonUmssoLoginUrl = "This is a malformed URL";
        when(settingsManager.getSetting(NonUserModifiableSetting.NGEO_WEB_SERVER_NON_UMSSO_LOGIN_URL)).thenReturn(nonUmssoLoginUrl);

        try {
           ngeoWebServerService.registrationMgmt(webServerRegisterUrl, new DMRegistrationMgmntRequ());
           fail("Registration call should throw throw away service exception.");
        }catch(ServiceException ex) {
            assertNotNull(ex.getCause());
            assertEquals("no protocol: This is a malformed URL", ex.getCause().getLocalizedMessage());
        }
    }

    @Test
    public void nonUmssoCredentialsUmssoExceptionTest() throws ParseException, SchemaNotFoundException, MalformedURLException, UmssoCLException, IOException {
        String nonUmssoLoginUrl = "http://non-umsso-url.com/";
        when(settingsManager.getSetting(NonUserModifiableSetting.NGEO_WEB_SERVER_NON_UMSSO_LOGIN_URL)).thenReturn(nonUmssoLoginUrl);
        when(xmlWithSchemaTransformer.serializeAndInferSchema(any(DMRegistrationMgmntRequ.class))).thenThrow(new ParseException("throw away exception"));
        
        UmSsoHttpClient umSsoHttpClient = mock(UmSsoHttpClient.class);
        when(connectionPropertiesSynchronizedUmSsoHttpClient.getUmSsoHttpClient()).thenReturn(umSsoHttpClient);
        
        when(umSsoHttpClient.executeGetRequest(new URL("http://non-umsso-url.com/"))).thenThrow(new UmssoCLException("unable to send GET request."));
        
        try {
            ngeoWebServerService.registrationMgmt(webServerRegisterUrl, new DMRegistrationMgmntRequ());
            fail("Registration call should throw throw away service exception.");
        } catch (ServiceException ex) {
            assertNotNull(ex.getCause());
            assertEquals("throw away exception", ex.getCause().getLocalizedMessage());
        }

        try {
            ngeoWebServerService.registrationMgmt(webServerRegisterUrl, new DMRegistrationMgmntRequ());
            fail("Registration call should throw throw away service exception.");
        } catch (ServiceException ex) {
            assertNotNull(ex.getCause());
            assertEquals("throw away exception", ex.getCause().getLocalizedMessage());
        }
    }

    @Test
    public void nonUmssoCredentialsIOExceptionTest() throws ParseException, SchemaNotFoundException, MalformedURLException, UmssoCLException, IOException {
        String nonUmssoLoginUrl = "http://non-umsso-url.com/";
        when(settingsManager.getSetting(NonUserModifiableSetting.NGEO_WEB_SERVER_NON_UMSSO_LOGIN_URL)).thenReturn(nonUmssoLoginUrl);
        when(xmlWithSchemaTransformer.serializeAndInferSchema(any(DMRegistrationMgmntRequ.class))).thenThrow(new ParseException("throw away exception"));
        
        UmSsoHttpClient umSsoHttpClient = mock(UmSsoHttpClient.class);
        when(connectionPropertiesSynchronizedUmSsoHttpClient.getUmSsoHttpClient()).thenReturn(umSsoHttpClient);
        
        when(umSsoHttpClient.executeGetRequest(new URL("http://non-umsso-url.com/"))).thenThrow(new IOException("IO Exception when sending GET request"));
        
        try {
            ngeoWebServerService.registrationMgmt(webServerRegisterUrl, new DMRegistrationMgmntRequ());
            fail("Registration call should throw throw away service exception.");
        } catch (ServiceException ex) {
            assertNotNull(ex.getCause());
            assertEquals("throw away exception", ex.getCause().getLocalizedMessage());
        }

        try {
            ngeoWebServerService.registrationMgmt(webServerRegisterUrl, new DMRegistrationMgmntRequ());
            fail("Registration call should throw throw away service exception.");
        } catch (ServiceException ex) {
            assertNotNull(ex.getCause());
            assertEquals("throw away exception", ex.getCause().getLocalizedMessage());
        }
    }
}
