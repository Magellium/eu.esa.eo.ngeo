package int_.esa.eo.ngeo.dmtu.cli.service;

import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;
import int_.esa.eo.ngeo.downloadmanager.transform.JSONTransformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class DownloadManagerResponseParser {
	private static final String MANUAL_PRODUCT_DOWNLOAD_HTTP_500_RESPONSE_PREFIX = "{\"response\":"; 
	private static final String MANUAL_PRODUCT_DOWNLOAD_HTTP_500_RESPONSE_SUFFIX = "}"; 
	private static final Logger LOGGER = Logger.getLogger(DownloadManagerResponseParser.class.getName());

	public String parseCommandResponse(HttpURLConnection conn, String successMessage) throws IOException, ParseException {
		String returnMessage;
		CommandResponse commandResponse;
		final int httpResponseCode = conn.getResponseCode();
		LOGGER.debug("HTTP response code = " + httpResponseCode);
		switch (httpResponseCode) {
		case HttpURLConnection.HTTP_OK:
			commandResponse = JSONTransformer.getInstance().deserialize(conn.getInputStream(), CommandResponse.class);
		    if (commandResponse.isSuccess()) {
		    	returnMessage = successMessage;
		    }
		    else if (commandResponse.getErrorMessage() != null) {
		    	returnMessage = String.format("Error: %s", commandResponse.getErrorMessage());
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
		    
		    commandResponse = JSONTransformer.getInstance().deserialize(new ByteArrayInputStream(trimmedErrorStreamContent.getBytes(StandardCharsets.UTF_8)), CommandResponse.class);
			returnMessage = String.format("Error: HTTP %s response code received from the Download Manager: %s", httpResponseCode, commandResponse.getErrorMessage());
		}
		
		return returnMessage;
	}
}
