package int_.esa.eo.ngeo.dmtu.cli.config;

import java.util.ResourceBundle;

public class ConfigurationProvider {
	private static ResourceBundle resourceBundle;
	
	public static final String DM_TITLE			= "DM_TITLE";
	public static final String DM_CLI_PROMPT    = "DM_CLI_PROMPT";
	public static final String DM_WEBAPP_URL    = "DM_WEBAPP_URL";

	private static void loadProperties() {
		resourceBundle = ResourceBundle.getBundle("cli");
	}

	public static String getProperty(String key) {
		if(resourceBundle == null) {
			loadProperties();
		}
		
		String value = resourceBundle.getString(key);
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException(String.format("There is no property for %s", key));
		}
		return value;
	}


}

