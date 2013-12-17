package int_.esa.eo.ngeo.downloadmanager.builder;

import int_.esa.eo.ngeo.downloadmanager.UmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

import org.apache.commons.lang.StringUtils;

public class SSOClientBuilder {
	public UmSsoHttpClient buildSSOClientFromSettings(SettingsManager settingsManager) {
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
		
		if (!StringUtils.isEmpty(proxyHost)) {
			if (!StringUtils.isEmpty(proxyUsername)) {
				return new UmSsoHttpClient(umSsoUsername, umSsoPassword, proxyHost, proxyPort, proxyUsername, proxyPassword);
			}else{
				return new UmSsoHttpClient(umSsoUsername, umSsoPassword, proxyHost, proxyPort);
			}
		}else{
			return new UmSsoHttpClient(umSsoUsername, umSsoPassword);
		}
	}
}
