package int_.esa.eo.ngeo.dmtu.cli.service;

import int_.esa.eo.ngeo.dmtu.cli.model.CommandResponse2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class DownloadManagerResponseParser {
	private static final String MANUAL_PRODUCT_DOWNLOAD_HTTP_500_RESPONSE_PREFIX = "{\"response\":"; 
	private static final String MANUAL_PRODUCT_DOWNLOAD_HTTP_500_RESPONSE_SUFFIX = "}"; 
	private static final Logger LOGGER = Logger.getLogger(DownloadManagerResponseParser.class.getName());

	public String parseResponse(HttpURLConnection conn, String successMessage) throws IOException {
		String returnMessage;
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
		    	returnMessage = successMessage;
		    }
		    else if (commandResponse.getMessage() != null) {
		    	returnMessage = String.format("Error: %s", commandResponse.getMessage());
		    }
		    else {
		    	returnMessage = "Error (No message provided)";
		    }
			break;
		case HttpURLConnection.HTTP_NOT_FOUND:
			returnMessage = String.format("Error: The CLI's reference to the relevant Download Manager command (%s) describes a nonexistent resource", conn.getURL().toString());
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
		
		return returnMessage;
	}
}
