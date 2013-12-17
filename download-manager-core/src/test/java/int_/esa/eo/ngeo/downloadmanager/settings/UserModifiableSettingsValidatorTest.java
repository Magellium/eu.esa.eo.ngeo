package int_.esa.eo.ngeo.downloadmanager.settings;

import int_.esa.eo.ngeo.downloadmanager.exception.InvalidSettingValueException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserModifiableSettingsValidatorTest {
	
	private static UserModifiableSettingsValidator userModifiableSettingsValidator;

	@Before
	public void setup() {
        userModifiableSettingsValidator = new UserModifiableSettingsValidator();
    }
	
	@Test
	public void testValidDMFriendlyName() throws InvalidSettingValueException {
		userModifiableSettingsValidator.validateSettingValue(UserModifiableSetting.DM_FRIENDLY_NAME, "abcdef");
	}

	@Test
	public void testInvalidDMFriendlyName() {
		try {
			userModifiableSettingsValidator.validateSettingValue(UserModifiableSetting.DM_FRIENDLY_NAME, "a");
		}catch(InvalidSettingValueException ex) {
			Assert.assertTrue("Exception with invalid setting value is not thrown as expected.", ex.getMessage().contains("Invalid value 'a' for setting DM_FRIENDLY_NAME"));
		}
	}
}
