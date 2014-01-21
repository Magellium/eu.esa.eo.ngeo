package int_.esa.eo.ngeo.downloadmanager.notifications;


import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
    public void postMail(String[] recipients, String subject, String message, SMTPServerDetails smtpServerDetails) throws MessagingException {
        boolean debug = false; 

        // Set the host smtp address
        Properties props = smtpServerDetails.getDetailsAsMailProperties();

        Authenticator auth = new SMTPAuthenticator(smtpServerDetails.getUsername(), smtpServerDetails.getPassword());
        Session session = Session.getDefaultInstance(props, auth);
        session.setDebug(debug);

        // create a message
        Message msg = new MimeMessage(session); 

        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addressTo[i] = new InternetAddress(recipients[i]);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);

        // Setting the Subject and Content Type
        msg.setSubject(subject);
        msg.setContent(message, "text/plain");
        Transport.send(msg); 
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        private String user;
        private String password;
        
        public SMTPAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }
        
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, password);
        }
    }
}
