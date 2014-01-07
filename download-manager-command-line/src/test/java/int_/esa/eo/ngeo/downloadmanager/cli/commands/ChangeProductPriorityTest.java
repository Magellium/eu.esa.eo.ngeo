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
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class ChangeProductPriorityTest {
    ChangeProductPriority changeProductPriority;
    DownloadManagerResponseParser downloadManagerResponseParser;
    DownloadManagerService downloadManagerService;
    ConfigurationProvider configurationProvider;
    String mockProductUuid;
    
    @Before
    public void setup() {
        downloadManagerResponseParser = mock(DownloadManagerResponseParser.class);
        downloadManagerService = mock(DownloadManagerService.class);
        configurationProvider = mock(ConfigurationProvider.class);
        changeProductPriority = new ChangeProductPriority(downloadManagerService, downloadManagerResponseParser, configurationProvider);
        
        when(configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL)).thenReturn("http://localhost:8082/download-manager");
        mockProductUuid = UUID.randomUUID().toString();
    }
    
    @Test
    public void isCommandAvailableTest() {
        assertTrue(changeProductPriority.isCancelAvailable());
    }

    @Test
    public void stopMonitoringCommandTest() throws IOException, ServiceException, CLICommandException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        ProductPriority priority = ProductPriority.HIGH;
        URL serviceUrl = new URL(String.format("http://localhost:8082/download-manager/products/%s?action=changePriority&newPriority=%s", mockProductUuid, priority.name()));
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenReturn(conn);

        CommandResponse commandResponse = new CommandResponse();
        commandResponse.setSuccess(true);
        when(downloadManagerResponseParser.parseCommandResponse(conn)).thenReturn(commandResponse);
        assertEquals("Product priority changed.", changeProductPriority.changePriority(mockProductUuid, priority));
    }
   
    @Test
    public void serviceExceptionTest() throws ServiceException, IOException {
        ProductPriority priority = ProductPriority.HIGH;
        URL serviceUrl = new URL(String.format("http://localhost:8082/download-manager/products/%s?action=changePriority&newPriority=%s", mockProductUuid, priority.name()));
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenThrow(new ServiceException("Test ServiceException"));

        try {
            changeProductPriority.changePriority(mockProductUuid, priority);
        } catch (CLICommandException e) {
            assertEquals("Unable to execute command, ServiceException: Test ServiceException", e.getMessage());
        }
    }
}
