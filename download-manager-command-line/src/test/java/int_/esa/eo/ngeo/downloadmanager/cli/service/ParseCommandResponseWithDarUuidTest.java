package int_.esa.eo.ngeo.downloadmanager.cli.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithDarUuid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;

public class ParseCommandResponseWithDarUuidTest {
    private DownloadManagerResponseParser downloadManagerResponseParser = new DownloadManagerResponseParser();

    @Test
    public void parseCommandResponseWithDarUuidTestNoResponseCode() throws IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(conn.getResponseCode()).thenThrow(new IOException("Mock IO Exception"));

        try {
            downloadManagerResponseParser.parseCommandResponseWithDarUuid(conn);
        } catch (ServiceException e) {
            assertEquals("java.io.IOException: Mock IO Exception", e.getMessage());
        }
    }

    @Test
    public void parseCommandResponseWithDarUuidTest200() throws IOException, ServiceException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(conn.getResponseCode()).thenReturn(200);
        URL resourceUrl = new URL("http://localhost:8082/download-manager/test");
        when(conn.getURL()).thenReturn(resourceUrl);
        String response = "{\"success\":true, \"errorMessage\":\"\", \"darUuid\": \"abcdefg\" }";
        when(conn.getInputStream()).thenReturn(new ByteArrayInputStream(response.getBytes()));

        CommandResponseWithDarUuid CommandResponseWithDarUuid = downloadManagerResponseParser.parseCommandResponseWithDarUuid(conn);
        assertTrue(CommandResponseWithDarUuid.isSuccess());
        assertEquals("", CommandResponseWithDarUuid.getErrorMessage());
        assertEquals("abcdefg", CommandResponseWithDarUuid.getDarUuid());
    }

    @Test
    public void parseCommandResponseWithDarUuidTest404() throws IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(conn.getResponseCode()).thenReturn(404);
        URL resourceUrl = new URL("http://localhost:8082/download-manager/test");
        when(conn.getURL()).thenReturn(resourceUrl);
        try {
            downloadManagerResponseParser.parseCommandResponseWithDarUuid(conn);
        }catch(ServiceException ex) {
            assertEquals(String.format("Error: The CLI's reference to the relevant Download Manager command (%s) describes a nonexistent resource", resourceUrl.toString()), ex.getMessage());
        }
    }

    @Test
    public void parseCommandResponseWithDarUuidTestIOException() throws IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        String mockIOException = "mock IO Exception";
        when(conn.getResponseCode()).thenThrow(new IOException(mockIOException));
        try {
            downloadManagerResponseParser.parseCommandResponseWithDarUuid(conn);
        }catch(ServiceException ex) {
            assertEquals("java.io.IOException: " + mockIOException, ex.getMessage());
        }
    }

    @Test
    public void parseCommandResponseWithDarUuidTest500() throws ServiceException, IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        String errorStreamContent = "{\"response\":{\"success\":false," +
                "\"errorMessage\":\"Unable to find product with UUID abc723b1-56fa-47fd-9f38-61723f20a24c. This product may have already been completed.\"," +
                "\"errorType\":\"ProductNotFoundException\"}}";
        int httpResponseCode = 500;
        when(conn.getResponseCode()).thenReturn(httpResponseCode);
        when(conn.getErrorStream()).thenReturn(new ByteArrayInputStream(errorStreamContent.getBytes()));

        try {
            downloadManagerResponseParser.parseCommandResponseWithDarUuid(conn);
        }catch(ServiceException ex) {
            String errorMessage = "Unable to find product with UUID abc723b1-56fa-47fd-9f38-61723f20a24c. This product may have already been completed.";
            assertEquals(String.format("Error: HTTP %s response code received from the Download Manager: %s", httpResponseCode, errorMessage), ex.getMessage());
        }
    }

    @Test
    public void parseCommandResponseWithDarUuidTest500UnableToParseComamndResponse() throws IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        String errorStreamContent = "{\"response\":This is not parsable JSON}";
        int httpResponseCode = 500;
        when(conn.getResponseCode()).thenReturn(httpResponseCode);
        when(conn.getErrorStream()).thenReturn(new ByteArrayInputStream(errorStreamContent.getBytes()));

        try {
            downloadManagerResponseParser.parseCommandResponseWithDarUuid(conn);
        }catch(ServiceException ex) {
            assertEquals(String.format("Error: HTTP %s response code received from the Download Manager.", httpResponseCode), ex.getMessage());
        }
    }

    @Test
    public void parseCommandResponseWithDarUuidTest500UnexpectedResponse() throws IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        String errorStreamContent = "This is not parsable JSON";
        int httpResponseCode = 500;
        when(conn.getResponseCode()).thenReturn(httpResponseCode);
        when(conn.getErrorStream()).thenReturn(new ByteArrayInputStream(errorStreamContent.getBytes()));

        try {
            downloadManagerResponseParser.parseCommandResponseWithDarUuid(conn);
        }catch(ServiceException ex) {
            assertEquals(String.format("Error: HTTP %s response code received from the Download Manager.", httpResponseCode), ex.getMessage());
        }
    }
}
