package int_.esa.eo.ngeo.dmtu.cli.commands;

import int_.esa.eo.ngeo.dmtu.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.dmtu.controller.CommandResponse2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.log4j.Logger;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
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
	
	private static final String MANUAL_PRODUCT_DOWNLOAD_HTTP_500_RESPONSE_PREFIX = "{\"response\":"; 
	private static final String MANUAL_PRODUCT_DOWNLOAD_HTTP_500_RESPONSE_SUFFIX = "}"; 
	private static final Logger LOGGER = Logger.getLogger(StopCommands.class.getName());
	private static final String TABS = "\t\t\t\t\t\t\t";	
	
	@CliAvailabilityIndicator({"stop"})
	public boolean isAddAvailable() {
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
		
		String returnMessage;
		try {
			String urlAsString = String.format("%s/monitoring/stop?type=%s", ConfigurationProvider.getProperty(ConfigurationProvider.DM_WEBAPP_URL), productDownloadUrl);
			URL url = new URL(urlAsString);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setDoOutput(true);
			conn.addRequestProperty("Accept", "application/json");
			
			CommandResponse2 commandResponse;
		    ObjectMapper mapper = new ObjectMapper();
			final int httpResponseCode = conn.getResponseCode();
			LOGGER.debug("HTTP response code = " + httpResponseCode);
			switch (httpResponseCode) {
			case HttpURLConnection.HTTP_OK:
				
				BufferedReader streamReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8")); 
			    StringBuilder responseStrBuilder = new StringBuilder();

			    String inputStr;
			    while ((inputStr = streamReader.readLine()) != null) {
			        responseStrBuilder.append(inputStr);
			    }
			    streamReader.close();
			    
			    LOGGER.debug("JSON = " + responseStrBuilder.toString());
			    
			    commandResponse = mapper.readValue(responseStrBuilder.toString(), CommandResponse2.class);
			    if (commandResponse.isSuccess()) {
			    	returnMessage = "Stop command sent. Please use the \"status\" command to monitor the progress of the product downloads.";
			    }
			    else if (commandResponse.getMessage() != null) {
			    	returnMessage = String.format("Error: %s", commandResponse.getMessage());
			    }
			    else {
			    	returnMessage = "Error (No message provided)";
			    }
			    //returnMessage = commandResponse.isSuccess() ? "Added" : (commandResponse.getMessage() == null ? "Error (No message provided)" : String.format("Error: %s", commandResponse.getMessage()));
				break;
			case HttpURLConnection.HTTP_NOT_FOUND:
				returnMessage = String.format("Error: The CLI's reference to the relevant Download Manager command (%s) describes a nonexistent resource", urlAsString);
				break;
			default:
				String errorStreamContent = IOUtils.toString(conn.getErrorStream());
				LOGGER.debug("JSON = " + errorStreamContent);
				int unTrimmedLength = errorStreamContent.length();
				
				// XXX: Here follows a hacky way of dealing with the fact that, relative to the HTTP 200 scenario, the server's JSON responses are wrapped. 
				String trimmedErrorStreamContent = errorStreamContent.substring(MANUAL_PRODUCT_DOWNLOAD_HTTP_500_RESPONSE_PREFIX.length(),
																				unTrimmedLength - MANUAL_PRODUCT_DOWNLOAD_HTTP_500_RESPONSE_SUFFIX.length());
			    
			    commandResponse = mapper.readValue(trimmedErrorStreamContent, CommandResponse2.class);
				returnMessage = String.format("Error: HTTP %s response code received from the Download Manager: %s", httpResponseCode, commandResponse.getMessage());
			}
		}
		catch (IOException e) {
			returnMessage = e.getMessage();
		}
		
		return returnMessage;
	}
		
}