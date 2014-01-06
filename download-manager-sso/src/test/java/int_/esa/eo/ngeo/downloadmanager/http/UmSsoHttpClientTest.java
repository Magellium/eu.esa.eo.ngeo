package int_.esa.eo.ngeo.downloadmanager.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UmSsoHttpClientTest {
    private UmSsoHttpClient umSsoHttpClient;
    
    @Test
    public void ConstructorTest() {
        UmSsoHttpConnectionSettings umSsoHttpConnectionSettings = new UmSsoHttpConnectionSettings("username", "password", "localhost", 8888, "ngeo", "ngeo");

        umSsoHttpClient = new UmSsoHttpClient(umSsoHttpConnectionSettings);
        
        assertEquals(umSsoHttpConnectionSettings, umSsoHttpClient.getUmSsoHttpConnectionSettings());
        
        UmSsoHttpConnectionSettings updatedUmSsoHttpConnectionSettings = new UmSsoHttpConnectionSettings("updated username", "updated password", "external_proxy_host", 1234, "myUsername", "myPassword");

        umSsoHttpClient.setUmSsoHttpConnectionSettings(updatedUmSsoHttpConnectionSettings);
        assertEquals(updatedUmSsoHttpConnectionSettings, umSsoHttpClient.getUmSsoHttpConnectionSettings());
    }
}
