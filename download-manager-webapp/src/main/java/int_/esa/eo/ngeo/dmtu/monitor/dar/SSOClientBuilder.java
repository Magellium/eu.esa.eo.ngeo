package int_.esa.eo.ngeo.dmtu.monitor.dar;

import int_.esa.eo.ngeo.dmtu.controller.MonitoringController;
import int_.esa.eo.ngeo.downloadmanager.UmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;

import org.apache.commons.lang.StringUtils;

public class SSOClientBuilder {
	public UmSsoHttpClient buildSSOClientFromSettings(MonitoringController monitoringController) {
		String umSsoUsername = monitoringController.getSetting(SettingsManager.KEY_SSO_USERNAME);
		String umSsoPassword = monitoringController.getSetting(SettingsManager.KEY_SSO_PASSWORD);
		
		String proxyHost = monitoringController.getSetting(SettingsManager.KEY_WEB_PROXY_HOST);
		String proxyPortString = monitoringController.getSetting(SettingsManager.KEY_WEB_PROXY_PORT);
		int proxyPort;
		if (proxyPortString == null || proxyPortString.isEmpty()) {
			proxyPort = -1;
		}else{
			proxyPort = Integer.parseInt(proxyPortString);
		}
		String proxyUsername = monitoringController.getSetting(SettingsManager.KEY_WEB_PROXY_USERNAME);
		String proxyPassword = monitoringController.getSetting(SettingsManager.KEY_WEB_PROXY_PASSWORD);
		
		if (!StringUtils.isEmpty(proxyHost)) {
			if (!StringUtils.isEmpty(proxyUsername)) {
				return new UmSsoHttpClient(umSsoUsername, umSsoPassword, proxyHost, proxyPort, proxyUsername, proxyPassword);
			}
			else {
				return new UmSsoHttpClient(umSsoUsername, umSsoPassword, proxyHost, proxyPort);
			}
		}else{
			return new UmSsoHttpClient(umSsoUsername, umSsoPassword);
		}
	}

	public UmSsoHttpClient buildSSOClientFromSettings(SettingsManager settingsManager) {
		String umSsoUsername = settingsManager.getSetting(SettingsManager.KEY_SSO_USERNAME);
		String umSsoPassword = settingsManager.getSetting(SettingsManager.KEY_SSO_PASSWORD);
		
		String proxyHost = settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_HOST);
		String proxyPortString = settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_PORT);
		int proxyPort;
		if (proxyPortString == null || proxyPortString.isEmpty()) {
			proxyPort = -1;
		}else{
			proxyPort = Integer.parseInt(proxyPortString);
		}
		String proxyUsername = settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_USERNAME);
		String proxyPassword = settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_PASSWORD);
		
		if (!StringUtils.isEmpty(proxyHost)) {
			if (!StringUtils.isEmpty(proxyUsername)) {
				return new UmSsoHttpClient(umSsoUsername, umSsoPassword, proxyHost, proxyPort, proxyUsername, proxyPassword);
			}
			else {
				return new UmSsoHttpClient(umSsoUsername, umSsoPassword, proxyHost, proxyPort);
			}
		}else{
			return new UmSsoHttpClient(umSsoUsername, umSsoPassword);
		}
	}
}
