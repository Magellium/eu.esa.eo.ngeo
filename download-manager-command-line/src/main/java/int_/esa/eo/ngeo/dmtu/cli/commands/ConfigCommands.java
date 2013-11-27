package int_.esa.eo.ngeo.dmtu.cli.commands;

import int_.esa.eo.ngeo.dmtu.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.dmtu.cli.service.DownloadManagerResponseParser;
import int_.esa.eo.ngeo.dmtu.cli.service.DownloadManagerService;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.rest.ConfigResponse;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSettingEntry;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSettingEntry;
import int_.esa.eo.ngeo.downloadmanager.transform.JSONTransformer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

/**
 *  XXX: Consider supporting commands that will allow the user to perform run-time configuration of:
 *  <ul>
 *  	<li>DM port number</li>
 *  	<li>DM webapp context root</li>
 *  	<li>URL paths</li>
 *  </ul>
 *  Note that this might make the availability of other commands dependent on configuration having been performed.
 */
@Component
public class ConfigCommands implements CommandMarker {
	private final static String configChangeSuccessMessage = "Change of configuration successful. Use the \"config\" command to view the current config settings.";

	@CliAvailabilityIndicator({"config"})
	public boolean isConfigAvailable() {
		return true;
	}
	
	@CliCommand(value = "config", help = "View or modify Download Manager configuration.")
	public String config(
			@CliOption(key = { "settingKey" }, mandatory = false, help = "The user modifiable setting to change") final UserModifiableSetting settingKey,
			@CliOption(key = { "settingValue" }, mandatory = false, help = "The value to change the setting to.") final String settingValue) {

		DownloadManagerService downloadManagerService = new DownloadManagerService();
		DownloadManagerResponseParser downloadManagerResponseParser = new DownloadManagerResponseParser();
		try {
			if(settingKey != null && settingValue != null) {
				String urlAsString = String.format("%s/config?action=changeSetting&settingKey=%s&settingValue=%s", ConfigurationProvider.getProperty(ConfigurationProvider.DM_WEBAPP_URL), settingKey, URLEncoder.encode(settingValue, StandardCharsets.UTF_8.displayName()));
				HttpURLConnection conn = downloadManagerService.sendGetCommand(new URL(urlAsString));
				return downloadManagerResponseParser.parseCommandResponse(conn, configChangeSuccessMessage);
			}
			if(settingKey == null && settingValue == null) {
				String urlAsString = String.format("%s/config", ConfigurationProvider.getProperty(ConfigurationProvider.DM_WEBAPP_URL));
				HttpURLConnection conn = downloadManagerService.sendGetCommand(new URL(urlAsString));

				ConfigResponse configResponse = JSONTransformer.getInstance().deserialize(conn.getInputStream(), ConfigResponse.class);
			    
		    	return formatConfigOutput(configResponse);
			}
			throw new IllegalArgumentException("Both settingKey and settingValue must be supplied.");
		} catch (IOException | ParseException e) {
			return e.getMessage();
		}
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