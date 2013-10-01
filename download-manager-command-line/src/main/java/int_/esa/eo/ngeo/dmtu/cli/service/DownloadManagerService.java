package int_.esa.eo.ngeo.dmtu.cli.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadManagerService {

	public HttpURLConnection sendCommand(URL commandUrl) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)commandUrl.openConnection();
		conn.setDoOutput(true);
		conn.addRequestProperty("Accept", "application/json");
		conn.connect();

		return conn;
	}
}
