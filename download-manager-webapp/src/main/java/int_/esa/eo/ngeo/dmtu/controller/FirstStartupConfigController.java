package int_.esa.eo.ngeo.dmtu.controller;

import int_.esa.eo.ngeo.dmtu.configuration.FirstStartupConfigSettings;
import int_.esa.eo.ngeo.dmtu.exception.WebServerServiceException;
import int_.esa.eo.ngeo.dmtu.monitor.dar.DARMonitor;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;

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
@RequestMapping(value="/firststartupconfig")
public class FirstStartupConfigController {
	
	@Autowired
	private SettingsManager settingsManager;
		
	@Autowired
	private DARMonitor darMonitor;
	
	@RequestMapping(method=RequestMethod.GET)
	public String showForm(ModelMap model){
		
		if (Boolean.parseBoolean(settingsManager.getSetting(SettingsManager.KEY_DM_IS_SETUP))
				&& Boolean.parseBoolean(settingsManager.getSetting(SettingsManager.KEY_DM_IS_REGISTERED))) {
			return "redirect:/";
		}
		
		FirstStartupConfigSettings firstStartupConfigSettings = new FirstStartupConfigSettings();
		firstStartupConfigSettings.setDmFriendlyName(settingsManager.getSetting(SettingsManager.KEY_DM_FRIENDLY_NAME));
		String baseDownloadFolderAbsolute = settingsManager.getSetting(SettingsManager.KEY_BASE_DOWNLOAD_FOLDER_ABSOLUTE);
		if (!Boolean.parseBoolean(settingsManager.getSetting(SettingsManager.KEY_DM_IS_SETUP))) {
			baseDownloadFolderAbsolute = Paths.get(System.getProperty("user.home"), "ngEO-Downloads").toString(); // Default value
		}
		firstStartupConfigSettings.setBaseDownloadFolder(baseDownloadFolderAbsolute);
		firstStartupConfigSettings.setWebInterfacePortNo(settingsManager.getSetting(SettingsManager.KEY_WEB_INTERFACE_PORT_NO));
		model.addAttribute("FIRSTSTARTUPCONFIGSETTINGS", firstStartupConfigSettings);
		return "firstStartupConfig";
	}

	@RequestMapping(method=RequestMethod.POST)
	public String processForm(@Valid @ModelAttribute(value="FIRSTSTARTUPCONFIGSETTINGS") FirstStartupConfigSettings firstStartupConfigSettings, BindingResult result) {
		if (result.hasErrors()) {
			return "firstStartupConfig";
		}
		settingsManager.setSetting(SettingsManager.KEY_SSO_USERNAME, firstStartupConfigSettings.getSsoUsername());
		settingsManager.setSetting(SettingsManager.KEY_SSO_PASSWORD, firstStartupConfigSettings.getSsoPassword());
		settingsManager.setSetting(SettingsManager.KEY_DM_FRIENDLY_NAME, firstStartupConfigSettings.getDmFriendlyName());
		settingsManager.setSetting(SettingsManager.KEY_BASE_DOWNLOAD_FOLDER_ABSOLUTE, firstStartupConfigSettings.getBaseDownloadFolder());
		settingsManager.setSetting(SettingsManager.KEY_WEB_INTERFACE_PORT_NO, firstStartupConfigSettings.getWebInterfacePortNo());
		settingsManager.setSetting(SettingsManager.KEY_DM_IS_SETUP, "true");
		
		// XXX: The controller should ultimately not be responsible for creating this directory if it doesn't exist
		Path baseDownloadFolderPath = Paths.get(firstStartupConfigSettings.getBaseDownloadFolder());
		if(Files.exists(baseDownloadFolderPath)) {
			try {
				Files.createDirectories(baseDownloadFolderPath);
			} catch (IOException e) {
				FieldError fieldError = new FieldError("FirstStartupConfigSettings", "baseDownloadFolder", String.format("Unable to create base download directory: %s", e.getLocalizedMessage()));
				result.addError(fieldError);
				return "firstStartupConfig";
			}
		}

		// XXX: Would be preferable for this class not to be *directly* responsible for doing the following:
		try {
			darMonitor.registerDownloadManager();
		} catch (WebServerServiceException e) {
			FieldError fieldError = new FieldError("FirstStartupConfigSettings", "ssoUserName", e.getLocalizedMessage());
			result.addError(fieldError);
			return "firstStartupConfig";
		}
		darMonitor.monitorForDARs();
		
		return "redirect:/";
	}
}