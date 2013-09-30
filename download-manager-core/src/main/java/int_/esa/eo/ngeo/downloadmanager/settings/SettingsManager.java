package int_.esa.eo.ngeo.downloadmanager.settings;

import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NB: We don't support scenarios where a user-modifiable and non-user-modifiable settings have the same key.
 * NB: We may not currently support distinguishing between cases where a setting has been defined to have a blank value and cases where the setting has not been defined. 
 */
public class SettingsManager {
	
	// TODO: If time allows, create a SettingKey enum (giving us type safety), rather than using Strings
	public static final String KEY_SSO_PASSWORD                     		= "SSO_PASSWORD";
	public static final String KEY_BASE_DOWNLOAD_FOLDER_ABSOLUTE     		= "BASE_DOWNLOAD_FOLDER_ABSOLUTE";
	public static final String KEY_DIR_PLUGINS                       		= "DIR_PLUGINS";
	public static final String KEY_DM_FRIENDLY_NAME                  		= "DM_FRIENDLY_NAME";
	public static final String KEY_DM_IS_REGISTERED                  		= "DM_IS_REGISTERED";
	public static final String KEY_DM_IS_SETUP                       		= "DM_IS_SETUP";
	public static final String KEY_DM_ID                       		 		= "DM_ID";
	public static final String KEY_NGEO_WEB_SERVER_URL               		= "NGEO_WEB_SERVER_URL";
	public static final String KEY_NGEO_MONITORING_SERVICE_URL       		= "NGEO_MONITORING_SERVICE_URL";
	public static final String KEY_NGEO_MONITORING_SERVICE_SET_TIME  		= "NGEO_MONITORING_SERVICE_SET_TIME";
	public static final String KEY_IICD_D_WS_DEFAULT_REFRESH_PERIOD 		= "IICD_D_WS_DEFAULT_REFRESH_PERIOD";
	public static final String KEY_NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS  = "NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS";
	public static final String KEY_PRODUCT_DOWNLOAD_COMPLETE_COMMAND 		= "PRODUCT_DOWNLOAD_COMPLETE_COMMAND";
	public static final String KEY_SSO_USERNAME                      		= "SSO_USERNAME";
	public static final String KEY_WEB_INTERFACE_PASSWORD            		= "WEB_INTERFACE_PASSWORD";
	public static final String KEY_WEB_INTERFACE_PORT_NO             		= "WEB_INTERFACE_PORT_NO";
	public static final String KEY_WEB_INTERFACE_REMOTE_ACCESS_ENABLED 		= "WEB_INTERFACE_REMOTE_ACCESS_ENABLED";
	public static final String KEY_WEB_INTERFACE_USERNAME            		= "WEB_INTERFACE_USERNAME";
	public static final String KEY_WEB_PROXY_PASSWORD                		= "WEB_PROXY_PASSWORD";
	public static final String KEY_WEB_PROXY_URL                    		= "WEB_PROXY_URL";
	public static final String KEY_WEB_PROXY_PORT                    		= "WEB_PROXY_PORT";
	public static final String KEY_WEB_PROXY_USERNAME                		= "WEB_PROXY_USERNAME";
	

	private static final Logger LOGGER = LoggerFactory.getLogger(SettingsManager.class);
	
	private final String NAME_OF_CONF_DIR = "conf";
	private final String NAME_OF_USER_MODIFIABLE_SETTINGS_PERSISTENT_STORE     =    "userModifiableSettingsPersistentStore.properties";
	private final String NAME_OF_NON_USER_MODIFIABLE_SETTINGS_PERSISTENT_STORE = "nonUserModifiableSettingsPersistentStore.properties";
	
	private Properties nonUserModifiableProperties = new Properties();
	private Properties userModifiableProperties = new Properties();
	
	private enum SettingsType {
		NON_USER_MODIFIABLE,
		USER_MODIFIABLE
	}
	
	public void init() {
		// Load non-user-modifiable settings
		String persistentStoreAbsolutePath = System.getenv("DM_HOME") + File.separator + NAME_OF_CONF_DIR + File.separator + NAME_OF_NON_USER_MODIFIABLE_SETTINGS_PERSISTENT_STORE;
		String defaultValuesPathRelativeToClasspath = "/META-INF/non-user-modifiable-settings-defaults.properties";
		loadPropertiesFromPersistentStoreOrDefaults(nonUserModifiableProperties, persistentStoreAbsolutePath, defaultValuesPathRelativeToClasspath);

		// Load the user-modifiable settings
		persistentStoreAbsolutePath = System.getenv("DM_HOME") + File.separator + NAME_OF_CONF_DIR + File.separator + NAME_OF_USER_MODIFIABLE_SETTINGS_PERSISTENT_STORE;
		defaultValuesPathRelativeToClasspath = "/META-INF/user-modifiable-settings-defaults.properties";
		loadPropertiesFromPersistentStoreOrDefaults(userModifiableProperties, persistentStoreAbsolutePath, defaultValuesPathRelativeToClasspath);
	}

	private void loadPropertiesFromPersistentStoreOrDefaults(Properties properties, String persistentStoreAbsolutePath, String defaultValuesPathRelativeToClasspath) {
		InputStream in = null;
		try {
			File nonUserModifiableSettingsPersistentStore = new File(persistentStoreAbsolutePath); 
			if (nonUserModifiableSettingsPersistentStore.exists()) {
				in = new FileInputStream(nonUserModifiableSettingsPersistentStore);
			}
			else {
				LOGGER.info(String.format("Persistent property file \"%s\" does not exist; will use defaults instead", persistentStoreAbsolutePath));
				in = SettingsManager.class.getResourceAsStream(defaultValuesPathRelativeToClasspath);
			}
			// TODO: Investigate whether to wrap InputStream within a BufferedInputStream
			properties.load(in);
		} 
		catch (IOException e) {
				throw new NonRecoverableException(e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Swallow
				}
			}
		}
	}
	
	private String getSettingInternal(String settingName) {
		String setting = null;
		setting = nonUserModifiableProperties.getProperty(settingName);
		if (setting == null) {
			setting = userModifiableProperties.getProperty(settingName);
		}
		return setting;
	}
	
	public String getSetting(String settingName) {
		String setting = getSettingInternal(settingName);
		if (setting == null || setting.isEmpty()) {
			LOGGER.info(String.format("There is no setting for %s", settingName));
		}
		if (settingName.equals(KEY_SSO_PASSWORD)) { 
			setting = decrypt(setting);
		}
		return setting;
	}
	
	private String decrypt(String setting) {
		Base64 base64 = new Base64();
		return new String(base64.decode(setting.getBytes()));
	}

	public void setSetting(String settingName, String value) {
		if (settingName.equals(KEY_SSO_PASSWORD)) {
			value = encrypt(value);
		}
		if (nonUserModifiableProperties.containsKey(settingName)) {
			nonUserModifiableProperties.setProperty(settingName, value);
			updatePersistentStore(SettingsType.NON_USER_MODIFIABLE);
		}
		else if (userModifiableProperties.containsKey(settingName)) {
			userModifiableProperties.setProperty(settingName, value);
			updatePersistentStore(SettingsType.USER_MODIFIABLE);
		}
		else {
			throw new NonRecoverableException(String.format("Unable to set value of setting \"%s\"; there is no corresponding settings repository.", settingName));
		}
	}

	private String encrypt(String value) {
//		Base64 base64 = new Base64();
		return Base64.encodeBase64String(value.getBytes()); // TODO: Implement stronger encryption?
	}

	private void updatePersistentStore(SettingsType settingsType) {
		try {
			String pathNameOfPersistentStore = getPathNameOfPersistentStore(settingsType);
			File persistentStore = new File(pathNameOfPersistentStore);
			OutputStream out = new FileOutputStream(persistentStore); // TODO: Investigate whether to wrap OutputStream within a BufferedOutputStream
			if (settingsType == SettingsType.NON_USER_MODIFIABLE) {
				nonUserModifiableProperties.store(out, "");
			}
			else {
				userModifiableProperties.store(out, "");
			}
		} catch (IOException e) {
			LOGGER.error(String.format("Unable to update the persistent store for %s settings", settingsType.toString()), e);
		}
	}

	private String getPathNameOfPersistentStore(SettingsType settingsType) {
		File parentFolderOfPersistentStores = new File(System.getenv("DM_HOME") + File.separator + NAME_OF_CONF_DIR);
		parentFolderOfPersistentStores.mkdirs();
		
		String pathNameOfPersistentStore = Paths.get(parentFolderOfPersistentStores.getAbsolutePath(),
				settingsType == SettingsType.NON_USER_MODIFIABLE ? NAME_OF_NON_USER_MODIFIABLE_SETTINGS_PERSISTENT_STORE
																 : NAME_OF_USER_MODIFIABLE_SETTINGS_PERSISTENT_STORE).toString();
		return pathNameOfPersistentStore;
	}

}