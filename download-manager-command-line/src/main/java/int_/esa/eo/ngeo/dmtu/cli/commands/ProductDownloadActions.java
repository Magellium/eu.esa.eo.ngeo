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
public class ProductDownloadActions implements CommandMarker {
	@CliAvailabilityIndicator({"resume"})
	public boolean isResumeAvailable() {
		return true;
	}
	
	@CliAvailabilityIndicator({"cancel"})
	public boolean isCancelAvailable() {
		return true;
	}

	@CliAvailabilityIndicator({"pause"})
	public boolean isPauseAvailable() {
		return true;
	}

	@CliCommand(value = "resume", help = "Resume a paused product download")
	public String resume(
		@CliOption(key = { "uuid" }, mandatory = true, help = "The uuid of the product to resume") final String productUuid) {

		return sendProductAction(ProductAction.RESUME, productUuid);
	}
		
	@CliCommand(value = "pause", help = "Pause a product download")
	public String pause(
		@CliOption(key = { "uuid" }, mandatory = true, help = "The uuid of the product to pause") final String productUuid) {
		
		return sendProductAction(ProductAction.PAUSE, productUuid);
	}

	@CliCommand(value = "cancel", help = "Cancel a product download")
	public String cancel(
		@CliOption(key = { "uuid" }, mandatory = true, help = "The uuid of the product to cancel") final String productUuid) {
		
		return sendProductAction(ProductAction.CANCEL, productUuid);
	}
	
	private String sendProductAction(ProductAction productAction, String productUuid) {
		DownloadManagerService downloadManagerService = new DownloadManagerService();
		DownloadManagerResponseParser downloadManagerResponseParser = new DownloadManagerResponseParser();

		try {
			String urlAsString = String.format("%s/products/%s?action=%s", ConfigurationProvider.getProperty(ConfigurationProvider.DM_WEBAPP_URL), productUuid, productAction.getActionString());
			URL commandUrl = new URL(urlAsString);

			HttpURLConnection conn = downloadManagerService.sendGetCommand(commandUrl);
			return downloadManagerResponseParser.parseCommandResponse(conn, productAction.getSuccessMessage());
		} catch (IOException | ParseException e) {
			return e.getMessage();
		}		
	}
	
	enum ProductAction {
		PAUSE ("pause", "Product paused."),
		RESUME("resume", "Product resumed."),
		CANCEL("cancel", "Product cancelled.");
		
		private final String actionString, successMessage;
		
		ProductAction(String actionString, String successMessage) {
			this.actionString = actionString;
			this.successMessage = successMessage;
		}

		public String getActionString() {
			return actionString;
		}

		public String getSuccessMessage() {
			return successMessage;
		}
	}
}