package int_.esa.eo.ngeo.dmtu.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HomeControllerTest {
	@InjectMocks private HomeController controller = new HomeController();
	@Mock SettingsManager settingsManager;

	@Test
	public void testHomeControllerWhenDmIsSetupAndIsRegistered() {
		when(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_SETUP)).thenReturn("true");
		when(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_REGISTERED)).thenReturn("true");
		assertEquals("home", controller.home());
	}

	@Test
	public void testHomeControllerWhenDmIsSetupAndIsNotRegistered() {
		when(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_SETUP)).thenReturn("true");
		when(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_REGISTERED)).thenReturn("false");
		assertEquals("redirect:/config/firststartup", controller.home());
	}

	@Test
	public void testHomeControllerWhenDmIsNotSetupAndIsNotRegistered() {
		when(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_SETUP)).thenReturn("false");
		when(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_REGISTERED)).thenReturn("false");
		assertEquals("redirect:/config/firststartup", controller.home());
	}
}
