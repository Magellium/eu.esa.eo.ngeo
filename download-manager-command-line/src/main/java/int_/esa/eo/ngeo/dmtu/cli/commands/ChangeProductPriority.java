package int_.esa.eo.ngeo.dmtu.cli.commands;

import int_.esa.eo.ngeo.dmtu.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.dmtu.cli.service.DownloadManagerResponseParser;
import int_.esa.eo.ngeo.dmtu.cli.service.DownloadManagerService;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;

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
public class ChangeProductPriority implements CommandMarker {
	private final static String successMessage = "Product priority changed.";

	@CliAvailabilityIndicator({"change-priority"})
	public boolean isCancelAvailable() {
		return true;
	}
	
	@CliCommand(value = "change-priority", help = "Change the priority of a product download")
	public String add(
		@CliOption(key = { "uuid" }, mandatory = true, help = "The uuid of the product to change priority") final String productUuid,
		@CliOption(key = { "priority" }, mandatory = true, help = "The priority to change this product to") final ProductPriority newPriority) {
		
		DownloadManagerService downloadManagerService = new DownloadManagerService();
		DownloadManagerResponseParser downloadManagerResponseParser = new DownloadManagerResponseParser();

		String returnMessage;
		try {
			String urlAsString = String.format("%s/products/%s?action=changePriority&newPriority=%s", ConfigurationProvider.getProperty(ConfigurationProvider.DM_WEBAPP_URL), productUuid, newPriority.name());
			URL commandUrl = new URL(urlAsString);

			HttpURLConnection conn = downloadManagerService.sendGetCommand(commandUrl);
			returnMessage = downloadManagerResponseParser.parseCommandResponse(conn, successMessage);
		} catch (IOException | ParseException e) {
			returnMessage = e.getMessage();
		}
		
		return returnMessage;
	}
		
}