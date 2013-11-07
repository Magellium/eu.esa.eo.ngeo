package int_.esa.eo.ngeo.dmtu.cli.commands;

import int_.esa.eo.ngeo.dmtu.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductProgress;
import int_.esa.eo.ngeo.downloadmanager.rest.StatusResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

/**
 *  XXX: Consider supporting commands that will allow the user to perform run-time configuration of:
 *  <ul>
 *  	<li>DM port number</li>
 *  	<li>DM webapp context root</li>
 *  </ul>
 *  Note that this might make the availability of other commands dependent on configuration having been performed.
 */
@Component
public class GetStatus implements CommandMarker {
	@CliAvailabilityIndicator({"status"})
	public boolean isGetStatusAvailable() {
		return true;
	}

	@CliCommand(value = "status", help = "Get the status of visible DARs")
	public String getStatus() {
		try {
			String urlAsString = String.format("%s/dataAccessRequests", ConfigurationProvider.getProperty(ConfigurationProvider.DM_WEBAPP_URL));
			URL url = new URL(urlAsString);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.addRequestProperty("Accept", "application/json");

		    ObjectMapper mapper = new ObjectMapper();
		    StatusResponse statusResponse = mapper.readValue(conn.getInputStream(), StatusResponse.class);
		    
		    List<DataAccessRequest> dataAccessRequests = statusResponse.getDataAccessRequests();
			if(dataAccessRequests.size() == 0) {
		    	return "There are currently no visible DARs.";
		    }else{
		    	return formatStatusOutput(dataAccessRequests);
		    }
		} catch (IOException e) {
			return e.getMessage();
		}
	}

	private String formatStatusOutput(List<DataAccessRequest> dataAccessRequests) {
		StringBuilder output = new StringBuilder(100);
		
		for (DataAccessRequest dataAccessRequest : dataAccessRequests) {
			if(dataAccessRequest.isVisible()) {
				output.append(dataAccessRequest.getMonitoringURL());
				output.append(": ");
				output.append(dataAccessRequest.getMonitoringStatus());
				output.append("\n\n");
				for (Product product : dataAccessRequest.getProductList()) {
					if(product.isVisible()) {
						output.append("\t");
						output.append(product.getProductAccessUrl());
						output.append(" (");
						output.append(product.getUuid());
						output.append(")");
						output.append("\n\t\t");
						
						ProductProgress productProgress = product.getProductProgress();
						output.append("Status: ");
						output.append(productProgress.getStatus());
						output.append(", ");
						output.append("Downloaded size: ");
						output.append(String.format("%,d", productProgress.getDownloadedSize()));
						output.append(" bytes, ");
						output.append("Progress: ");
						output.append(productProgress.getProgressPercentage());
						output.append("%");
						output.append("\n\n");
					}
				}
			}
		}
		
		return output.toString();
	}
}