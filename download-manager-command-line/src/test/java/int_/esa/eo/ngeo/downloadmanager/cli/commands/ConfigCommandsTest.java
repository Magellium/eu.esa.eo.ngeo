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
import int_.esa.eo.ngeo.downloadmanager.settings.UserModifiableSetting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigCommandsTest {
    ConfigCommands configCommands;
    DownloadManagerResponseParser downloadManagerResponseParser;
    DownloadManagerService downloadManagerService;
    ConfigurationProvider configurationProvider;

    @Before
    public void setup() {
        downloadManagerResponseParser = mock(DownloadManagerResponseParser.class);
        downloadManagerService = mock(DownloadManagerService.class);
        configurationProvider = mock(ConfigurationProvider.class);
        configCommands = new ConfigCommands(downloadManagerService, downloadManagerResponseParser, configurationProvider);
        when(configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL)).thenReturn("http://localhost:8082/download-manager");
    }
    
    @Test
    public void isConfigAvailableTest() {
        assertTrue(configCommands.isConfigAvailable());
    }

    @Test
    public void configTestSettingKeyOnly() {
        try {
            configCommands.config(UserModifiableSetting.DM_FRIENDLY_NAME, null);
        } catch (CLICommandException e) {
            assertEquals("Both settingKey and settingValue must be supplied.", e.getMessage());
        }
    }

    @Test
    public void configTestSettingValueOnly() {
        try {
            configCommands.config(null, "testValue");
        } catch (CLICommandException e) {
            assertEquals("Both settingKey and settingValue must be supplied.", e.getMessage());
        }
    }

    @Test
    public void viewConfigTestServiceException() throws MalformedURLException, ServiceException {
        when(downloadManagerService.sendGetCommand(new URL("http://localhost:8082/download-manager/config"))).thenThrow(new ServiceException("Test ServiceException"));

        try {
            configCommands.config(null, null);
        } catch (CLICommandException e) {
            assertEquals("Unable to execute command, ServiceException: Test ServiceException", e.getMessage());
        }
    }
        
    @Test
    public void viewConfigTest() throws CLICommandException, ServiceException, IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(downloadManagerService.sendGetCommand(new URL("http://localhost:8082/download-manager/config"))).thenReturn(conn);

        String validResponse = "{\"userModifiableSettingEntries\":[{\"key\":\"SSO_USERNAME\",\"value\":\"ngeo\"},{\"key\":\"NO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS\",\"value\":\"5\"},{\"key\":\"WEB_INTERFACE_PORT_NO\",\"value\":\"8082\"},{\"key\":\"DM_FRIENDLY_NAME\",\"value\":\"Download Manager\"},{\"key\":\"WEB_PROXY_USERNAME\",\"value\":\"\"},{\"key\":\"PRODUCT_DOWNLOAD_COMPLETE_COMMAND\",\"value\":\"\"},{\"key\":\"WEB_INTERFACE_PASSWORD\",\"value\":\"\"},{\"key\":\"WEB_PROXY_PASSWORD\",\"value\":\"\"},{\"key\":\"BASE_DOWNLOAD_FOLDER_ABSOLUTE\",\"value\":\"C:\\\\Users\\\\lkn\\\\ngEO-Downloads\\\\\"},{\"key\":\"SSO_PASSWORD\",\"value\":\"Z2Vnd2VlZ3dlZw==\"},{\"key\":\"WEB_PROXY_HOST\",\"value\":\"localhost\"},{\"key\":\"WEB_INTERFACE_REMOTE_ACCESS_ENABLED\",\"value\":\"false\"},{\"key\":\"WEB_PROXY_PORT\",\"value\":\"8888\"},{\"key\":\"WEB_INTERFACE_USERNAME\",\"value\":\"\"}],\"nonUserModifiableSettingEntries\":[{\"key\":\"DM_ID\",\"value\":\"bae945c9e4474f3ca4245effd6a67eac\"},{\"key\":\"DIR_PLUGINS\",\"value\":\"plugins\"},{\"key\":\"NGEO_MONITORING_SERVICE_SET_TIME\",\"value\":\"2013-12-20T14:33:26 GMT\"},{\"key\":\"IICD_D_WS_REFRESH_PERIOD\",\"value\":\"20\"},{\"key\":\"NGEO_WEB_SERVER_DAR_MONITORING_URLS\",\"value\":\"http://localhost:8080/download-manager-mock-web-server/monitoringservice\"},{\"key\":\"NGEO_WEB_SERVER_REGISTRATION_URLS\",\"value\":\"http://localhost:8080/download-manager-mock-web-server/register\"},{\"key\":\"DM_IS_SETUP\",\"value\":\"true\"},{\"key\":\"NGEO_WEB_SERVER_NON_UMSSO_LOGIN_URL\",\"value\":\"\"},{\"key\":\"DM_IS_REGISTERED\",\"value\":\"true\"}]}";
        when(conn.getInputStream()).thenReturn(new ByteArrayInputStream(validResponse.getBytes()));

        StringBuilder expectedOutput = new StringBuilder(100);
        expectedOutput.append("User modifiable settings:\n");
        expectedOutput.append("\tSSO_USERNAME=ngeo\n");
        expectedOutput.append("\tNO_OF_PARALLEL_PRODUCT_DOWNLOAD_THREADS=5\n");
        expectedOutput.append("\tWEB_INTERFACE_PORT_NO=8082\n");
        expectedOutput.append("\tDM_FRIENDLY_NAME=Download Manager\n");
        expectedOutput.append("\tWEB_PROXY_USERNAME=\n");
        expectedOutput.append("\tPRODUCT_DOWNLOAD_COMPLETE_COMMAND=\n");
        expectedOutput.append("\tWEB_INTERFACE_PASSWORD=\n");
        expectedOutput.append("\tWEB_PROXY_PASSWORD=\n");
        expectedOutput.append("\tBASE_DOWNLOAD_FOLDER_ABSOLUTE=C:\\Users\\lkn\\ngEO-Downloads\\\n");
        expectedOutput.append("\tSSO_PASSWORD=Z2Vnd2VlZ3dlZw==\n");
        expectedOutput.append("\tWEB_PROXY_HOST=localhost\n");
        expectedOutput.append("\tWEB_INTERFACE_REMOTE_ACCESS_ENABLED=false\n");
        expectedOutput.append("\tWEB_PROXY_PORT=8888\n");
        expectedOutput.append("\tWEB_INTERFACE_USERNAME=\n\n");
        expectedOutput.append("Non-user modifiable settings:\n");
        expectedOutput.append("\tDM_ID=bae945c9e4474f3ca4245effd6a67eac\n");
        expectedOutput.append("\tDIR_PLUGINS=plugins\n");
        expectedOutput.append("\tNGEO_MONITORING_SERVICE_SET_TIME=2013-12-20T14:33:26 GMT\n");
        expectedOutput.append("\tIICD_D_WS_REFRESH_PERIOD=20\n");
        expectedOutput.append("\tNGEO_WEB_SERVER_DAR_MONITORING_URLS=http://localhost:8080/download-manager-mock-web-server/monitoringservice\n");
        expectedOutput.append("\tNGEO_WEB_SERVER_REGISTRATION_URLS=http://localhost:8080/download-manager-mock-web-server/register\n");
        expectedOutput.append("\tDM_IS_SETUP=true\n");
        expectedOutput.append("\tNGEO_WEB_SERVER_NON_UMSSO_LOGIN_URL=\n");
        expectedOutput.append("\tDM_IS_REGISTERED=true\n");

        assertEquals(expectedOutput.toString(), configCommands.config(null, null));
    }

    @Test
    public void viewEmptyConfigTest() throws CLICommandException, ServiceException, IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(downloadManagerService.sendGetCommand(new URL("http://localhost:8082/download-manager/config"))).thenReturn(conn);

        String validResponse = "{\"userModifiableSettingEntries\":[],\"nonUserModifiableSettingEntries\":[]}";
        when(conn.getInputStream()).thenReturn(new ByteArrayInputStream(validResponse.getBytes()));

        StringBuilder expectedOutput = new StringBuilder(100);
        expectedOutput.append("User modifiable settings:\n\n");
        expectedOutput.append("Non-user modifiable settings:\n");

        assertEquals(expectedOutput.toString(), configCommands.config(null, null));
    }
    
    @Test
    public void setConfigEntrySucccessTest() throws ServiceException, CLICommandException, IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        UserModifiableSetting settingKey = UserModifiableSetting.DM_FRIENDLY_NAME;
        String settingValue = "Download Manager";
        URL serviceUrl = new URL(String.format("http://localhost:8082/download-manager/config?action=changeSetting&settingKey=%s&settingValue=%s", settingKey.toString(), URLEncoder.encode(settingValue, StandardCharsets.UTF_8.displayName())));
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenReturn(conn);

        CommandResponse commandResponse = new CommandResponse();
        commandResponse.setSuccess(true);
        when(downloadManagerResponseParser.parseCommandResponse(conn)).thenReturn(commandResponse);
        
        assertEquals("Change of configuration successful. Use the \"config\" command to view the current config settings.", configCommands.config(settingKey, settingValue));
    }

    @Test
    public void setConfigEntryFailureWithErrorMessageTest() throws ServiceException, CLICommandException, IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        UserModifiableSetting settingKey = UserModifiableSetting.DM_FRIENDLY_NAME;
        String settingValue = "d";
        URL serviceUrl = new URL(String.format("http://localhost:8082/download-manager/config?action=changeSetting&settingKey=%s&settingValue=%s", settingKey.toString(), URLEncoder.encode(settingValue, StandardCharsets.UTF_8.displayName())));
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenReturn(conn);

        CommandResponse commandResponse = new CommandResponse();
        commandResponse.setSuccess(false);
        String errorMessage = "Invalid value 'd' for setting DM_FRIENDLY_NAME \\n\\tName of Download Manager instance must be 3 - 100 characters long";
        commandResponse.setErrorMessage(errorMessage);
        commandResponse.setErrorType("int_.esa.eo.ngeo.downloadmanager.exception.InvalidSettingValueException");
        when(downloadManagerResponseParser.parseCommandResponse(conn)).thenReturn(commandResponse);
        
        try {
            configCommands.config(settingKey, settingValue);
        }catch(CLICommandException ex) {
            assertEquals("Error from Download Manager: " + errorMessage, ex.getMessage());
        }
    }

    @Test
    public void setConfigEntryFailureNoErrorMessageTest() throws ServiceException, CLICommandException, IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        UserModifiableSetting settingKey = UserModifiableSetting.DM_FRIENDLY_NAME;
        String settingValue = "d";
        URL serviceUrl = new URL(String.format("http://localhost:8082/download-manager/config?action=changeSetting&settingKey=%s&settingValue=%s", settingKey.toString(), URLEncoder.encode(settingValue, StandardCharsets.UTF_8.displayName())));
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenReturn(conn);

        CommandResponse commandResponse = new CommandResponse();
        commandResponse.setSuccess(false);
        commandResponse.setErrorMessage("");
        commandResponse.setErrorType("int_.esa.eo.ngeo.downloadmanager.exception.InvalidSettingValueException");
        when(downloadManagerResponseParser.parseCommandResponse(conn)).thenReturn(commandResponse);
        
        try {
            configCommands.config(settingKey, settingValue);
        }catch(CLICommandException ex) {
            assertEquals("Error received from Download Manager, no message provided.", ex.getMessage());
        }
    }

    @Test
    public void setConfigEntryFailureNoCommandResponseTest() throws ServiceException, CLICommandException, IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        UserModifiableSetting settingKey = UserModifiableSetting.DM_FRIENDLY_NAME;
        String settingValue = "d";
        URL serviceUrl = new URL(String.format("http://localhost:8082/download-manager/config?action=changeSetting&settingKey=%s&settingValue=%s", settingKey.toString(), URLEncoder.encode(settingValue, StandardCharsets.UTF_8.displayName())));
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenReturn(conn);

        when(downloadManagerResponseParser.parseCommandResponse(conn)).thenReturn(null);
        
        try {
            configCommands.config(settingKey, settingValue);
        }catch(CLICommandException ex) {
            assertEquals("Error received from Download Manager, no message provided.", ex.getMessage());
        }
    }

    @Test
    public void setConfigEntryServiceExceptionTest() throws ServiceException, IOException {
        UserModifiableSetting settingKey = UserModifiableSetting.DM_FRIENDLY_NAME;
        String settingValue = "Download Manager";
        URL serviceUrl = new URL(String.format("http://localhost:8082/download-manager/config?action=changeSetting&settingKey=%s&settingValue=%s", settingKey.toString(), URLEncoder.encode(settingValue, StandardCharsets.UTF_8.displayName())));
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenThrow(new ServiceException("Test ServiceException"));

        try {
            configCommands.config(UserModifiableSetting.DM_FRIENDLY_NAME, "Download Manager");
        } catch (CLICommandException e) {
            assertEquals("Unable to execute command, ServiceException: Test ServiceException", e.getMessage());
        }
    }
}
