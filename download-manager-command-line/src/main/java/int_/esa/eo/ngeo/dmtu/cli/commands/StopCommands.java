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
public class StopCommands implements CommandMarker {
	private static final String TABS = "\t\t\t\t\t\t\t";
	private static final String SUCCESS_MESSAGE = "Stop command initiated. Please use the \"status\" command to monitor the progress of the products.";
	
	@CliAvailabilityIndicator({"stop"})
	public boolean isStopAvailable() {
		return true;
	}
	
	@CliCommand(value = "stop", help = "stop command for monitoring and downloads.")
	public String add(
		@CliOption(key = { "type" }, 
					mandatory = true, 
					help = "The type of stop command to send.\n"+
					TABS + "* monitoring - Stop monitoring for new downloads. All product downloads received from the monitoring service and which are not currently running will be cancelled immediately.\n" +
					TABS + "* monitoring_now - Stop monitoring for new downloads. All product downloads received from the monitoring service will be cancelled immediately.\n" +
					TABS + "* monitoring_all - Stop monitoring for new downloads. All product downloads including manual downloads will be cancelled immediately.") final String productDownloadUrl) {
		
		DownloadManagerService downloadManagerService = new DownloadManagerService();
		DownloadManagerResponseParser downloadManagerResponseParser = new DownloadManagerResponseParser();

		String returnMessage;
		try {
			String urlAsString = String.format("%s/monitoring/stop?type=%s", ConfigurationProvider.getProperty(ConfigurationProvider.DM_WEBAPP_URL), productDownloadUrl);
			URL commandUrl = new URL(urlAsString);

			HttpURLConnection conn = downloadManagerService.sendGetCommand(commandUrl);
			returnMessage = downloadManagerResponseParser.parseCommandResponse(conn, SUCCESS_MESSAGE);
		} catch (IOException | ParseException e) {
			returnMessage = e.getMessage();
		}
		
		return returnMessage;
	}
		
}