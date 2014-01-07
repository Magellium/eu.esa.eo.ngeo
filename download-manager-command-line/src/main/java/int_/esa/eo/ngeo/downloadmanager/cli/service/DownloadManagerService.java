package int_.esa.eo.ngeo.downloadmanager.cli.service;

import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.stereotype.Service;

@Service
public class DownloadManagerService {

    public HttpURLConnection sendGetCommand(URL commandUrl) throws ServiceException {
        try {
            HttpURLConnection conn = setupConnection(commandUrl);
            conn.connect();

            return conn;
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    public HttpURLConnection sendPostCommand(URL commandUrl, String parameters) throws ServiceException {
        try {
            HttpURLConnection conn = setupConnection(commandUrl);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(parameters);
            writer.flush();
            writer.close();
            conn.connect();

            return conn;
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    protected HttpURLConnection setupConnection(URL commandUrl)
            throws IOException {
        HttpURLConnection conn = (HttpURLConnection)commandUrl.openConnection();
        conn.setDoOutput(true);
        conn.addRequestProperty("Accept", "application/json");
        return conn;
    }
}
