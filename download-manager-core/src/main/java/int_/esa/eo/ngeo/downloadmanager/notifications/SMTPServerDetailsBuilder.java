package int_.esa.eo.ngeo.downloadmanager.notifications;

import org.apache.commons.lang.StringUtils;

import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

public class SMTPServerDetailsBuilder {
    public SMTPServerDetails createSMTPDetailsFromSettings(SettingsManager settingsManager) {
        String smtpServer = settingsManager.getSetting(UserModifiableSetting.EMAIL_SMTP_SERVER);
        String port = settingsManager.getSetting(UserModifiableSetting.EMAIL_SMTP_PORT);
        String username = settingsManager.getSetting(UserModifiableSetting.EMAIL_SMTP_USERNAME);
        String password = settingsManager.getSetting(UserModifiableSetting.EMAIL_SMTP_PASSWORD);
        EmailSecurity emailSecurity = EmailSecurity.valueOf(settingsManager.getSetting(UserModifiableSetting.EMAIL_SMTP_SECURITY_TYPE));

        if(StringUtils.isNotEmpty(smtpServer) && StringUtils.isNotEmpty(port) && StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password) && emailSecurity != null) {
            return new SMTPServerDetails(smtpServer, port, username, password, emailSecurity);
        }else{
            return null;
        }
    }
}
