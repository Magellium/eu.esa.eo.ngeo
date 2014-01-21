package int_.esa.eo.ngeo.dmtu.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import int_.esa.eo.ngeo.downloadmanager.configuration.DownloadManagerProperties;
import int_.esa.eo.ngeo.downloadmanager.exception.NotificationException;
import int_.esa.eo.ngeo.downloadmanager.notifications.NotificationLevel;
import int_.esa.eo.ngeo.downloadmanager.notifications.NotificationManager;
import int_.esa.eo.ngeo.downloadmanager.notifications.NotificationOutput;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigControllerTest {
    @InjectMocks private ConfigController configController = new ConfigController();
    @Mock NotificationManager notificationManager;
    @Mock SettingsManager settingsManager;
    @Mock DownloadManagerProperties downloadManagerProperties;
    
    @Test
    public void testSendOfEmailWhenCredentialsHaveChanged() throws NotificationException {
        when(notificationManager.getDownloadManagerProperties()).thenReturn(downloadManagerProperties);
        when(downloadManagerProperties.getDownloadManagerTitle()).thenReturn("DM Test");
        when(downloadManagerProperties.getDownloadManagerVersion()).thenReturn("1.0.0");
        
        when(settingsManager.getSetting(UserModifiableSetting.SSO_USERNAME)).thenReturn("username2");
        when(settingsManager.getSetting(UserModifiableSetting.SSO_PASSWORD)).thenReturn("password2");
        
        configController.sendNotificationOfUserCredentialsChange("username1", "password1");
        
        verify(notificationManager).sendNotification(any(NotificationLevel.class), any(String.class), any(String.class), any(NotificationOutput.class));
    }

    @Test
    public void testSendOfEmailWhenCredentialsHaveNotChanged() throws NotificationException {
        when(notificationManager.getDownloadManagerProperties()).thenReturn(downloadManagerProperties);
        when(downloadManagerProperties.getDownloadManagerTitle()).thenReturn("DM Test");
        when(downloadManagerProperties.getDownloadManagerVersion()).thenReturn("1.0.0");
        
        when(settingsManager.getSetting(UserModifiableSetting.SSO_USERNAME)).thenReturn("username1");
        when(settingsManager.getSetting(UserModifiableSetting.SSO_PASSWORD)).thenReturn("password1");
        
        configController.sendNotificationOfUserCredentialsChange("username1", "password1");
        
        verify(notificationManager, times(0)).sendNotification(any(NotificationLevel.class), any(String.class), any(String.class), any(NotificationOutput.class));
    }
}
