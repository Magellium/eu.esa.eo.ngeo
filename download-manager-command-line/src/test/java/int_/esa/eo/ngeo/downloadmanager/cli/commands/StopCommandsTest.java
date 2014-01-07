package int_.esa.eo.ngeo.downloadmanager.cli.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.cli.commands.StopCommands.StopCommandType;
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StopCommandsTest {
    StopCommands stopCommands;
    DownloadManagerResponseParser downloadManagerResponseParser;
    DownloadManagerService downloadManagerService;
    ConfigurationProvider configurationProvider;
    
    @Before
    public void setup() {
        downloadManagerResponseParser = mock(DownloadManagerResponseParser.class);
        downloadManagerService = mock(DownloadManagerService.class);
        configurationProvider = mock(ConfigurationProvider.class);
        stopCommands = new StopCommands(downloadManagerService, downloadManagerResponseParser, configurationProvider);
        when(configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL)).thenReturn("http://localhost:8082/download-manager");
    }
    
    @Test
    public void isCommandAvailableTest() {
        assertTrue(stopCommands.isStopAvailable());
    }

    @Test
    public void stopMonitoringCommandTest() throws IOException, ServiceException, CLICommandException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        URL serviceUrl = new URL("http://localhost:8082/download-manager/monitoring/stop?type=monitoring");
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenReturn(conn);

        CommandResponse commandResponse = new CommandResponse();
        commandResponse.setSuccess(true);
        when(downloadManagerResponseParser.parseCommandResponse(conn)).thenReturn(commandResponse);
        assertEquals("Stop command initiated. Please use the \"status\" command to monitor the progress of the products.", stopCommands.stop(StopCommandType.MONITORING));
    }
   
    @Test
    public void stopMonitoringNowCommandTest() throws IOException, ServiceException, CLICommandException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        URL serviceUrl = new URL("http://localhost:8082/download-manager/monitoring/stop?type=monitoring_now");
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenReturn(conn);

        CommandResponse commandResponse = new CommandResponse();
        commandResponse.setSuccess(true);
        when(downloadManagerResponseParser.parseCommandResponse(conn)).thenReturn(commandResponse);
        assertEquals("Stop command initiated. Please use the \"status\" command to monitor the progress of the products.", stopCommands.stop(StopCommandType.MONITORING_NOW));
    }

    @Test
    public void stopMonitoringAllCommandTest() throws IOException, ServiceException, CLICommandException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        URL serviceUrl = new URL("http://localhost:8082/download-manager/monitoring/stop?type=monitoring_all");
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenReturn(conn);

        CommandResponse commandResponse = new CommandResponse();
        commandResponse.setSuccess(true);
        when(downloadManagerResponseParser.parseCommandResponse(conn)).thenReturn(commandResponse);
        assertEquals("Stop command initiated. Please use the \"status\" command to monitor the progress of the products.", stopCommands.stop(StopCommandType.MONITORING_ALL));
    }

    @Test
    public void serviceExceptionTest() throws ServiceException, IOException {
        URL serviceUrl = new URL("http://localhost:8082/download-manager/monitoring/stop?type=monitoring_all");
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenThrow(new ServiceException("Test ServiceException"));

        try {
            stopCommands.stop(StopCommandType.MONITORING_ALL);
        } catch (CLICommandException e) {
            assertEquals("Unable to execute command, ServiceException: Test ServiceException", e.getMessage());
        }
    }
}
