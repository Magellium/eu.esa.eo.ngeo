package int_.esa.eo.ngeo.downloadmanager.cli.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;
import org.mockito.Mockito;

public class DownloadManagerServiceTest {
    DownloadManagerService downloadManagerService = spy(new DownloadManagerService());
    
    @Test
    public void setupConnectionTest() throws IOException {
        URL testUrl = new URL("http://test-url.com/");
        HttpURLConnection conn = downloadManagerService.setupConnection(testUrl);
        assertEquals("application/json", conn.getRequestProperty("Accept"));
        assertTrue(conn.getDoOutput());
    }
    
    @Test
    public void sendGetCommandTest() throws ServiceException, IOException {
        URL testUrl = new URL("http://test-url.com/");
        HttpURLConnection urlConnection = spy(downloadManagerService.setupConnection(testUrl));
        when(downloadManagerService.setupConnection(testUrl)).thenReturn(urlConnection);
        Mockito.doNothing().when(urlConnection).connect();
        
        HttpURLConnection returnedConnection = downloadManagerService.sendGetCommand(testUrl);
        assertEquals(urlConnection, returnedConnection);
        verify(urlConnection).connect();
    }
    
    @Test
    public void sendGetCommandIOExceptionTest() throws ServiceException, IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        URL testUrl = new URL("http://test-url.com/");
        when(downloadManagerService.setupConnection(testUrl)).thenReturn(conn);
        Mockito.doThrow(new ConnectException("Connection timed out")).when(conn).connect();
        try {
            downloadManagerService.sendGetCommand(testUrl);
        }catch(ServiceException ex) {
            assertEquals("java.net.ConnectException: Connection timed out", ex.getMessage());
        }
    }

    @Test
    public void sendPostCommandTest() throws ServiceException, IOException {
        URL testUrl = new URL("http://test-url.com/");
        String parameters = "hello=world";
        HttpURLConnection urlConnection = spy(downloadManagerService.setupConnection(testUrl));
        when(downloadManagerService.setupConnection(testUrl)).thenReturn(urlConnection);
        OutputStream out = new ByteArrayOutputStream();
        Mockito.doReturn(out).when(urlConnection).getOutputStream();
        Mockito.doNothing().when(urlConnection).connect();
        
        HttpURLConnection returnedConnection = downloadManagerService.sendPostCommand(testUrl, parameters);
        assertEquals(urlConnection, returnedConnection);
        OutputStream outputStream = urlConnection.getOutputStream();
        assertEquals("hello=world", outputStream.toString());
        verify(urlConnection).connect();
    }

    @Test
    public void sendPostCommandIOExceptionTest() throws ServiceException, IOException {
        URL testUrl = new URL("http://test-url.com/");
        String parameters = "hello=world";
        HttpURLConnection urlConnection = spy(downloadManagerService.setupConnection(testUrl));
        when(downloadManagerService.setupConnection(testUrl)).thenReturn(urlConnection);

        Mockito.doThrow(new ConnectException("Connection timed out when calling getOutputStream")).when(urlConnection).getOutputStream();
        
        try {
            downloadManagerService.sendPostCommand(testUrl, parameters);
        }catch(ServiceException ex) {
            assertEquals("java.net.ConnectException: Connection timed out when calling getOutputStream", ex.getMessage());
        }
    }
}
