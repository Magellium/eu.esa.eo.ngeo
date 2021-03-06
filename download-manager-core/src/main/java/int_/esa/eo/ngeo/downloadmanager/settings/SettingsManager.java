package int_.esa.eo.ngeo.downloadmanager.settings;

import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.observer.SettingsObserver;
import int_.esa.eo.ngeo.downloadmanager.observer.SettingsSubject;
import int_.esa.eo.ngeo.downloadmanager.rest.ConfigResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NB: We don't support scenarios where a user-modifiable and non-user-modifiable settings have the same key.
 * NB: We may not currently support distinguishing between cases where a setting has been defined to have a blank value and cases where the setting has not been defined. 
 */
public class SettingsManager implements SettingsSubject {
    private static final String THERE_IS_NO_SETTING_FOR = "There is no setting for %s";

    private static final String DM_HOME = "DM_HOME";

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsManager.class);

    private static final String CONF_DIR = "conf";
    private static final String USER_MODIFIABLE_SETTINGS_PERSISTENT_STORE     = "user-modifiable-settings.properties";
    private static final String NON_USER_MODIFIABLE_SETTINGS_PERSISTENT_STORE = "non-user-modifiable-settings.properties";

    private Properties nonUserModifiableProperties = new Properties();
    private Properties userModifiableProperties = new Properties();

    private List<SettingsObserver> observers;

    public SettingsManager() {
        this.observers = new ArrayList<>();
    }

    public void init() {
        // Load non-user-modifiable settings
        String persistentStoreAbsolutePath = System.getenv(DM_HOME) + File.separator + CONF_DIR + File.separator + NON_USER_MODIFIABLE_SETTINGS_PERSISTENT_STORE;
        String defaultValuesPathRelativeToClasspath = "/META-INF/non-user-modifiable-settings.properties";
        loadPropertiesFromPersistentStoreOrDefaults(nonUserModifiableProperties, persistentStoreAbsolutePath, defaultValuesPathRelativeToClasspath);

        // Load the user-modifiable settings
        persistentStoreAbsolutePath = System.getenv(DM_HOME) + File.separator + CONF_DIR + File.separator + USER_MODIFIABLE_SETTINGS_PERSISTENT_STORE;
        defaultValuesPathRelativeToClasspath = "/META-INF/user-modifiable-settings.properties";
        loadPropertiesFromPersistentStoreOrDefaults(userModifiableProperties, persistentStoreAbsolutePath, defaultValuesPathRelativeToClasspath);
    }

    private void loadPropertiesFromPersistentStoreOrDefaults(Properties properties, String persistentStoreAbsolutePath, String defaultValuesPathRelativeToClasspath) {
        InputStream in = null;
        try {
            File nonUserModifiableSettingsPersistentStore = new File(persistentStoreAbsolutePath); 
            if (nonUserModifiableSettingsPersistentStore.exists()) {
                in = new FileInputStream(nonUserModifiableSettingsPersistentStore);
            }else{
                LOGGER.info(String.format("Persistent property file \"%s\" does not exist; will use defaults instead", persistentStoreAbsolutePath));
                in = SettingsManager.class.getResourceAsStream(defaultValuesPathRelativeToClasspath);
            }
            properties.load(in);
        } catch (IOException e) {
            throw new NonRecoverableException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public String getSetting(UserModifiableSetting userModifiableSetting) {
        String settingName = userModifiableSetting.toString();
        String setting = userModifiableProperties.getProperty(settingName);
        if (StringUtils.isEmpty(setting)) {
            LOGGER.debug(String.format(THERE_IS_NO_SETTING_FOR, settingName));
            return setting;
        }
        switch (userModifiableSetting) {
        case SSO_PASSWORD:
        case EMAIL_SMTP_PASSWORD:
            return decrypt(setting);
        default:
            return setting;
        }
    }

    public String getSetting(NonUserModifiableSetting nonUserModifiableSetting) {
        String settingName = nonUserModifiableSetting.toString();
        String setting = nonUserModifiableProperties.getProperty(settingName);
        if (StringUtils.isEmpty(setting)) {
            LOGGER.debug(String.format(THERE_IS_NO_SETTING_FOR, settingName));
        }
        return setting;
    }

    private String decrypt(String setting) {
        Base64 base64 = new Base64();
        return new String(base64.decode(setting.getBytes()));
    }

    public synchronized void setUserModifiableSetting(UserModifiableSetting userModifiableSetting, String value) {
        Map<UserModifiableSetting, String> userModifiableSettings = new HashMap<>();
        userModifiableSettings.put(userModifiableSetting, value);
        setUserModifiableSettings(userModifiableSettings);
    }

    public synchronized void setUserModifiableSettings(Map<UserModifiableSetting, String> userModifiableSettings) {
        for (Entry<UserModifiableSetting, String> userModifiableSettingEntry : userModifiableSettings.entrySet()) {
            UserModifiableSetting userModifiableSetting = userModifiableSettingEntry.getKey();
            String value;
            switch (userModifiableSetting) {
            case SSO_PASSWORD:
            case EMAIL_SMTP_PASSWORD:
                value = encrypt(userModifiableSettingEntry.getValue());
                break;
            default:
                value = userModifiableSettingEntry.getValue();
            }

            userModifiableProperties.setProperty(userModifiableSetting.toString(), value);
        }
        updatePersistentStore(SettingsType.USER_MODIFIABLE);

        List<UserModifiableSetting> settingsToNotifyOfUpdate = new ArrayList<>();
        settingsToNotifyOfUpdate.addAll(userModifiableSettings.keySet());

        notifyObserversOfUpdateToUserModifiableSettings(settingsToNotifyOfUpdate);
    }

    public synchronized void setNonUserModifiableSetting(NonUserModifiableSetting nonUserModifiableSetting, String value) {
        Map<NonUserModifiableSetting, String> nonUserModifiableSettings = new HashMap<>();
        nonUserModifiableSettings.put(nonUserModifiableSetting, value);
        setNonUserModifiableSettings(nonUserModifiableSettings);
    }

    public synchronized void setNonUserModifiableSettings(Map<NonUserModifiableSetting, String> nonUserModifiableSettings) {
        for (Entry<NonUserModifiableSetting, String> nonUserModifiableSettingEntry : nonUserModifiableSettings.entrySet()) {
            NonUserModifiableSetting nonUserModifiableSetting = nonUserModifiableSettingEntry.getKey();
            String value = nonUserModifiableSettingEntry.getValue();

            nonUserModifiableProperties.setProperty(nonUserModifiableSetting.toString(), value);
        }
        //save properties to file
        updatePersistentStore(SettingsType.NON_USER_MODIFIABLE);

        List<NonUserModifiableSetting> settingsToNotifyOfUpdate = new ArrayList<>();
        settingsToNotifyOfUpdate.addAll(nonUserModifiableSettings.keySet());

        notifyObserversOfUpdateToNonUserModifiableSettings(settingsToNotifyOfUpdate);
    }

    private String encrypt(String value) {
        return Base64.encodeBase64String(value.getBytes());
    }

    protected synchronized void updatePersistentStore(SettingsType settingsType) {
        try {
            String pathNameOfPersistentStore = getPathNameOfPersistentStore(settingsType);
            File persistentStore = new File(pathNameOfPersistentStore);
            OutputStream out = new FileOutputStream(persistentStore);
            if (settingsType == SettingsType.NON_USER_MODIFIABLE) {
                nonUserModifiableProperties.store(out, "");
            }else{
                userModifiableProperties.store(out, "");
            }
        } catch (IOException e) {
            LOGGER.error(String.format("Unable to update the persistent store for %s settings", settingsType.toString()), e);
        }
    }

    private String getPathNameOfPersistentStore(SettingsType settingsType) {
        File parentFolderOfPersistentStores = new File(System.getenv(DM_HOME) + File.separator + CONF_DIR);
        parentFolderOfPersistentStores.mkdirs();

        String pathNameOfPersistentStore = Paths.get(parentFolderOfPersistentStores.getAbsolutePath(),
                settingsType == SettingsType.NON_USER_MODIFIABLE ? NON_USER_MODIFIABLE_SETTINGS_PERSISTENT_STORE
                        : USER_MODIFIABLE_SETTINGS_PERSISTENT_STORE).toString();
        return pathNameOfPersistentStore;
    }

    /*
     * TODO: Move into a seperate builder class? visibility of properties might need to be changed
     *       to protected or getter methods created
     */
    public ConfigResponse buildConfigResponse() {
        ConfigResponse configResponse = new ConfigResponse();

        List<UserModifiableSettingEntry> userModifiableSettingEntryList = new ArrayList<>();
        for (Map.Entry<Object, Object> userModifiablePropertiesEntry : userModifiableProperties.entrySet()) {
            UserModifiableSetting key = UserModifiableSetting.valueOf((String)userModifiablePropertiesEntry.getKey());
            String value = (String) userModifiablePropertiesEntry.getValue();

            UserModifiableSettingEntry userModifiableSettingEntry = new UserModifiableSettingEntry();
            userModifiableSettingEntry.setKey(key);
            userModifiableSettingEntry.setValue(value);

            userModifiableSettingEntryList.add(userModifiableSettingEntry);
        }
        List<NonUserModifiableSettingEntry> nonUserModifiableSettingEntryList = new ArrayList<>();
        for (Map.Entry<Object, Object> nonUserModifiablePropertiesEntry : nonUserModifiableProperties.entrySet()) {
            NonUserModifiableSetting key = NonUserModifiableSetting.valueOf((String)nonUserModifiablePropertiesEntry.getKey());
            String value = (String) nonUserModifiablePropertiesEntry.getValue();

            NonUserModifiableSettingEntry nonUserModifiableSettingEntry = new NonUserModifiableSettingEntry();
            nonUserModifiableSettingEntry.setKey(key);
            nonUserModifiableSettingEntry.setValue(value);

            nonUserModifiableSettingEntryList.add(nonUserModifiableSettingEntry);
        }

        configResponse.setUserModifiableSettingEntries(userModifiableSettingEntryList);
        configResponse.setNonUserModifiableSettingEntries(nonUserModifiableSettingEntryList);

        return configResponse;
    }

    @Override
    public void registerObserver(SettingsObserver o) {
        this.observers.add(o);
    }

    @Override
    public void notifyObserversOfUpdateToUserModifiableSettings(List<UserModifiableSetting> userModifiableSettings) {
        for (SettingsObserver o : observers) {
            o.updateToUserModifiableSettings(userModifiableSettings);
        }
    }

    @Override
    public void notifyObserversOfUpdateToNonUserModifiableSettings(List<NonUserModifiableSetting> nonUserModifiableSettings) {
        for (SettingsObserver o : observers) {
            o.updateToNonUserModifiableSettings(nonUserModifiableSettings);
        }
    }
}