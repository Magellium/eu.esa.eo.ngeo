package int_.esa.eo.ngeo.downloadmanager.configuration;

import int_.esa.eo.ngeo.downloadmanager.notifications.EmailSecurity;
import int_.esa.eo.ngeo.downloadmanager.notifications.NotificationLevel;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ConfigSettings {
    @Size(min=1, max=40, message="SSO username must be 1 - 40 characters long")
    @Pattern(regexp="^[a-zA-Z0-9]+$", message="SSO username must be alphanumeric with no spaces")
    private String ssoUsername;

    @Size(min=4, max=40, message="SSO password must be 4 - 40 characters long")
    private String ssoPassword;

    @Size(min=3, max=100, message="Name of Download Manager instance must be 3 - 100 characters long")
    @Pattern(regexp="^[^ ].*$", message="Name of Download Manager instance must not start with a space")
    private String dmFriendlyName;

    @Size(min=1, message="Download directory name must be at least one character long")
    private String baseDownloadFolder;

    @Size(min=1, max=5, message="Download Manager web interface port no. must be 1 - 5 numeric digits")
    @Pattern(regexp="^[0-9]+$", message="Download Manager web interface port no. must be 1 - 5 numeric digits")
    private String webInterfacePortNo;

    private String webProxyHost;

    @Size(min=0, max=5, message="Web proxy port no. must be 1 - 5 numeric digits")
    @Pattern(regexp="^$|[0-9]+$", message="Web proxy port no. must be 1 - 5 numeric digits")
    private String webProxyPort;

    @Size(min=0, max=40, message="Web proxy username must be no longer than 40 characters")
    @Pattern(regexp="^$|[a-zA-Z0-9]+$", message="Web proxy username must be alphanumeric with no spaces")
    private String webProxyUsername;

    @Size(min=0, max=40, message="Web proxy password must be no longer than 40 characters")
    private String webProxyPassword;

    private String emailRecipients;

    private NotificationLevel emailSendLevel;

    @Size(min=0, max=40, message="SMTP server address must be a maximum of 40 characters long")
    private String smtpServer;

    @Size(min=0, max=5, message="SMTP port no. must be a maximum of 5 numeric digits")
    @Pattern(regexp="^$|[0-9]+$", message="SMTP port no. must be a maximum of 5 numeric digits")
    private String smtpPort;

    @Size(min=0, max=40, message="SMTP username must be a maximum of 40 characters long")
    private String smtpUsername;

    @Size(min=0, max=40, message="SMTP password must be a maximum of 40 characters long")
    private String smtpPassword;

    private EmailSecurity smtpSecurity;

    public String getSsoUsername() {
        return ssoUsername;
    }
    public void setSsoUsername(String ssoUsername) {
        this.ssoUsername = ssoUsername;
    }
    public String getSsoPassword() {
        return ssoPassword;
    }
    public void setSsoPassword(String ssoPassword) {
        this.ssoPassword = ssoPassword;
    }
    public String getDmFriendlyName() {
        return dmFriendlyName;
    }
    public void setDmFriendlyName(String dmFriendlyName) {
        this.dmFriendlyName = dmFriendlyName;
    }
    public String getBaseDownloadFolder() {
        return baseDownloadFolder;
    }
    public void setBaseDownloadFolder(String baseDownloadFolder) {
        this.baseDownloadFolder = baseDownloadFolder;
    }
    public String getWebInterfacePortNo() {
        return webInterfacePortNo;
    }
    public void setWebInterfacePortNo(String webInterfacePortNo) {
        this.webInterfacePortNo = webInterfacePortNo;
    }

    public String getWebProxyHost() {
        return webProxyHost;
    }

    public void setWebProxyHost(String webProxyHost) {
        this.webProxyHost = webProxyHost;
    }

    public String getWebProxyPort() {
        return webProxyPort;
    }

    public void setWebProxyPort(String webProxyPort) {
        this.webProxyPort = webProxyPort;
    }

    public String getWebProxyUsername() {
        return webProxyUsername;
    }

    public void setWebProxyUsername(String webProxyUsername) {
        this.webProxyUsername = webProxyUsername;
    }

    public String getWebProxyPassword() {
        return webProxyPassword;
    }

    public void setWebProxyPassword(String webProxyPassword) {
        this.webProxyPassword = webProxyPassword;
    }
    public String getEmailRecipients() {
        return emailRecipients;
    }
    public void setEmailRecipients(String emailRecipients) {
        this.emailRecipients = emailRecipients;
    }
    public NotificationLevel getEmailSendLevel() {
        return emailSendLevel;
    }
    public void setEmailSendLevel(NotificationLevel emailSendLevel) {
        this.emailSendLevel = emailSendLevel;
    }
    public String getSmtpServer() {
        return smtpServer;
    }
    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }
    public String getSmtpPort() {
        return smtpPort;
    }
    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }
    public String getSmtpUsername() {
        return smtpUsername;
    }
    public void setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
    }
    public String getSmtpPassword() {
        return smtpPassword;
    }
    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }
    public EmailSecurity getSmtpSecurity() {
        return smtpSecurity;
    }
    public void setSmtpSecurity(EmailSecurity smtpSecurity) {
        this.smtpSecurity = smtpSecurity;
    }
}