package int_.esa.eo.ngeo.downloadmanager.cli.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.downloadmanager.cli.config.DmCliSetting;
import int_.esa.eo.ngeo.downloadmanager.cli.exception.CLICommandException;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerResponseParser;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerService;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductDownloadActionsTest {
    private ProductDownloadActions productDownloadActions;
    DownloadManagerResponseParser downloadManagerResponseParser;
    DownloadManagerService downloadManagerService;
    ConfigurationProvider configurationProvider;
    String mockProductUuid;

    @Before
    public void setup() {
        downloadManagerResponseParser = mock(DownloadManagerResponseParser.class);
        downloadManagerService = mock(DownloadManagerService.class);
        configurationProvider = mock(ConfigurationProvider.class);
        productDownloadActions = new ProductDownloadActions(downloadManagerService, downloadManagerResponseParser, configurationProvider);
        
        when(configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL)).thenReturn("http://localhost:8082/download-manager");
        mockProductUuid = UUID.randomUUID().toString();
    }
    
    @Test
    public void areCommandsAvailableTest() {
        assertTrue(productDownloadActions.isPauseAvailable());
        assertTrue(productDownloadActions.isResumeAvailable());
        assertTrue(productDownloadActions.isCancelAvailable());
    }
    
    @Test
    public void pauseTest() throws IOException, ServiceException, CLICommandException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        URL serviceUrl = new URL(String.format("http://localhost:8082/download-manager/products/%s?action=pause", mockProductUuid));
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenReturn(conn);

        CommandResponse commandResponse = new CommandResponse();
        commandResponse.setSuccess(true);
        when(downloadManagerResponseParser.parseCommandResponse(conn)).thenReturn(commandResponse);
        assertEquals("Product paused.", productDownloadActions.pause(mockProductUuid));
    }

    @Test
    public void resumeTest() throws IOException, ServiceException, CLICommandException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        URL serviceUrl = new URL(String.format("http://localhost:8082/download-manager/products/%s?action=resume", mockProductUuid));
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenReturn(conn);

        CommandResponse commandResponse = new CommandResponse();
        commandResponse.setSuccess(true);
        when(downloadManagerResponseParser.parseCommandResponse(conn)).thenReturn(commandResponse);
        assertEquals("Product resumed.", productDownloadActions.resume(mockProductUuid));
    }

    @Test
    public void cancelTest() throws IOException, ServiceException, CLICommandException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        URL serviceUrl = new URL(String.format("http://localhost:8082/download-manager/products/%s?action=cancel", mockProductUuid));
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenReturn(conn);

        CommandResponse commandResponse = new CommandResponse();
        commandResponse.setSuccess(true);
        when(downloadManagerResponseParser.parseCommandResponse(conn)).thenReturn(commandResponse);
        assertEquals("Product cancelled.", productDownloadActions.cancel(mockProductUuid));
    }
    
    @Test
    public void serviceExceptionTest() throws ServiceException, IOException {
        URL serviceUrl = new URL(String.format("http://localhost:8082/download-manager/products/%s?action=pause", mockProductUuid));
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenThrow(new ServiceException("Test ServiceException"));

        try {
            productDownloadActions.pause(mockProductUuid);
        } catch (CLICommandException e) {
            assertEquals("Unable to execute command, ServiceException: Test ServiceException", e.getMessage());
        }
    }
}
