package int_.esa.eo.ngeo.downloadmanager.cli.commands;

import int_.esa.eo.ngeo.downloadmanager.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.downloadmanager.cli.config.DmCliSetting;
import int_.esa.eo.ngeo.downloadmanager.cli.exception.CLICommandException;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerResponseParser;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerService;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;
import int_.esa.eo.ngeo.downloadmanager.rest.ConfigResponse;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSettingEntry;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSettingEntry;
import int_.esa.eo.ngeo.downloadmanager.transform.JSONTransformer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class ConfigCommands extends ActionWithCommandResponse implements CommandMarker {
    private static final String SUCCESS_MESSAGE = "Change of configuration successful. Use the \"config\" command to view the current config settings.";
    private DownloadManagerService downloadManagerService;
    private DownloadManagerResponseParser downloadManagerResponseParser;
    private ConfigurationProvider configurationProvider;

    public ConfigCommands(DownloadManagerService downloadManagerService, DownloadManagerResponseParser downloadManagerResponseParser, ConfigurationProvider configurationProvider) {
        this.downloadManagerService = downloadManagerService;
        this.downloadManagerResponseParser = downloadManagerResponseParser;
        this.configurationProvider = configurationProvider;
    }

    @CliAvailabilityIndicator({"config"})
    public boolean isConfigAvailable() {
        return true;
    }

    @CliCommand(value = "config", help = "View or modify Download Manager configuration.")
    public String config(
            @CliOption(key = { "settingKey" }, mandatory = false, help = "The user modifiable setting to change") final UserModifiableSetting settingKey,
            @CliOption(key = { "settingValue" }, mandatory = false, help = "The value to change the setting to.") final String settingValue) {

        if(settingKey != null && settingValue != null) {
            return setConfigParam(settingKey, settingValue);
        }
        if(settingKey == null && settingValue == null) {
            return getConfigParams();
        }
        throw new CLICommandException("Both settingKey and settingValue must be supplied.");
    }

    private String getConfigParams() {
        String urlAsString = String.format("%s/config", configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL));
        ConfigResponse configResponse;
        try {
            HttpURLConnection conn = downloadManagerService.sendGetCommand(new URL(urlAsString));
            configResponse = new JSONTransformer().deserialize(conn.getInputStream(), ConfigResponse.class);
        }catch(IOException | ServiceException e) {
            throw new CLICommandException(e);
        }
        return formatConfigOutput(configResponse);
    }

    private String setConfigParam(UserModifiableSetting settingKey, String settingValue) {
        CommandResponse commandResponse;
        try {
            String urlAsString = String.format("%s/config?action=changeSetting&settingKey=%s&settingValue=%s", configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL), settingKey, URLEncoder.encode(settingValue, StandardCharsets.UTF_8.displayName()));
            HttpURLConnection conn = downloadManagerService.sendGetCommand(new URL(urlAsString));
            commandResponse = downloadManagerResponseParser.parseCommandResponse(conn);
        }catch(MalformedURLException | UnsupportedEncodingException | ServiceException e) {
            throw new CLICommandException(e);
        }
        return getMessageFromCommandResponse(commandResponse, SUCCESS_MESSAGE);
    }

    private String formatConfigOutput(ConfigResponse configResponse) {
        StringBuilder output = new StringBuilder(100);

        output.append("User modifiable settings:\n");
        for (UserModifiableSettingEntry userModifiableSettingEntry : configResponse.getUserModifiableSettingEntries()) {
            output.append("\t");
            output.append(userModifiableSettingEntry.getKey());
            output.append("=");
            output.append(userModifiableSettingEntry.getValue());
            output.append("\n");
        }
        output.append("\n");
        output.append("Non-user modifiable settings:\n");
        for (NonUserModifiableSettingEntry nonUserModifiableSettingEntry : configResponse.getNonUserModifiableSettingEntries()) {
            output.append("\t");
            output.append(nonUserModifiableSettingEntry.getKey());
            output.append("=");
            output.append(nonUserModifiableSettingEntry.getValue());
            output.append("\n");
        }

        return output.toString();
    }
}