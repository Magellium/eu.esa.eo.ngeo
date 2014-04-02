package int_.esa.eo.ngeo.dmtu.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.configuration.DownloadManagerProperties;
import int_.esa.eo.ngeo.downloadmanager.exception.NotificationException;
import int_.esa.eo.ngeo.downloadmanager.notifications.NotificationManager;
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
    public void testWhenCredentialsHaveChanged() throws NotificationException {
        when(notificationManager.getDownloadManagerProperties()).thenReturn(downloadManagerProperties);
        when(downloadManagerProperties.getDownloadManagerTitle()).thenReturn("DM Test");
        when(downloadManagerProperties.getDownloadManagerVersion()).thenReturn("1.0.0");
        
        when(settingsManager.getSetting(UserModifiableSetting.SSO_USERNAME)).thenReturn("username2");
        when(settingsManager.getSetting(UserModifiableSetting.SSO_PASSWORD)).thenReturn("password2");
        
        assertTrue(configController.haveUserCredentialsChanged("username1", "password1"));
    }

    @Test
    public void testWhenCredentialsHaveNotChanged() throws NotificationException {
        when(notificationManager.getDownloadManagerProperties()).thenReturn(downloadManagerProperties);
        when(downloadManagerProperties.getDownloadManagerTitle()).thenReturn("DM Test");
        when(downloadManagerProperties.getDownloadManagerVersion()).thenReturn("1.0.0");
        
        when(settingsManager.getSetting(UserModifiableSetting.SSO_USERNAME)).thenReturn("username1");
        when(settingsManager.getSetting(UserModifiableSetting.SSO_PASSWORD)).thenReturn("password1");
        
        assertFalse(configController.haveUserCredentialsChanged("username1", "password1"));
    }
}
