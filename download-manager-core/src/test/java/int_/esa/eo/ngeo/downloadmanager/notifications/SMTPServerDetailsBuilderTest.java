package int_.esa.eo.ngeo.downloadmanager.notifications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

import org.junit.Before;
import org.junit.Test;

public class SMTPServerDetailsBuilderTest {
    private SMTPServerDetailsBuilder smtpServerDetailsBuilder;
    private SettingsManager settingsManager;
    
    String smtpServer = "smtp.test";
    String port = "567";
    String username = "myUsername";
    String password = "myPassword";

    @Before
    public void setup() {
        smtpServerDetailsBuilder = new SMTPServerDetailsBuilder();
        settingsManager = mock(SettingsManager.class);

        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SMTP_SERVER)).thenReturn(smtpServer);
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SMTP_PORT)).thenReturn(port);
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SMTP_USERNAME)).thenReturn(username);
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SMTP_PASSWORD)).thenReturn(password);
    }
    
    @Test
    public void createSMTPDetailsFromSettingsNoSecurityTest() {
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SMTP_SECURITY_TYPE)).thenReturn(EmailSecurity.NONE.name());

        SMTPServerDetails smtpServerDetails = smtpServerDetailsBuilder.createSMTPDetailsFromSettings(settingsManager);
        
        assertEquals(smtpServer, smtpServerDetails.getSmtpServer());
        assertEquals(port, smtpServerDetails.getPort());
        assertEquals(username, smtpServerDetails.getUsername());
        assertEquals(password, smtpServerDetails.getPassword());
        assertEquals(EmailSecurity.NONE, smtpServerDetails.getEmailSecurity());
        
        Properties detailsAsMailProperties = smtpServerDetails.getDetailsAsMailProperties();
        assertEquals(smtpServer, detailsAsMailProperties.getProperty("mail.smtp.host"));
        assertEquals(port, detailsAsMailProperties.getProperty("mail.smtp.port"));
        assertEquals(username, detailsAsMailProperties.getProperty("mail.smtp.user"));
        assertEquals(password, detailsAsMailProperties.getProperty("mail.smtp.password"));
        assertFalse(Boolean.parseBoolean(detailsAsMailProperties.getProperty("mail.smtp.auth")));
        assertNull(detailsAsMailProperties.getProperty("mail.smtp.ssl.enable"));
        assertNull(detailsAsMailProperties.getProperty("mail.smtp.starttls.enable"));
    }

    @Test
    public void createSMTPDetailsFromSettingsSSLSecurityTest() {
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SMTP_SECURITY_TYPE)).thenReturn(EmailSecurity.SSL.name());

        SMTPServerDetails smtpServerDetails = smtpServerDetailsBuilder.createSMTPDetailsFromSettings(settingsManager);
        
        assertEquals(EmailSecurity.SSL, smtpServerDetails.getEmailSecurity());
        
        Properties detailsAsMailProperties = smtpServerDetails.getDetailsAsMailProperties();
        assertTrue(Boolean.parseBoolean(detailsAsMailProperties.getProperty("mail.smtp.auth")));
        assertTrue(Boolean.parseBoolean(detailsAsMailProperties.getProperty("mail.smtp.ssl.enable")));
    }

    @Test
    public void createSMTPDetailsFromSettingsTLSSecurityTest() {
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SMTP_SECURITY_TYPE)).thenReturn(EmailSecurity.TLS.name());

        SMTPServerDetails smtpServerDetails = smtpServerDetailsBuilder.createSMTPDetailsFromSettings(settingsManager);
        
        assertEquals(EmailSecurity.TLS, smtpServerDetails.getEmailSecurity());
        
        Properties detailsAsMailProperties = smtpServerDetails.getDetailsAsMailProperties();
        assertTrue(Boolean.parseBoolean(detailsAsMailProperties.getProperty("mail.smtp.auth")));
        assertTrue(Boolean.parseBoolean(detailsAsMailProperties.getProperty("mail.smtp.starttls.enable")));
    }
}
