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
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithDarUuid;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithProductUuid;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

public class AddManualDownloadTest {
    AddManualDownload addManualDownload;
    DownloadManagerResponseParser downloadManagerResponseParser;
    DownloadManagerService downloadManagerService;
    ConfigurationProvider configurationProvider;

    @Before
    public void setup() {
        downloadManagerResponseParser = mock(DownloadManagerResponseParser.class);
        downloadManagerService = mock(DownloadManagerService.class);
        configurationProvider = mock(ConfigurationProvider.class);

        addManualDownload = new AddManualDownload(downloadManagerService, downloadManagerResponseParser, configurationProvider);
        
        when(configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL)).thenReturn("http://localhost:8082/download-manager");
    }

    @Test
    public void areCommandsAvailableTest() {
        assertTrue(addManualDownload.isAddDarAvailable());
        assertTrue(addManualDownload.isAddProductAvailable());
    }

    @Test
    public void addDarTest() throws IOException, ServiceException, CLICommandException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        String darUrl = "http://localhost:8080/download-manager-mock-web-server/static/manualDAR";
        URL serviceUrl = new URL("http://localhost:8082/download-manager/download");
        String parameters = String.format("darUrl=%s&priority=%s", URLEncoder.encode(darUrl, StandardCharsets.UTF_8.displayName()), ProductPriority.NORMAL.name());
        when(downloadManagerService.sendPostCommand(serviceUrl, parameters)).thenReturn(conn);

        CommandResponseWithDarUuid commandResponseWithDarUuid = new CommandResponseWithDarUuid();
        commandResponseWithDarUuid.setSuccess(true);
        commandResponseWithDarUuid.setDarUuid("abc123");
        when(downloadManagerResponseParser.parseCommandResponseWithDarUuid(conn)).thenReturn(commandResponseWithDarUuid);
        assertEquals("DAR added, UUID is abc123. Please use the \"status\" command to monitor the progress.", addManualDownload.addDar(darUrl, ProductPriority.NORMAL));
    }

    @Test
    public void addDarNoSuccessTest() throws IOException, ServiceException, CLICommandException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        String darUrl = "http://localhost:8080/download-manager-mock-web-server/static/manualDAR";
        URL serviceUrl = new URL("http://localhost:8082/download-manager/download");
        String parameters = String.format("darUrl=%s&priority=%s", URLEncoder.encode(darUrl, StandardCharsets.UTF_8.displayName()), ProductPriority.NORMAL.name());
        when(downloadManagerService.sendPostCommand(serviceUrl, parameters)).thenReturn(conn);

        CommandResponseWithDarUuid commandResponseWithDarUuid = new CommandResponseWithDarUuid();
        commandResponseWithDarUuid.setSuccess(false);
        commandResponseWithDarUuid.setErrorMessage("Unable to add DAR, invalid format.");
        commandResponseWithDarUuid.setErrorType("int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException");

        when(downloadManagerResponseParser.parseCommandResponseWithDarUuid(conn)).thenReturn(commandResponseWithDarUuid);
        try {
            addManualDownload.addDar(darUrl, ProductPriority.NORMAL);
        }catch(CLICommandException ex) {
            assertEquals("Error from Download Manager: Unable to add DAR, invalid format.", ex.getMessage());
        }
    }

    @Test
    public void addDarTestPostServiceException() throws ServiceException, UnsupportedEncodingException, MalformedURLException {
        String darUrl = "http://localhost:8080/download-manager-mock-web-server/static/manualDAR";
        URL serviceUrl = new URL("http://localhost:8082/download-manager/download");
        String parameters = String.format("darUrl=%s&priority=%s", URLEncoder.encode(darUrl, StandardCharsets.UTF_8.displayName()), ProductPriority.NORMAL.name());
        when(downloadManagerService.sendPostCommand(serviceUrl, parameters)).thenThrow(new ServiceException("test ServiceException"));

        try {
            addManualDownload.addDar(darUrl, ProductPriority.NORMAL);
        }catch(CLICommandException ex) {
            assertEquals("Unable to execute command, ServiceException: test ServiceException", ex.getMessage());
        }
    }

    @Test
    public void addDarTestParseServiceException() throws IOException, ServiceException, CLICommandException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        String darUrl = "http://localhost:8080/download-manager-mock-web-server/static/manualDAR";
        URL serviceUrl = new URL("http://localhost:8082/download-manager/download");
        String parameters = String.format("darUrl=%s&priority=%s", URLEncoder.encode(darUrl, StandardCharsets.UTF_8.displayName()), ProductPriority.NORMAL.name());
        when(downloadManagerService.sendPostCommand(serviceUrl, parameters)).thenReturn(conn);

        when(downloadManagerResponseParser.parseCommandResponseWithDarUuid(conn)).thenThrow(new ServiceException("Unable to parse command response for add product."));
        try {
            addManualDownload.addDar(darUrl, ProductPriority.NORMAL);
        }catch(CLICommandException ex) {
            assertEquals("Unable to execute command, ServiceException: Unable to parse command response for add product.", ex.getMessage());
        }
    }

    @Test
    public void addDarNoCommandResponseTest() throws IOException, ServiceException, CLICommandException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        String darUrl = "http://localhost:8080/download-manager-mock-web-server/static/manualDAR";
        URL serviceUrl = new URL("http://localhost:8082/download-manager/download");
        String parameters = String.format("darUrl=%s&priority=%s", URLEncoder.encode(darUrl, StandardCharsets.UTF_8.displayName()), ProductPriority.NORMAL.name());
        when(downloadManagerService.sendPostCommand(serviceUrl, parameters)).thenReturn(conn);

        when(downloadManagerResponseParser.parseCommandResponseWithDarUuid(conn)).thenReturn(null);
        try {
            addManualDownload.addDar(darUrl, ProductPriority.NORMAL);
        }catch(CLICommandException ex) {
            assertEquals("Error received from Download Manager, no message provided.", ex.getMessage());
        }
    }

    @Test
    public void addProductTest() throws IOException, ServiceException, CLICommandException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        String productDownloadUrl = "http://localhost:8080/download-manager-mock-web-server/static/testProduct";
        URL serviceUrl = new URL("http://localhost:8082/download-manager/download");
        String parameters = String.format("productDownloadUrl=%s&priority=%s", URLEncoder.encode(productDownloadUrl, StandardCharsets.UTF_8.displayName()), ProductPriority.NORMAL.name());
        when(downloadManagerService.sendPostCommand(serviceUrl, parameters)).thenReturn(conn);

        CommandResponseWithProductUuid commandResponseWithProductUuid = new CommandResponseWithProductUuid();
        commandResponseWithProductUuid.setSuccess(true);
        commandResponseWithProductUuid.setProductUuid("productUuid123");
        when(downloadManagerResponseParser.parseCommandResponseWithProductUuid(conn)).thenReturn(commandResponseWithProductUuid);
        assertEquals("Product added, UUID is productUuid123. Please use the \"status\" command to monitor the progress.", addManualDownload.addProduct(productDownloadUrl, ProductPriority.NORMAL));
    }

    @Test
    public void addProductTestParseServiceException() throws ServiceException, UnsupportedEncodingException, MalformedURLException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        String productDownloadUrl = "http://localhost:8080/download-manager-mock-web-server/static/testProduct";
        URL serviceUrl = new URL("http://localhost:8082/download-manager/download");
        String parameters = String.format("productDownloadUrl=%s&priority=%s", URLEncoder.encode(productDownloadUrl, StandardCharsets.UTF_8.displayName()), ProductPriority.NORMAL.name());
        when(downloadManagerService.sendPostCommand(serviceUrl, parameters)).thenReturn(conn);

        when(downloadManagerResponseParser.parseCommandResponseWithProductUuid(conn)).thenThrow(new ServiceException("Unable to parse command response for add product."));

        try {
            addManualDownload.addProduct(productDownloadUrl, ProductPriority.NORMAL);
        }catch(CLICommandException ex) {
            assertEquals("Unable to execute command, ServiceException: Unable to parse command response for add product.", ex.getMessage());
        }
    }
    
    @Test
    public void getMessageFromCommandResponseCommandResponseInstanceTest() {
        CommandResponse commandResponse = new CommandResponse();
        commandResponse.setSuccess(true);
        
        try{
            addManualDownload.getMessageFromCommandResponse(commandResponse, "Success message.");
        }catch(CLICommandException ex) {
            assertEquals("Error received from Download Manager, no message provided.", ex.getMessage());
        }
    }
}
