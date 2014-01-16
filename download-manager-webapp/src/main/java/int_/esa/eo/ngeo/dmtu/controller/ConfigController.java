package int_.esa.eo.ngeo.dmtu.controller;

import int_.esa.eo.ngeo.dmtu.exception.WebServerServiceException;
import int_.esa.eo.ngeo.dmtu.monitor.dar.DARMonitor;
import int_.esa.eo.ngeo.downloadmanager.builder.CommandResponseBuilder;
import int_.esa.eo.ngeo.downloadmanager.configuration.AdvancedConfigSettings;
import int_.esa.eo.ngeo.downloadmanager.configuration.ConfigSettings;
import int_.esa.eo.ngeo.downloadmanager.exception.InvalidSettingValueException;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;
import int_.esa.eo.ngeo.downloadmanager.rest.ConfigResponse;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSettingsValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

        Map<UserModifiableSetting, String> configSettingsMap = createConfigSettingsMap(firstStartupConfigSettings);
        settingsManager.setUserModifiableSettings(configSettingsMap);

        settingsManager.setNonUserModifiableSetting(NonUserModifiableSetting.DM_IS_SETUP, "true");

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

        Map<UserModifiableSetting, String> userModifiableSettings = new HashMap<>();
        userModifiableSettings.putAll(createConfigSettingsMap(advancedConfigSettings));

        userModifiableSettings.put(UserModifiableSetting.NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS, Integer.toString(advancedConfigSettings.getNoOfParallelProductDownloadThreads()));
        userModifiableSettings.put(UserModifiableSetting.PRODUCT_DOWNLOAD_COMPLETE_COMMAND, advancedConfigSettings.getProductDownloadCompleteCommand());
        userModifiableSettings.put(UserModifiableSetting.WEB_INTERFACE_USERNAME, advancedConfigSettings.getWebInterfaceUsername());
        userModifiableSettings.put(UserModifiableSetting.WEB_INTERFACE_PASSWORD, advancedConfigSettings.getWebInterfacePassword());
        userModifiableSettings.put(UserModifiableSetting.WEB_INTERFACE_REMOTE_ACCESS_ENABLED, Boolean.toString(advancedConfigSettings.isWebInterfaceRemoteAccessEnabled()));

        settingsManager.setUserModifiableSettings(userModifiableSettings);
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

    private Map<UserModifiableSetting, String> createConfigSettingsMap(ConfigSettings configSettings) {
        Map<UserModifiableSetting, String> configSettingsMap = new HashMap<>();

        configSettingsMap.put(UserModifiableSetting.SSO_USERNAME, configSettings.getSsoUsername());
        configSettingsMap.put(UserModifiableSetting.SSO_PASSWORD, configSettings.getSsoPassword());
        configSettingsMap.put(UserModifiableSetting.DM_FRIENDLY_NAME, configSettings.getDmFriendlyName());
        configSettingsMap.put(UserModifiableSetting.BASE_DOWNLOAD_FOLDER_ABSOLUTE, configSettings.getBaseDownloadFolder());
        configSettingsMap.put(UserModifiableSetting.WEB_INTERFACE_PORT_NO, configSettings.getWebInterfacePortNo());
        configSettingsMap.put(UserModifiableSetting.WEB_PROXY_HOST, configSettings.getWebProxyHost());
        configSettingsMap.put(UserModifiableSetting.WEB_PROXY_PORT, configSettings.getWebProxyPort());
        configSettingsMap.put(UserModifiableSetting.WEB_PROXY_USERNAME, configSettings.getWebProxyUsername());
        configSettingsMap.put(UserModifiableSetting.WEB_PROXY_PASSWORD, configSettings.getWebProxyPassword());

        return configSettingsMap;
    }

    // XXX: The controller should ultimately not be responsible for creating this directory if it doesn't exist
    private void createBaseDownloadFolder(String baseDownloadFolder) throws IOException {
        Path baseDownloadFolderPath = Paths.get(baseDownloadFolder);
        if(Files.exists(baseDownloadFolderPath)) {
            Files.createDirectories(baseDownloadFolderPath);
        }
    }

    @RequestMapping(method=RequestMethod.GET)
    @ResponseBody
    public ConfigResponse getConfigValues() {
        return settingsManager.buildConfigResponse();
    }

    @RequestMapping(method=RequestMethod.GET, params="action=changeSetting")
    @ResponseBody
    public CommandResponse setConfigValue(@RequestParam UserModifiableSetting settingKey, @RequestParam String settingValue) {
        UserModifiableSettingsValidator userModifiableSettingsValidator = new UserModifiableSettingsValidator();
        CommandResponseBuilder commandResponseBuilder = new CommandResponseBuilder();

        try {
            userModifiableSettingsValidator.validateSettingValue(settingKey, settingValue);
            settingsManager.setUserModifiableSetting(settingKey, settingValue);
            return commandResponseBuilder.buildCommandResponse(true, "");
        } catch (InvalidSettingValueException e) {
            return commandResponseBuilder.buildCommandResponse(false, e.getLocalizedMessage(), e.getClass().getName());
        }
    }
}