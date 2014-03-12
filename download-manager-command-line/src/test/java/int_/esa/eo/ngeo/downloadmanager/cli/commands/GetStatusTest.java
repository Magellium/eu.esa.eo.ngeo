package int_.esa.eo.ngeo.downloadmanager.cli.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.builder.DataAccessRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.builder.ProductBuilder;
import int_.esa.eo.ngeo.downloadmanager.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.downloadmanager.cli.config.DmCliSetting;
import int_.esa.eo.ngeo.downloadmanager.cli.exception.CLICommandException;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerResponseParser;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerService;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.rest.StatusResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetStatusTest {
    GetStatus getStatus;
    DownloadManagerResponseParser downloadManagerResponseParser;
    DownloadManagerService downloadManagerService;
    ConfigurationProvider configurationProvider;

    @Before
    public void setup() {
        downloadManagerResponseParser = mock(DownloadManagerResponseParser.class);
        downloadManagerService = mock(DownloadManagerService.class);
        configurationProvider = mock(ConfigurationProvider.class);
        getStatus = new GetStatus(downloadManagerService, downloadManagerResponseParser, configurationProvider);
        when(configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL)).thenReturn("http://localhost:8082/download-manager");
    }

    @Test
    public void isConfigAvailableTest() {
        assertTrue(getStatus.isGetStatusAvailable());
    }

    @Test
    public void getStatusTest() throws CLICommandException, ServiceException, IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(downloadManagerService.sendGetCommand(new URL("http://localhost:8082/download-manager/dataAccessRequests"))).thenReturn(conn);

        //setup basic status response
        StatusResponse statusResponse = new StatusResponse();
        List<DataAccessRequest> darList = new ArrayList<>();
        DataAccessRequest dar = new DataAccessRequestBuilder().buildDAR("http://www.test.com/dar", null, true);
        Product product1 = new ProductBuilder().buildProduct("product 1 url");
        product1.setTotalFileSize(-1);
        product1.getProductProgress().setDownloadedSize(21345);
        product1.getProductProgress().setProgressPercentage(-1);
        
        Product product2 = new ProductBuilder().buildProduct("product 2 url");
        product2.setPriority(ProductPriority.HIGH);
        Product product3 = new ProductBuilder().buildProduct("product 3 url");
        product3.setTotalFileSize(5242880);
        product3.getProductProgress().setDownloadedSize(5242880);
        product3.getProductProgress().setProgressPercentage(100);
        product3.getProductProgress().setStatus(EDownloadStatus.COMPLETED);
        Product hiddenProduct = new ProductBuilder().buildProduct("hidden product url");
        hiddenProduct.setVisible(false);
        
        List<Product> productList = new ArrayList<>();
        productList.add(product1);
        productList.add(product2);
        productList.add(product3);
        productList.add(hiddenProduct);

        dar.setProductList(productList);
        darList.add(dar);

        DataAccessRequest hiddenDar = new DataAccessRequestBuilder().buildDAR("hidden dar url", "hidden dar name", true);
        hiddenDar.setVisible(false);
        darList.add(hiddenDar);
        
        statusResponse.setDataAccessRequests(darList);
        when(downloadManagerResponseParser.parseStatusResponse(conn)).thenReturn(statusResponse);

        StringBuilder expectedOutput = new StringBuilder(100);
        expectedOutput.append("http://www.test.com/dar\nMonitoring Status: IN_PROGRESS\n\n");

        expectedOutput.append(String.format("\tproduct 1 url (%s)\n", product1.getUuid()));
        expectedOutput.append("\t\tDownloaded: 21,345 / Unknown (Unknown %)\n");
        expectedOutput.append("\t\tStatus: NOT_STARTED\n");
        expectedOutput.append("\t\tPriority: Normal\n\n");
        expectedOutput.append(String.format("\tproduct 2 url (%s)\n", product2.getUuid()));
        expectedOutput.append("\t\tDownloaded: 0 / 0 (0%)\n");
        expectedOutput.append("\t\tStatus: NOT_STARTED\n");
        expectedOutput.append("\t\tPriority: High\n\n");
        expectedOutput.append(String.format("\tproduct 3 url (%s)\n", product3.getUuid()));
        expectedOutput.append("\t\tDownloaded: 5,242,880 / 5,242,880 (100%)\n");
        expectedOutput.append("\t\tStatus: COMPLETED\n\n");

        assertEquals(expectedOutput.toString(), getStatus.getStatus());
    }
    
    @Test
    public void getStatusWithNameNotMonitoredTest() throws CLICommandException, ServiceException, IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(downloadManagerService.sendGetCommand(new URL("http://localhost:8082/download-manager/dataAccessRequests"))).thenReturn(conn);

        //setup basic status response
        StatusResponse statusResponse = new StatusResponse();
        List<DataAccessRequest> darList = new ArrayList<>();
        DataAccessRequest dar = new DataAccessRequestBuilder().buildDAR(null, "Test DAR name", false);
        Product product3 = new ProductBuilder().buildProduct("product 3 url");
        product3.setTotalFileSize(5242880);
        product3.getProductProgress().setDownloadedSize(5242880);
        product3.getProductProgress().setProgressPercentage(100);
        product3.getProductProgress().setStatus(EDownloadStatus.COMPLETED);
        
        List<Product> productList = new ArrayList<>();
        productList.add(product3);

        dar.setProductList(productList);
        darList.add(dar);

        statusResponse.setDataAccessRequests(darList);
        when(downloadManagerResponseParser.parseStatusResponse(conn)).thenReturn(statusResponse);

        StringBuilder expectedOutput = new StringBuilder(100);
        expectedOutput.append("Test DAR name\n\n");

        expectedOutput.append(String.format("\tproduct 3 url (%s)\n", product3.getUuid()));
        expectedOutput.append("\t\tDownloaded: 5,242,880 / 5,242,880 (100%)\n");
        expectedOutput.append("\t\tStatus: COMPLETED\n\n");

        assertEquals(expectedOutput.toString(), getStatus.getStatus());
    }    

    @Test
    public void getStatusEmptyDarListTest() throws CLICommandException, ServiceException, IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(downloadManagerService.sendGetCommand(new URL("http://localhost:8082/download-manager/dataAccessRequests"))).thenReturn(conn);

        //setup basic status response
        StatusResponse statusResponse = new StatusResponse();
        List<DataAccessRequest> darList = new ArrayList<>();
        statusResponse.setDataAccessRequests(darList);
        when(downloadManagerResponseParser.parseStatusResponse(conn)).thenReturn(statusResponse);

        assertEquals("There are currently no visible DARs.", getStatus.getStatus());
    }
    
    @Test
    public void serviceExceptionTest() throws ServiceException, IOException {
        URL serviceUrl = new URL("http://localhost:8082/download-manager/dataAccessRequests");
        when(downloadManagerService.sendGetCommand(serviceUrl)).thenThrow(new ServiceException("Test ServiceException"));

        try {
            getStatus.getStatus();
        } catch (CLICommandException e) {
            assertEquals("Unable to execute command, ServiceException: Test ServiceException", e.getMessage());
        }
    }
}
