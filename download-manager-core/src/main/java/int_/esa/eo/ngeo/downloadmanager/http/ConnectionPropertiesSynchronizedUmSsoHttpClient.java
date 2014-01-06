package int_.esa.eo.ngeo.downloadmanager.http;

import int_.esa.eo.ngeo.downloadmanager.observer.SettingsObserver;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper class for UmSsoHttpClient which keeps the latter's connection properties 
 * in sync with the corresponding settings of the SettingsManager.
 */
public class ConnectionPropertiesSynchronizedUmSsoHttpClient implements SettingsObserver {
	private SettingsManager settingsManager;
	private List<UserModifiableSetting> userModifiableSettingsToObserve;
	private UmSsoHttpClient umSsoHttpClient;
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPropertiesSynchronizedUmSsoHttpClient.class);

	public ConnectionPropertiesSynchronizedUmSsoHttpClient(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
		userModifiableSettingsToObserve = new ArrayList<>();
		userModifiableSettingsToObserve.add(UserModifiableSetting.SSO_USERNAME);
		userModifiableSettingsToObserve.add(UserModifiableSetting.SSO_PASSWORD);
		userModifiableSettingsToObserve.add(UserModifiableSetting.WEB_PROXY_HOST);
		userModifiableSettingsToObserve.add(UserModifiableSetting.WEB_PROXY_PORT);
		userModifiableSettingsToObserve.add(UserModifiableSetting.WEB_PROXY_USERNAME);
		userModifiableSettingsToObserve.add(UserModifiableSetting.WEB_PROXY_PASSWORD);
	}

	@Override
	public void updateToUserModifiableSettings(List<UserModifiableSetting> userModifiableSetting) {
		if(userModifiableSetting != null && !Collections.disjoint(userModifiableSettingsToObserve, userModifiableSetting)) {
			LOGGER.debug("Settings which we are observing have changed, so update connection settings");
			initUmSsoConnectionSettingsFromSettingsManager();
		}
	}

	public void registerWithSettingsManager() {
		settingsManager.registerObserver(this);
	}
	
	public synchronized void initUmSsoConnectionSettingsFromSettingsManager() {
		String umSsoUsername = settingsManager.getSetting(UserModifiableSetting.SSO_USERNAME);
		String umSsoPassword = settingsManager.getSetting(UserModifiableSetting.SSO_PASSWORD);
		
		String proxyHost = settingsManager.getSetting(UserModifiableSetting.WEB_PROXY_HOST);
		String proxyPortString = settingsManager.getSetting(UserModifiableSetting.WEB_PROXY_PORT);
		int proxyPort;
		if (proxyPortString == null || proxyPortString.isEmpty()) {
			proxyPort = -1;
		}else{
			proxyPort = Integer.parseInt(proxyPortString);
		}
		String proxyUsername = settingsManager.getSetting(UserModifiableSetting.WEB_PROXY_USERNAME);
		String proxyPassword = settingsManager.getSetting(UserModifiableSetting.WEB_PROXY_PASSWORD);
		
		UmSsoHttpConnectionSettings umSsoHttpConnectionSettings;
		if (!StringUtils.isEmpty(proxyHost)) {
			if (!StringUtils.isEmpty(proxyUsername)) {
				umSsoHttpConnectionSettings = new UmSsoHttpConnectionSettings(umSsoUsername, umSsoPassword, proxyHost, proxyPort, proxyUsername, proxyPassword);
			}else{
				umSsoHttpConnectionSettings = new UmSsoHttpConnectionSettings(umSsoUsername, umSsoPassword, proxyHost, proxyPort);
			}
		}else{
			umSsoHttpConnectionSettings = new UmSsoHttpConnectionSettings(umSsoUsername, umSsoPassword);
		}
		
		LOGGER.debug(String.format("New connection details:%n%s", umSsoHttpConnectionSettings.toString()));
		if(umSsoHttpClient == null) {
			umSsoHttpClient = new UmSsoHttpClient(umSsoHttpConnectionSettings);
		}else{
			umSsoHttpClient.setUmSsoHttpConnectionSettings(umSsoHttpConnectionSettings);
		}
	}

	@Override
	public void updateToNonUserModifiableSettings(List<NonUserModifiableSetting> nonUserModifiableSetting) {
		//There are no non-user modifiable settings which this class is interested in
	}

	public synchronized UmSsoHttpClient getUmSsoHttpClient() {
		return umSsoHttpClient;
	}
}
