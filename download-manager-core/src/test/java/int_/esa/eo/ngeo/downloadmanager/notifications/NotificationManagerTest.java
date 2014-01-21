package int_.esa.eo.ngeo.downloadmanager.notifications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.configuration.DownloadManagerProperties;
import int_.esa.eo.ngeo.downloadmanager.exception.NotificationException;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class NotificationManagerTest {
    private NotificationManager notificationManager;
    private SettingsManager settingsManager;
    private EmailSender emailSender;
    private SMTPServerDetailsBuilder smtpServerDetailsBuilder;
    private DownloadManagerProperties downloadManagerProperties;
    
    private String recipientsAsString, subject, message, smtpServer, port, username, password;
    private EmailSecurity emailSecurity;
    
    private SMTPServerDetails smtpServerDetails;
    
    @Before
    public void setup() {
        settingsManager = mock(SettingsManager.class);
        emailSender = mock(EmailSender.class);
        smtpServerDetailsBuilder = mock(SMTPServerDetailsBuilder.class);
        downloadManagerProperties = mock(DownloadManagerProperties.class);
        notificationManager = new NotificationManager(settingsManager, emailSender, smtpServerDetailsBuilder, downloadManagerProperties);
        
        recipientsAsString = "test@download-manager.eo.esa.int";
        subject = "Web Server Communication error";
        message = "Unable communicate with Web Server.";
        smtpServer = "smtp.test";
        port = "567";
        username = "myUsername";
        password = "myPassword";
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_RECIPIENTS)).thenReturn(recipientsAsString);
        emailSecurity = EmailSecurity.SSL;
        
        smtpServerDetails = new SMTPServerDetails(smtpServer, port, username, password, emailSecurity);
        when(smtpServerDetailsBuilder.createSMTPDetailsFromSettings(settingsManager)).thenReturn(smtpServerDetails);

    }
    
    @Test
    public void sendEmailNotificationEqualLevelTest() throws NotificationException, MessagingException {
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SEND_NOTIFICATION_LEVEL)).thenReturn(NotificationLevel.ERROR.name());
        
        notificationManager.sendNotification(NotificationLevel.ERROR, subject, message, NotificationOutput.EMAIL);
        
        Mockito.verify(emailSender, times(1)).postMail(recipientsAsString.split(","), subject, message, smtpServerDetails);
    }

    @Test
    public void sendEmailNotificationHigherLevelTest() throws NotificationException, MessagingException {
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SEND_NOTIFICATION_LEVEL)).thenReturn(NotificationLevel.FATAL.name());
        
        notificationManager.sendNotification(NotificationLevel.ERROR, subject, message, NotificationOutput.EMAIL);
        
        Mockito.verify(emailSender, times(0)).postMail(recipientsAsString.split(","), subject, message, smtpServerDetails);
    }

    @Test
    public void sendEmailNotificationLowerLevelTest() throws NotificationException, MessagingException {
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SEND_NOTIFICATION_LEVEL)).thenReturn(NotificationLevel.WARNING.name());
        
        notificationManager.sendNotification(NotificationLevel.ERROR, subject, message, NotificationOutput.EMAIL);
        
        Mockito.verify(emailSender, times(1)).postMail(recipientsAsString.split(","), subject, message, smtpServerDetails);
    }

    @Test
    public void sendEmailNotificationNullLevelTest() throws NotificationException, MessagingException {
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SEND_NOTIFICATION_LEVEL)).thenReturn(NotificationLevel.WARNING.name());
        
        notificationManager.sendNotification(null, subject, message, NotificationOutput.EMAIL);
        
        Mockito.verify(emailSender, times(0)).postMail(recipientsAsString.split(","), subject, message, smtpServerDetails);
    }

    @Test
    public void sendEmailNotificationNullSettingsLevelTest() throws NotificationException, MessagingException {
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SEND_NOTIFICATION_LEVEL)).thenReturn(null);
        
        notificationManager.sendNotification(NotificationLevel.ERROR, subject, message, NotificationOutput.EMAIL);
        
        Mockito.verify(emailSender, times(0)).postMail(recipientsAsString.split(","), subject, message, smtpServerDetails);
    }

    @Test
    public void sendEmailNotificationWebUITest() throws NotificationException, MessagingException {
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SEND_NOTIFICATION_LEVEL)).thenReturn(NotificationLevel.ERROR.name());
        
        notificationManager.sendNotification(NotificationLevel.ERROR, subject, message, NotificationOutput.WEB_UI);
        
        Mockito.verify(emailSender, times(0)).postMail(recipientsAsString.split(","), subject, message, smtpServerDetails);
    }

    @Test
    public void sendEmailNotificationEmailExceptionTest() throws MessagingException {
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SEND_NOTIFICATION_LEVEL)).thenReturn(NotificationLevel.ERROR.name());
        Mockito.doThrow(new MessagingException("message error")).when(emailSender).postMail(recipientsAsString.split(","), subject, message, smtpServerDetails);
        
        try {
            notificationManager.sendNotification(NotificationLevel.ERROR, subject, message, NotificationOutput.EMAIL);
            fail("sending an email should throw an exception in this test");
        }catch(NotificationException ex) {
            assertEquals("Unable to send email.", ex.getMessage());
            assertEquals("message error", ex.getCause().getMessage());
        }
    }
    
    @Test
    public void sendEmailNotificationNoRecipientsTest() throws MessagingException, NotificationException {
        this.recipientsAsString = "";
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_RECIPIENTS)).thenReturn(recipientsAsString);

        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SEND_NOTIFICATION_LEVEL)).thenReturn(NotificationLevel.ERROR.name());
        
        notificationManager.sendNotification(NotificationLevel.ERROR, subject, message, NotificationOutput.EMAIL);
        
        Mockito.verify(emailSender, times(0)).postMail(recipientsAsString.split(","), subject, message, smtpServerDetails);
    }


    @Test
    public void sendEmailNotificationMultipleOutputsTest() throws NotificationException, MessagingException {
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SEND_NOTIFICATION_LEVEL)).thenReturn(NotificationLevel.ERROR.name());
        List<NotificationOutput> notificationOutputList = new ArrayList<>();
        notificationOutputList.add(NotificationOutput.EMAIL);
        notificationOutputList.add(NotificationOutput.WEB_UI);
        
        notificationManager.sendNotification(NotificationLevel.ERROR, subject, message, notificationOutputList);
        
        Mockito.verify(emailSender, times(1)).postMail(recipientsAsString.split(","), subject, message, smtpServerDetails);
    }

    @Test
    public void sendEmailNotificationNoOutputsTest() throws NotificationException, MessagingException {
        when(settingsManager.getSetting(UserModifiableSetting.EMAIL_SEND_NOTIFICATION_LEVEL)).thenReturn(NotificationLevel.ERROR.name());
        List<NotificationOutput> notificationOutputList = new ArrayList<>();
        
        notificationManager.sendNotification(NotificationLevel.ERROR, subject, message, notificationOutputList);
        
        Mockito.verify(emailSender, times(0)).postMail(recipientsAsString.split(","), subject, message, smtpServerDetails);
    }
}

