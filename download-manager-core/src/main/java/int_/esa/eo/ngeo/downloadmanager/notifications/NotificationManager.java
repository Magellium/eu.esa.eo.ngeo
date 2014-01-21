package int_.esa.eo.ngeo.downloadmanager.notifications;

import int_.esa.eo.ngeo.downloadmanager.configuration.DownloadManagerProperties;
import int_.esa.eo.ngeo.downloadmanager.exception.NotificationException;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;

public class NotificationManager {
    private final SettingsManager settingsManager;
    private final EmailSender emailSender;
    private final SMTPServerDetailsBuilder smtpServerDetailsBuilder;
    private final DownloadManagerProperties downloadManagerProperties;
    private final ProductionTerminationEmailFormatter productionTerminationEmailFormatter;

    public NotificationManager(SettingsManager settingsManager, EmailSender emailSender, SMTPServerDetailsBuilder smtpServerDetailsBuilder, DownloadManagerProperties downloadManagerProperties) {
        this.settingsManager = settingsManager;
        this.emailSender = emailSender;
        this.smtpServerDetailsBuilder = smtpServerDetailsBuilder;
        this.downloadManagerProperties = downloadManagerProperties;
        
        this.productionTerminationEmailFormatter = new ProductionTerminationEmailFormatter(downloadManagerProperties);
    }

    public void sendNotification(NotificationLevel notificationLevel, String title, String message, List<NotificationOutput> outputTypes) throws NotificationException {
        for (NotificationOutput notificationOutput : outputTypes) {
            sendNotification(notificationLevel, title, message, notificationOutput);
        }
    }

    public void sendNotification(NotificationLevel notificationLevel, String title, String message, NotificationOutput outputType) throws NotificationException {
        String emailSendNotificationLevel = settingsManager.getSetting(UserModifiableSetting.EMAIL_SEND_NOTIFICATION_LEVEL);
        if(notificationLevel != null && emailSendNotificationLevel != null) {
            NotificationLevel notificationLevelFromSettings = NotificationLevel.valueOf(emailSendNotificationLevel);
            if(notificationLevel.getNotificationLevelValue() <= notificationLevelFromSettings.getNotificationLevelValue()) {
                switch (outputType) {
                case EMAIL:
                    sendEmail(title, message);
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    public void sendProductTerminationNotification(Product product) throws NotificationException {
        NotificationLevel notificationLevel = productionTerminationEmailFormatter.getNotificationLevelForProduct(product);
        String title = productionTerminationEmailFormatter.getEmailTitleForProduct(product);
        String message = productionTerminationEmailFormatter.getEmailMessageForProduct(product);
        
        sendNotification(notificationLevel, title, message, NotificationOutput.EMAIL);
    }

    private void sendEmail(String title, String message)
            throws NotificationException {
        SMTPServerDetails smtpServerDetails = smtpServerDetailsBuilder.createSMTPDetailsFromSettings(settingsManager);
        String recipientsAsString = settingsManager.getSetting(UserModifiableSetting.EMAIL_RECIPIENTS);
        if(smtpServerDetails == null || StringUtils.isNotEmpty(recipientsAsString)) {
            String[] recipients = recipientsAsString.split(",");

            try {
                emailSender.postMail(recipients, title, message, smtpServerDetails);
            }catch(MessagingException ex) {
                throw new NotificationException("Unable to send email.", ex);
            }
        }
    }

    public DownloadManagerProperties getDownloadManagerProperties() {
        return downloadManagerProperties;
    }
}
