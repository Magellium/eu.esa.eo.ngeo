package int_.esa.eo.ngeo.downloadmanager.settings;

import int_.esa.eo.ngeo.downloadmanager.configuration.AdvancedConfigSettings;
import int_.esa.eo.ngeo.downloadmanager.exception.InvalidSettingValueException;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class UserModifiableSettingsValidator {
	private static Validator validator;

	public UserModifiableSettingsValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
	
	public void validateSettingValue(UserModifiableSetting settingKey, String settingValue) throws InvalidSettingValueException {
		String configSettingPropertyForUserModifiableSetting = getConfigSettingPropertyForUserModifiableSetting(settingKey);
		
		Set<ConstraintViolation<AdvancedConfigSettings>> validate = validator.validateValue(AdvancedConfigSettings.class, configSettingPropertyForUserModifiableSetting, settingValue);

		if(!validate.isEmpty()) {
			StringBuilder contraintMessage = new StringBuilder();
			for (ConstraintViolation<AdvancedConfigSettings> constraintViolation : validate) {
				contraintMessage.append("\n\t");
				contraintMessage.append(constraintViolation.getMessage());
			}
			throw new InvalidSettingValueException(String.format("Invalid value '%s' for setting %s %s", settingValue, settingKey, contraintMessage.toString()));
		}
	}
	
	private String getConfigSettingPropertyForUserModifiableSetting(UserModifiableSetting settingKey) {
		switch (settingKey) {
		case SSO_USERNAME:
			return "ssoUsername";
		case SSO_PASSWORD:
			return "ssoPassword";
		case DM_FRIENDLY_NAME:
			return "dmFriendlyName";
		case BASE_DOWNLOAD_FOLDER_ABSOLUTE:
			return "baseDownloadFolder";
		case NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS:
			return "noOfParallelProductDownloadThreads";
		case PRODUCT_DOWNLOAD_COMPLETE_COMMAND:
			return "productDownloadCompleteCommand";
		case WEB_INTERFACE_USERNAME:
			return "webInterfaceUsername";
		case WEB_INTERFACE_PASSWORD:
			return "webInterfacePassword";
		case WEB_INTERFACE_PORT_NO:
			return "webInterfacePortNo";
		case WEB_INTERFACE_REMOTE_ACCESS_ENABLED:
			return "webInterfaceRemoteAccessEnabled";
		case WEB_PROXY_HOST:
			return "webProxyHost";
		case WEB_PROXY_PORT:
			return "webProxyPort";
		case WEB_PROXY_USERNAME:
			return "webProxyUsername";
		case WEB_PROXY_PASSWORD:
			return "webProxyPassword";
		default:
			throw new IllegalArgumentException(String.format("Unable to find mapping for setting %s", settingKey));
		}
	}
}
