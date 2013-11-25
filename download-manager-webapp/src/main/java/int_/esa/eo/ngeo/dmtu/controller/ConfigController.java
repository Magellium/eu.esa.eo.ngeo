package int_.esa.eo.ngeo.dmtu.controller;

import int_.esa.eo.ngeo.dmtu.exception.WebServerServiceException;
import int_.esa.eo.ngeo.dmtu.monitor.dar.DARMonitor;
import int_.esa.eo.ngeo.downloadmanager.configuration.AdvancedConfigSettings;
import int_.esa.eo.ngeo.downloadmanager.configuration.ConfigSettings;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value="/config")
public class ConfigController {
	
	private static final String FIRST_STARTUP_CONFIG_VIEW_NAME = "firstStartupConfig";
	private static final String ADVANCEDCONFIG_VIEW_NAME = "advancedConfig";

	@Autowired
	private SettingsManager settingsManager;
		
	@Autowired
	private DARMonitor darMonitor;
	
	@RequestMapping(value="/firststartup", method=RequestMethod.GET)
	public String showFirstStartupConfigForm(ModelMap model){
		if (Boolean.parseBoolean(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_SETUP))
				&& Boolean.parseBoolean(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_REGISTERED))) {
			return "redirect:/";
		}
		
		ConfigSettings firstStartupConfigSettings = new ConfigSettings();
		getConfigSettingsFromManager(firstStartupConfigSettings);
		model.addAttribute("FIRSTSTARTUPCONFIGSETTINGS", firstStartupConfigSettings);
		return FIRST_STARTUP_CONFIG_VIEW_NAME;
	}

	@RequestMapping(value="/firststartup", method=RequestMethod.POST)
	public String processFirstStartupConfigForm(@Valid @ModelAttribute(value="FIRSTSTARTUPCONFIGSETTINGS") ConfigSettings firstStartupConfigSettings, BindingResult result) {
		if (result.hasErrors()) {
			return FIRST_STARTUP_CONFIG_VIEW_NAME;
		}

		setConfigSettingsToManager(firstStartupConfigSettings);
		settingsManager.setSetting(NonUserModifiableSetting.DM_IS_SETUP, "true");
		
		try {
			createBaseDownloadFolder(firstStartupConfigSettings.getBaseDownloadFolder());
		} catch (IOException e) {
			FieldError fieldError = new FieldError("ConfigSettings", "baseDownloadFolder", String.format("Unable to create base download directory: %s", e.getLocalizedMessage()));
			result.addError(fieldError);
			return FIRST_STARTUP_CONFIG_VIEW_NAME;
		}

		// XXX: Would be preferable for this class not to be *directly* responsible for doing the following:
		try {
			darMonitor.registerDownloadManager();
		} catch (WebServerServiceException e) {
			FieldError fieldError = new FieldError("ConfigSettings", "ssoUserName", e.getLocalizedMessage());
			result.addError(fieldError);
			return FIRST_STARTUP_CONFIG_VIEW_NAME;
		}
		darMonitor.monitorForDARs();
		
		return "redirect:/";
	}

	@RequestMapping(value="/advanced", method = RequestMethod.GET)
	public String showAdvancedConfigForm(ModelMap model) {
		
		// XXX: The following if statement is a shameless copy/paste from HomeController; we ought to factor out the common code instead.
		if (!Boolean.parseBoolean(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_SETUP))) {
			return "redirect:/config/firststartup";
		}
		
		AdvancedConfigSettings advancedConfigSettings = new AdvancedConfigSettings();
		getConfigSettingsFromManager(advancedConfigSettings);
		advancedConfigSettings.setNoOfParallelProductDownloadThreads(Integer.parseInt(settingsManager.getSetting(UserModifiableSetting.NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS)));
		advancedConfigSettings.setProductDownloadCompleteCommand(settingsManager.getSetting(UserModifiableSetting.PRODUCT_DOWNLOAD_COMPLETE_COMMAND));
		advancedConfigSettings.setWebInterfaceUsername(settingsManager.getSetting(UserModifiableSetting.WEB_INTERFACE_USERNAME));
		advancedConfigSettings.setWebInterfacePassword(settingsManager.getSetting(UserModifiableSetting.WEB_INTERFACE_PASSWORD));
		advancedConfigSettings.setWebInterfaceRemoteAccessEnabled(Boolean.parseBoolean(settingsManager.getSetting(UserModifiableSetting.WEB_INTERFACE_REMOTE_ACCESS_ENABLED)));
		model.addAttribute("ADVANCEDCONFIGSETTINGS", advancedConfigSettings);
		return ADVANCEDCONFIG_VIEW_NAME;
	}

	@RequestMapping(value="/advanced", method=RequestMethod.POST)
	public String processAdvancedConfigForm(@Valid @ModelAttribute(value="ADVANCEDCONFIGSETTINGS") AdvancedConfigSettings advancedConfigSettings, BindingResult result) {
		if (result.hasErrors()) {
			return ADVANCEDCONFIG_VIEW_NAME;
		}

		setConfigSettingsToManager(advancedConfigSettings);
		
		settingsManager.setSetting(UserModifiableSetting.NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS, Integer.toString(advancedConfigSettings.getNoOfParallelProductDownloadThreads()));
		settingsManager.setSetting(UserModifiableSetting.PRODUCT_DOWNLOAD_COMPLETE_COMMAND, advancedConfigSettings.getProductDownloadCompleteCommand());
		settingsManager.setSetting(UserModifiableSetting.WEB_INTERFACE_USERNAME, advancedConfigSettings.getWebInterfaceUsername());
		settingsManager.setSetting(UserModifiableSetting.WEB_INTERFACE_PASSWORD, advancedConfigSettings.getWebInterfacePassword());
		settingsManager.setSetting(UserModifiableSetting.WEB_INTERFACE_REMOTE_ACCESS_ENABLED, Boolean.toString(advancedConfigSettings.isWebInterfaceRemoteAccessEnabled()));
		
		try {
			createBaseDownloadFolder(advancedConfigSettings.getBaseDownloadFolder());
		} catch (IOException e) {
			FieldError fieldError = new FieldError("AdvancedConfigSettings", "baseDownloadFolder", String.format("Unable to create base download directory: %s", e.getLocalizedMessage()));
			result.addError(fieldError);
			return ADVANCEDCONFIG_VIEW_NAME;
		}

		return "redirect:/";
	}
	
	private void getConfigSettingsFromManager(ConfigSettings configSettings) {
		configSettings.setSsoPassword(settingsManager.getSetting(UserModifiableSetting.SSO_PASSWORD));
		configSettings.setSsoUsername(settingsManager.getSetting(UserModifiableSetting.SSO_USERNAME));

		String baseDownloadFolderAbsolute = settingsManager.getSetting(UserModifiableSetting.BASE_DOWNLOAD_FOLDER_ABSOLUTE);
		if (!Boolean.parseBoolean(settingsManager.getSetting(NonUserModifiableSetting.DM_IS_SETUP))) {
			baseDownloadFolderAbsolute = Paths.get(System.getProperty("user.home"), "ngEO-Downloads").toString(); // Default value
		}
		configSettings.setBaseDownloadFolder(baseDownloadFolderAbsolute);
		configSettings.setDmFriendlyName(settingsManager.getSetting(UserModifiableSetting.DM_FRIENDLY_NAME));
		configSettings.setWebInterfacePortNo(settingsManager.getSetting(UserModifiableSetting.WEB_INTERFACE_PORT_NO));
		configSettings.setWebProxyHost(settingsManager.getSetting(UserModifiableSetting.WEB_PROXY_HOST));
		configSettings.setWebProxyPort(settingsManager.getSetting(UserModifiableSetting.WEB_PROXY_PORT));
		configSettings.setWebProxyPassword(settingsManager.getSetting(UserModifiableSetting.WEB_PROXY_PASSWORD));
		configSettings.setWebProxyUsername(settingsManager.getSetting(UserModifiableSetting.WEB_PROXY_USERNAME));
	}	

	private void setConfigSettingsToManager(ConfigSettings configSettings) {
		settingsManager.setSetting(UserModifiableSetting.SSO_USERNAME, configSettings.getSsoUsername());
		settingsManager.setSetting(UserModifiableSetting.SSO_PASSWORD, configSettings.getSsoPassword());
		settingsManager.setSetting(UserModifiableSetting.DM_FRIENDLY_NAME, configSettings.getDmFriendlyName());
		settingsManager.setSetting(UserModifiableSetting.BASE_DOWNLOAD_FOLDER_ABSOLUTE, configSettings.getBaseDownloadFolder());
		settingsManager.setSetting(UserModifiableSetting.WEB_INTERFACE_PORT_NO, configSettings.getWebInterfacePortNo());
		settingsManager.setSetting(UserModifiableSetting.WEB_PROXY_HOST, configSettings.getWebProxyHost());
		settingsManager.setSetting(UserModifiableSetting.WEB_PROXY_PORT, configSettings.getWebProxyPort());
		settingsManager.setSetting(UserModifiableSetting.WEB_PROXY_USERNAME, configSettings.getWebProxyUsername());
		settingsManager.setSetting(UserModifiableSetting.WEB_PROXY_PASSWORD, configSettings.getWebProxyPassword());
	}

	// XXX: The controller should ultimately not be responsible for creating this directory if it doesn't exist
	private void createBaseDownloadFolder(String baseDownloadFolder) throws IOException {
		Path baseDownloadFolderPath = Paths.get(baseDownloadFolder);
		if(Files.exists(baseDownloadFolderPath)) {
			Files.createDirectories(baseDownloadFolderPath);
		}
	}
}