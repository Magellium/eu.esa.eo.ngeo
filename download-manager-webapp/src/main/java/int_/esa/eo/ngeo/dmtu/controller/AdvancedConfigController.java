package int_.esa.eo.ngeo.dmtu.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import int_.esa.eo.ngeo.dmtu.configuration.AdvancedConfigSettings;
import int_.esa.eo.ngeo.dmtu.manager.SettingsManager;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * See requirement DMTU-REQ-17
 */
@Controller
@RequestMapping(value="/advancedconfig")
public class AdvancedConfigController {
	
	private static final String ADVANCEDCONFIG_VIEW_NAME = "advancedConfig";
	
	@Autowired
	private SettingsManager settingsManager;
	
	@RequestMapping(method = RequestMethod.GET)
	public String showForm(ModelMap model) {
		
		// XXX: The following if statement is a shameless copy/paste from HomeController; we ought to factor out the common code instead.
		if (!Boolean.parseBoolean(settingsManager.getSetting(SettingsManager.KEY_DM_IS_SETUP))) {
			return "redirect:/firststartupconfig";
		}
		
		AdvancedConfigSettings advancedConfigSettings = new AdvancedConfigSettings();
		advancedConfigSettings.setBaseDownloadFolder(settingsManager.getSetting(SettingsManager.KEY_BASE_DOWNLOAD_FOLDER_ABSOLUTE));
		advancedConfigSettings.setDmFriendlyName(settingsManager.getSetting(SettingsManager.KEY_DM_FRIENDLY_NAME));
		advancedConfigSettings.setNoOfParallelProductDownloadThreads(Integer.parseInt(settingsManager.getSetting(SettingsManager.KEY_NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS)));
		advancedConfigSettings.setProductDownloadCompleteCommand(settingsManager.getSetting(SettingsManager.KEY_PRODUCT_DOWNLOAD_COMPLETE_COMMAND));
		advancedConfigSettings.setSsoPassword(settingsManager.getSetting(SettingsManager.KEY_SSO_PASSWORD));
		advancedConfigSettings.setSsoUsername(settingsManager.getSetting(SettingsManager.KEY_SSO_USERNAME));
		advancedConfigSettings.setWebInterfacePassword(settingsManager.getSetting(SettingsManager.KEY_WEB_INTERFACE_PASSWORD));
		advancedConfigSettings.setWebInterfacePortNo(settingsManager.getSetting(SettingsManager.KEY_WEB_INTERFACE_PORT_NO));
		advancedConfigSettings.setWebInterfaceRemoteAccessEnabled(Boolean.parseBoolean(settingsManager.getSetting(SettingsManager.KEY_WEB_INTERFACE_REMOTE_ACCESS_ENABLED)));
		advancedConfigSettings.setWebInterfaceUsername(settingsManager.getSetting(SettingsManager.KEY_WEB_INTERFACE_USERNAME));
		advancedConfigSettings.setWebProxyPassword(settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_PASSWORD));
		advancedConfigSettings.setWebProxyUrl(settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_URL));
		advancedConfigSettings.setWebProxyUsername(settingsManager.getSetting(SettingsManager.KEY_WEB_PROXY_USERNAME));
		model.addAttribute("ADVANCEDCONFIGSETTINGS", advancedConfigSettings);
		return ADVANCEDCONFIG_VIEW_NAME;
	}

	@RequestMapping(method=RequestMethod.POST)
	public String processForm(@Valid @ModelAttribute(value="ADVANCEDCONFIGSETTINGS") AdvancedConfigSettings advancedConfigSettings, BindingResult result) {
		if (result.hasErrors()) {
			return ADVANCEDCONFIG_VIEW_NAME;
		}
		settingsManager.setSetting(SettingsManager.KEY_SSO_USERNAME, advancedConfigSettings.getSsoUsername());
		settingsManager.setSetting(SettingsManager.KEY_SSO_PASSWORD, advancedConfigSettings.getSsoPassword());
		settingsManager.setSetting(SettingsManager.KEY_DM_FRIENDLY_NAME, advancedConfigSettings.getDmFriendlyName());
		settingsManager.setSetting(SettingsManager.KEY_BASE_DOWNLOAD_FOLDER_ABSOLUTE, advancedConfigSettings.getBaseDownloadFolder());
		settingsManager.setSetting(SettingsManager.KEY_WEB_INTERFACE_PORT_NO, advancedConfigSettings.getWebInterfacePortNo());
		settingsManager.setSetting(SettingsManager.KEY_NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS, Integer.toString(advancedConfigSettings.getNoOfParallelProductDownloadThreads()));
		settingsManager.setSetting(SettingsManager.KEY_WEB_PROXY_URL, advancedConfigSettings.getWebProxyUrl());
		settingsManager.setSetting(SettingsManager.KEY_WEB_PROXY_PORT, advancedConfigSettings.getWebProxyPort());
		settingsManager.setSetting(SettingsManager.KEY_WEB_PROXY_USERNAME, advancedConfigSettings.getWebProxyUsername());
		settingsManager.setSetting(SettingsManager.KEY_WEB_PROXY_PASSWORD, advancedConfigSettings.getWebProxyPassword());
		settingsManager.setSetting(SettingsManager.KEY_PRODUCT_DOWNLOAD_COMPLETE_COMMAND, advancedConfigSettings.getProductDownloadCompleteCommand());
		settingsManager.setSetting(SettingsManager.KEY_WEB_INTERFACE_USERNAME, advancedConfigSettings.getWebInterfaceUsername());
		settingsManager.setSetting(SettingsManager.KEY_WEB_INTERFACE_PASSWORD, advancedConfigSettings.getWebInterfacePassword());
		settingsManager.setSetting(SettingsManager.KEY_WEB_INTERFACE_REMOTE_ACCESS_ENABLED, Boolean.toString(advancedConfigSettings.isWebInterfaceRemoteAccessEnabled()));
		
		// XXX: The controller should ultimately not be responsible for creating this directory if it doesn't exist
		Path baseDownloadFolderPath = Paths.get(advancedConfigSettings.getBaseDownloadFolder());
		if(Files.exists(baseDownloadFolderPath)) {
			try {
				Files.createDirectories(baseDownloadFolderPath);
			} catch (IOException e) {
				FieldError fieldError = new FieldError("AdvancedConfigSettings", "baseDownloadFolder", String.format("Unable to create base download directory: %s", e.getLocalizedMessage()));
				result.addError(fieldError);
				return "ADVANCEDCONFIG_VIEW_NAME";
			}
		}
		
		return "redirect:/";
	}
}