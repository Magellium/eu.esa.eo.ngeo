package int_.esa.eo.ngeo.dmtu.cli.commands;

import int_.esa.eo.ngeo.dmtu.cli.config.ConfigurationProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.JsonPath;

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
	public boolean isAddAvailable() {
		return true;
	}

	@CliCommand(value = "status", help = "Get the status of visible DARs")
	public String getStatus() {
		String returnMessage;
		try {
			String urlAsString = String.format("%s/dataAccessRequests", ConfigurationProvider.getProperty(ConfigurationProvider.DM_WEBAPP_URL));
			URL url = new URL(urlAsString);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.addRequestProperty("Accept", "application/json");

			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder jsonBuilder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				jsonBuilder.append(line + '\n');
			}
			
			String jsonString = jsonBuilder.toString();			
			JSONArray dars = JsonPath.read(jsonString, "$.[*]");

			returnMessage = dars.size() == 0 ? "There are currently no visible DARs." : convertFromJSON(dars);
		} catch (IOException e) {
			returnMessage = e.getMessage();		
		}
		return returnMessage;
	}

	private String convertFromJSON(JSONArray dars) {
		StringBuilder sb = new StringBuilder(100);
		
		for (int i=0; i < dars.size(); i++) {
			JSONObject dar = (JSONObject) dars.get(i);
			sb.append(dar.get("monitoringURL"));
			sb.append(": ");
			sb.append(dar.get("monitoringStatus"));
			sb.append("\n\n");
			JSONArray productListJSON = (JSONArray) dar.get("productList");
			for (int j=0; j< productListJSON.size(); j++) {
				JSONObject product = (JSONObject)productListJSON.get(j);
				sb.append("\t");
				sb.append(product.get("productAccessUrl"));
				sb.append(" (");
				sb.append(product.get("uuid"));
				sb.append(")");
				sb.append("\n\t\t");
				
				JSONObject productProgress = (JSONObject) product.get("productProgress");
				sb.append("Status: ");
				sb.append(productProgress.get("status"));
				sb.append(", ");
				sb.append("Downloaded size: ");
				sb.append(String.format("%,d", productProgress.get("downloadedSize")));
				sb.append(" bytes, ");
				sb.append("Progress: ");
				sb.append(productProgress.get("progressPercentage"));
				sb.append("%");
				sb.append("\n\n");
			}
		}
		
		return sb.toString();
	}

}