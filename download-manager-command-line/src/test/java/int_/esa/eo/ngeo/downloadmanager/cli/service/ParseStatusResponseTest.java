package int_.esa.eo.ngeo.downloadmanager.cli.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.rest.StatusResponse;
import int_.esa.eo.ngeo.downloadmanager.transform.JSONTransformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;

public class ParseStatusResponseTest {
    private DownloadManagerResponseParser downloadManagerResponseParser = new DownloadManagerResponseParser();

    @Test
    public void parseStatusResponseTestNoResponseCode() throws IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(conn.getResponseCode()).thenThrow(new IOException("Mock IO Exception"));

        try {
            downloadManagerResponseParser.parseStatusResponse(conn);
        } catch (ServiceException e) {
            assertEquals("java.io.IOException: Mock IO Exception", e.getMessage());
        }
    }

    @Test
    public void parseStatusResponseTest200() throws IOException, ServiceException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(conn.getResponseCode()).thenReturn(200);
        URL resourceUrl = new URL("http://localhost:8082/download-manager/test");
        when(conn.getURL()).thenReturn(resourceUrl);
        String response = "{\"dataAccessRequests\":[{\"uuid\":\"6bc2d50f-4ef1-4f89-8846-412ba5cb04d1\",\"darURL\":\"Manual Data Access Request\",\"monitoringStatus\":\"IN_PROGRESS\",\"monitored\":false,\"visible\":true,\"productList\":[{\"uuid\":\"f01fa8a7-bef5-413a-a3c3-a23d3583b348\",\"productAccessUrl\":\"http://ipv4.download.thinkbroadband.com/50MB.zip\",\"completedDownloadPath\":\"C:\\\\Users\\\\lkn\\\\ngEO-Downloads\\\\50MB.zip\",\"totalFileSize\":52428800,\"notified\":false,\"numberOfFiles\":1,\"productName\":\"50MB.zip\",\"priority\":\"Normal\",\"creationTimestamp\":1387472363283,\"startOfFirstDownloadRequest\":1387472363370,\"startOfActualDownload\":1387472363374,\"stopOfDownload\":1387472421147,\"pausedByDownloadManager\":false,\"visible\":true,\"productProgress\":{\"downloadedSize\":52428800,\"progressPercentage\":100,\"status\":\"COMPLETED\"}}]}]}";
        when(conn.getInputStream()).thenReturn(new ByteArrayInputStream(response.getBytes()));

        StatusResponse statusResponse = downloadManagerResponseParser.parseStatusResponse(conn);
        assertEquals(1, statusResponse.getDataAccessRequests().size());
        DataAccessRequest dataAccessRequest = statusResponse.getDataAccessRequests().get(0);
        assertEquals("6bc2d50f-4ef1-4f89-8846-412ba5cb04d1", dataAccessRequest.getUuid());
        assertEquals("Manual Data Access Request", dataAccessRequest.getDarURL());
        assertEquals(1, dataAccessRequest.getProductList().size());
    }

    @Test
    public void parseStatusResponseTest500() throws ServiceException, IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        String errorStreamContent = "{\"response\":{\"dataAccessRequests\":[]}}";
        int httpResponseCode = 500;
        when(conn.getResponseCode()).thenReturn(httpResponseCode);
        when(conn.getErrorStream()).thenReturn(new ByteArrayInputStream(errorStreamContent.getBytes()));

        try {
            downloadManagerResponseParser.parseStatusResponse(conn);
        }catch(ServiceException ex) {
            assertEquals(String.format("Error: HTTP %s response code received from the Download Manager.", httpResponseCode), ex.getMessage());
        }
    }

    @Test
    public void parseStatusResponseTest500UnableToParse() throws IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        String errorStreamContent = "{\"response\":This is not parsable JSON}";
        int httpResponseCode = 500;
        when(conn.getResponseCode()).thenReturn(httpResponseCode);
        when(conn.getErrorStream()).thenReturn(new ByteArrayInputStream(errorStreamContent.getBytes()));

        try {
            downloadManagerResponseParser.parseStatusResponse(conn);
        }catch(ServiceException ex) {
            assertEquals(String.format("Error: HTTP %s response code received from the Download Manager.", httpResponseCode), ex.getMessage());
        }
    }

    @Test
    public void parseStatusResponseTest500UnexpectedResponse() throws IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        String errorStreamContent = "This is not parsable JSON";
        int httpResponseCode = 500;
        when(conn.getResponseCode()).thenReturn(httpResponseCode);
        when(conn.getErrorStream()).thenReturn(new ByteArrayInputStream(errorStreamContent.getBytes()));

        try {
            downloadManagerResponseParser.parseStatusResponse(conn);
        }catch(ServiceException ex) {
            assertEquals(String.format("Error: HTTP %s response code received from the Download Manager.", httpResponseCode), ex.getMessage());
        }
    }

    @Test
    public void parseStatusResponseTest404() throws IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(conn.getResponseCode()).thenReturn(404);
        URL resourceUrl = new URL("http://localhost:8082/download-manager/test");
        when(conn.getURL()).thenReturn(resourceUrl);

        try {
            downloadManagerResponseParser.parseStatusResponse(conn);
        }catch(ServiceException ex) {
            assertEquals(String.format("Error: The CLI's reference to the relevant Download Manager command (%s) describes a nonexistent resource", resourceUrl.toString()), ex.getMessage());
        }
    }

    @Test
    public void getJsonTransformerTest() {
        JSONTransformer jsonTransformer = downloadManagerResponseParser.getJsonTransformer();
        assertEquals(jsonTransformer, downloadManagerResponseParser.getJsonTransformer());
    }
}
