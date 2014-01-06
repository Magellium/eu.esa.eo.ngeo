package int_.esa.eo.ngeo.downloadmanager.http;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ConnectionPropertiesSynchronizedUmSsoHttpClientTest {
	private ConnectionPropertiesSynchronizedUmSsoHttpClient connectionPropertiesSynchronizedUmSsoHttpClient;
	private SettingsManager settingsManager;
	
	@Before
	public void setup() {
		settingsManager = mock(SettingsManager.class);
		connectionPropertiesSynchronizedUmSsoHttpClient = spy(new ConnectionPropertiesSynchronizedUmSsoHttpClient(settingsManager));
	}
	
	@Test
	public void updateToUserModifiableSettingsTestNoSettings() {
		connectionPropertiesSynchronizedUmSsoHttpClient.updateToUserModifiableSettings(null);
		
		verify(connectionPropertiesSynchronizedUmSsoHttpClient, times(0)).initUmSsoConnectionSettingsFromSettingsManager();
	}

	@Test
	public void updateToUserModifiableSettingsTestNonObservedSettings() {
		List<UserModifiableSetting> userModifiableSettings = new ArrayList<>();
		userModifiableSettings.add(UserModifiableSetting.DM_FRIENDLY_NAME);
		
		connectionPropertiesSynchronizedUmSsoHttpClient.updateToUserModifiableSettings(userModifiableSettings);
		
		verify(connectionPropertiesSynchronizedUmSsoHttpClient, times(0)).initUmSsoConnectionSettingsFromSettingsManager();
	}

	@Test
	public void updateToUserModifiableSettingsTestObservedSettings() {
		List<UserModifiableSetting> userModifiableSettings = new ArrayList<>();
		userModifiableSettings.add(UserModifiableSetting.SSO_USERNAME);
		
		connectionPropertiesSynchronizedUmSsoHttpClient.updateToUserModifiableSettings(userModifiableSettings);

		verify(connectionPropertiesSynchronizedUmSsoHttpClient, times(1)).initUmSsoConnectionSettingsFromSettingsManager();
	}
}
