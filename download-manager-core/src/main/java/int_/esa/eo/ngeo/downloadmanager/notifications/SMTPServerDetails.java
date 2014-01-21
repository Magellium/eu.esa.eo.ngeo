package int_.esa.eo.ngeo.downloadmanager.notifications;

import java.util.Properties;


public class SMTPServerDetails {
    private final String smtpServer, port, username, password;
    private final EmailSecurity emailSecurity;

    public SMTPServerDetails(String smtpServer, String port, String username, String password, EmailSecurity emailSecurity) {
        this.smtpServer = smtpServer;
        this.port = port;
        this.username = username;
        this.password = password;
        this.emailSecurity = emailSecurity;
    }

    public Properties getDetailsAsMailProperties() {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", smtpServer);
        props.setProperty("mail.smtp.port", port);
        props.setProperty("mail.smtp.user", username);
        props.setProperty("mail.smtp.password", password);

        boolean smtpAuth = false;
        switch (emailSecurity) {
        case SSL:
            smtpAuth = true;
            props.setProperty("mail.smtp.ssl.enable", "true");
            break;
        case TLS:
            smtpAuth = true;
            props.setProperty("mail.smtp.starttls.enable", "true");
            break;
        default:
            break;
        }
        props.setProperty("mail.smtp.auth", Boolean.toString(smtpAuth));

        return props;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public String getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public EmailSecurity getEmailSecurity() {
        return emailSecurity;
    }
}
