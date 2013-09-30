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
public class PauseProductDownload implements CommandMarker {
	
	private static final String MANUAL_PRODUCT_DOWNLOAD_HTTP_500_RESPONSE_PREFIX = "{\"response\":"; 
	private static final String MANUAL_PRODUCT_DOWNLOAD_HTTP_500_RESPONSE_SUFFIX = "}"; 
	private static final Logger LOGGER = Logger.getLogger(PauseProductDownload.class.getName());
		
	@CliAvailabilityIndicator({"resume"})
	public boolean isAddAvailable() {
		return true;
	}
	
	@CliCommand(value = "resume", help = "Resume a paused product download")
	public String add(
		@CliOption(key = { "uuid" }, mandatory = true, help = "The uuid of the product of interest") final String productUuid) {
		
		String returnMessage;
		try {
			String urlAsString = String.format("%s/products/%s?action=resume", ConfigurationProvider.getProperty(ConfigurationProvider.DM_WEBAPP_URL), productUuid);
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
			    	returnMessage = "Product resumed.";
			    }
			    else if (commandResponse.getMessage() != null) {
			    	returnMessage = String.format("Error: %s", commandResponse.getMessage());
			    }
			    else {
			    	returnMessage = "Error (No message provided)";
			    }
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