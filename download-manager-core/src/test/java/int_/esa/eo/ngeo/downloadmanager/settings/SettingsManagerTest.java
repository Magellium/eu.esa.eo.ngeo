package int_.esa.eo.ngeo.downloadmanager.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SettingsManagerTest {
	private SettingsManager settingsManager;
	
	@Before
	public void setup() {
		settingsManager = spy(new SettingsManager());
		Mockito.doNothing().when(settingsManager).updatePersistentStore(SettingsType.USER_MODIFIABLE);
		Mockito.doNothing().when(settingsManager).updatePersistentStore(SettingsType.NON_USER_MODIFIABLE);
	}
	
    @Test
    public void setUserModifiableSettingTest() {
        assertNull(settingsManager.getSetting(UserModifiableSetting.SSO_USERNAME));
        settingsManager.setUserModifiableSetting(UserModifiableSetting.SSO_USERNAME, "hello");
        assertEquals("hello", settingsManager.getSetting(UserModifiableSetting.SSO_USERNAME));
    }

    @Test
    public void setNonUserModifiableSettingTest() {
        assertNull(settingsManager.getSetting(NonUserModifiableSetting.DM_ID));
        settingsManager.setNonUserModifiableSetting(NonUserModifiableSetting.DM_ID, "hello");
        assertEquals("hello", settingsManager.getSetting(NonUserModifiableSetting.DM_ID));
    }

    @Test
    public void setUserModifiableSettingsTest() {
        assertNull(settingsManager.getSetting(UserModifiableSetting.SSO_USERNAME));
        assertNull(settingsManager.getSetting(UserModifiableSetting.SSO_PASSWORD));
        
        Map<UserModifiableSetting, String> userModifiableSettings = new HashMap<>();
        userModifiableSettings.put(UserModifiableSetting.SSO_USERNAME, "hello");
        userModifiableSettings.put(UserModifiableSetting.SSO_PASSWORD, "world");
        settingsManager.setUserModifiableSettings(userModifiableSettings);

        assertEquals("hello", settingsManager.getSetting(UserModifiableSetting.SSO_USERNAME));
        assertEquals("world", settingsManager.getSetting(UserModifiableSetting.SSO_PASSWORD));
    }

    @Test
    public void setNonUserModifiableSettingsTest() {
        assertNull(settingsManager.getSetting(NonUserModifiableSetting.DM_ID));
        assertNull(settingsManager.getSetting(NonUserModifiableSetting.DIR_PLUGINS));

        Map<NonUserModifiableSetting, String> nonUserModifiableSettings = new HashMap<>();
        nonUserModifiableSettings.put(NonUserModifiableSetting.DM_ID, "hello");
        nonUserModifiableSettings.put(NonUserModifiableSetting.DIR_PLUGINS, "plugins");
        settingsManager.setNonUserModifiableSettings(nonUserModifiableSettings);

        assertEquals("hello", settingsManager.getSetting(NonUserModifiableSetting.DM_ID));
        assertEquals("plugins", settingsManager.getSetting(NonUserModifiableSetting.DIR_PLUGINS));
    }
}
