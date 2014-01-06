package int_.esa.eo.ngeo.downloadmanager.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.siemens.pse.umsso.client.UmssoCLEnvironment;

public class UmSsoHttpConnectionSettingsTest {
    private UmSsoHttpConnectionSettings umSsoHttpConnectionSettings;
    
    @Test
    public void ConstructorTestUmSsoDetailsOnly() {
        String umSsoUsername = "username";
        String umSsoPassword = "password";
        umSsoHttpConnectionSettings = new UmSsoHttpConnectionSettings(umSsoUsername, umSsoPassword);
        
        assertEquals(umSsoUsername, umSsoHttpConnectionSettings.getUmssoUsername());
        assertEquals(umSsoPassword, umSsoHttpConnectionSettings.getUmssoPassword());
        assertEquals("", umSsoHttpConnectionSettings.getProxyHost());
        assertEquals(-1, umSsoHttpConnectionSettings.getProxyPort());
        assertEquals("", umSsoHttpConnectionSettings.getProxyUser());
        assertEquals("", umSsoHttpConnectionSettings.getProxyPassword());

        assertNull(umSsoHttpConnectionSettings.getUmSsoCLEnvironmentFromProxySettings());
        
        String toStringChecker = String.format("umSsoUserName %s%n umSsoPassword %s%n proxyHost %s%n proxyPort %s%n proxyUser %s%n proxyPassword %s", umSsoUsername, umSsoPassword, "", -1, "", "");
        assertEquals(toStringChecker, umSsoHttpConnectionSettings.toString());
    }

    @Test
    public void ConstructorTestUmSsoDetailsProxyHostAndPortOnly() {
        String umSsoUsername = "username";
        String umSsoPassword = "password";
        String proxyHost = "localhost";
        int proxyPort = 8888;
        umSsoHttpConnectionSettings = new UmSsoHttpConnectionSettings(umSsoUsername, umSsoPassword, proxyHost, proxyPort);
        
        assertEquals(umSsoUsername, umSsoHttpConnectionSettings.getUmssoUsername());
        assertEquals(umSsoPassword, umSsoHttpConnectionSettings.getUmssoPassword());
        assertEquals(proxyHost, umSsoHttpConnectionSettings.getProxyHost());
        assertEquals(proxyPort, umSsoHttpConnectionSettings.getProxyPort());
        assertEquals("", umSsoHttpConnectionSettings.getProxyUser());
        assertEquals("", umSsoHttpConnectionSettings.getProxyPassword());

        UmssoCLEnvironment umSsoCLEnvironmentFromProxySettings = umSsoHttpConnectionSettings.getUmSsoCLEnvironmentFromProxySettings();
        assertEquals(proxyHost, umSsoCLEnvironmentFromProxySettings.getProxyHost());
        assertEquals(proxyPort, umSsoCLEnvironmentFromProxySettings.getProxyPort());
        assertNull(umSsoCLEnvironmentFromProxySettings.getProxyUserName());
        assertNull(umSsoCLEnvironmentFromProxySettings.getProxyUserPasswd());
        
        String toStringChecker = String.format("umSsoUserName %s%n umSsoPassword %s%n proxyHost %s%n proxyPort %s%n proxyUser %s%n proxyPassword %s", umSsoUsername, umSsoPassword, proxyHost, proxyPort, "", "");
        assertEquals(toStringChecker, umSsoHttpConnectionSettings.toString());
    }

    @Test
    public void ConstructorTestFullDetails() {
        String umSsoUsername = "username";
        String umSsoPassword = "password";
        String proxyHost = "localhost";
        int proxyPort = 8888;
        String proxyUsername = "ngeo_username";
        String proxyPassword = "ngeo_password";
        umSsoHttpConnectionSettings = new UmSsoHttpConnectionSettings(umSsoUsername, umSsoPassword, proxyHost, proxyPort, proxyUsername, proxyPassword);
        
        assertEquals(umSsoUsername, umSsoHttpConnectionSettings.getUmssoUsername());
        assertEquals(umSsoPassword, umSsoHttpConnectionSettings.getUmssoPassword());
        assertEquals(proxyHost, umSsoHttpConnectionSettings.getProxyHost());
        assertEquals(proxyPort, umSsoHttpConnectionSettings.getProxyPort());
        assertEquals(proxyUsername, umSsoHttpConnectionSettings.getProxyUser());
        assertEquals(proxyPassword, umSsoHttpConnectionSettings.getProxyPassword());

        UmssoCLEnvironment umSsoCLEnvironmentFromProxySettings = umSsoHttpConnectionSettings.getUmSsoCLEnvironmentFromProxySettings();
        assertEquals(proxyHost, umSsoCLEnvironmentFromProxySettings.getProxyHost());
        assertEquals(proxyPort, umSsoCLEnvironmentFromProxySettings.getProxyPort());
        assertEquals(proxyUsername, umSsoCLEnvironmentFromProxySettings.getProxyUserName());
        assertEquals(proxyPassword, umSsoCLEnvironmentFromProxySettings.getProxyUserPasswd());
        
        String toStringChecker = String.format("umSsoUserName %s%n umSsoPassword %s%n proxyHost %s%n proxyPort %s%n proxyUser %s%n proxyPassword %s", umSsoUsername, umSsoPassword, proxyHost, proxyPort, proxyUsername, proxyPassword);
        assertEquals(toStringChecker, umSsoHttpConnectionSettings.toString());
    }
}
