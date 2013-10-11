package int_.esa.eo.ngeo.dmtu.cli.service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadManagerService {

	public HttpURLConnection sendGetCommand(URL commandUrl) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)commandUrl.openConnection();
		conn.setDoOutput(true);
		conn.addRequestProperty("Accept", "application/json");
		conn.connect();

		return conn;
	}

	public HttpURLConnection sendPostCommand(URL commandUrl, String parameters) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)commandUrl.openConnection();
		conn.setDoOutput(true);
		conn.addRequestProperty("Accept", "application/json");
		
		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
		writer.write(parameters);
		writer.flush();
		writer.close();
		conn.connect();

		return conn;
	}
}
