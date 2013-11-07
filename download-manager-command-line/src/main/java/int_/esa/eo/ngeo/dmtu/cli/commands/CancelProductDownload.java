package int_.esa.eo.ngeo.dmtu.cli.commands;

import int_.esa.eo.ngeo.dmtu.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.dmtu.cli.service.DownloadManagerResponseParser;
import int_.esa.eo.ngeo.dmtu.cli.service.DownloadManagerService;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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
public class CancelProductDownload implements CommandMarker {
	private final static String successMessage = "Product cancelled.";

	@CliAvailabilityIndicator({"cancel"})
	public boolean isCancelAvailable() {
		return true;
	}
	
	@CliCommand(value = "cancel", help = "Cancel a product download")
	public String add(
		@CliOption(key = { "uuid" }, mandatory = true, help = "The uuid of the product to cancel") final String productUuid) {
		
		DownloadManagerService downloadManagerService = new DownloadManagerService();
		DownloadManagerResponseParser downloadManagerResponseParser = new DownloadManagerResponseParser();

		String returnMessage;
		try {
			String urlAsString = String.format("%s/products/%s?action=cancel", ConfigurationProvider.getProperty(ConfigurationProvider.DM_WEBAPP_URL), productUuid);
			URL commandUrl = new URL(urlAsString);

			HttpURLConnection conn = downloadManagerService.sendGetCommand(commandUrl);
			returnMessage = downloadManagerResponseParser.parseResponse(conn, successMessage);
		} catch (IOException | ParseException e) {
			returnMessage = e.getMessage();
		}
		
		return returnMessage;
	}
		
}