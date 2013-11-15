package int_.esa.eo.ngeo.dmtu.cli.commands;

import int_.esa.eo.ngeo.dmtu.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.dmtu.cli.service.DownloadManagerResponseParser;
import int_.esa.eo.ngeo.dmtu.cli.service.DownloadManagerService;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

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
public class AddProductDownload implements CommandMarker {
	private final static String successMessage = "Added. Please use the \"status\" command to monitor the progress.";
	
	@CliAvailabilityIndicator({"addProduct"})
	public boolean isAddAvailable() {
		return true;
	}
	
	@CliCommand(value = "addProduct", help = "Manually add a product")
	public String add(
		@CliOption(key = { "url" }, mandatory = true, help = "URL of a single product to be downloaded") final String productDownloadUrl) { 
		
		DownloadManagerService downloadManagerService = new DownloadManagerService();
		DownloadManagerResponseParser downloadManagerResponseParser = new DownloadManagerResponseParser();

		String returnMessage;
		try {
			String urlAsString = String.format("%s/download", ConfigurationProvider.getProperty(ConfigurationProvider.DM_WEBAPP_URL));
			URL commandUrl = new URL(urlAsString);
			String parameters = null;
			parameters = String.format("productDownloadUrl=%s", URLEncoder.encode(productDownloadUrl, "UTF-8"));
			
			HttpURLConnection conn = downloadManagerService.sendPostCommand(commandUrl, parameters);
			returnMessage = downloadManagerResponseParser.parseCommandResponse(conn, successMessage);
		} catch (IOException | ParseException e) {
			returnMessage = e.getMessage();
		}
		
		return returnMessage;
	}
		
}