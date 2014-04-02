package int_.esa.eo.ngeo.downloadmanager.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.builder.DataAccessRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.builder.ProductBuilder;
import int_.esa.eo.ngeo.downloadmanager.dar.DataAccessRequestManager;
import int_.esa.eo.ngeo.downloadmanager.download.DownloadMonitor;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class DARMonitorControllerTest {
    DARMonitorController darMonitorController;
    DataAccessRequestManager dataAccessRequestManager;
    DownloadMonitor downloadMonitor;
    
    private static final String PRODUCT_URL_NOTEPAD_PLUSPLUS = "http://download.tuxfamily.org/notepadplus/6.3.1/npp.6.3.1.bin.zip";
    private static final String PRODUCT_URL_UBUNTU = "http://www.ubuntu.com/start-download?distro=desktop&bits=32&release=latest";
    private static final String MONITORING_URL = "http://dmtu.ngeo.eo.esa.int/monitoringUrl";

    @Before
    public void setup() {
        dataAccessRequestManager = mock(DataAccessRequestManager.class);
        downloadMonitor = mock(DownloadMonitor.class);
        
        darMonitorController = new DARMonitorController(dataAccessRequestManager, downloadMonitor);
    }
    
    @Test
    public void testGetDataAccessRequests() {
        List<DataAccessRequest> dataAccessRequestList = setupDataAccessRequestList();
        
        when(dataAccessRequestManager.getVisibleDARList(true)).thenReturn(dataAccessRequestList);
        
        List<DataAccessRequest> dataAccessRequests = darMonitorController.getDataAccessRequestStatus(true).getDataAccessRequests();
        assertEquals(1,dataAccessRequests.size());
        DataAccessRequest dataAccessRequest = dataAccessRequests.get(0);
        assertEquals(MONITORING_URL, dataAccessRequest.getDarURL());
    }

    public List<DataAccessRequest> setupDataAccessRequestList() {
        List<DataAccessRequest> dataAccessRequestList;
        dataAccessRequestList = new ArrayList<DataAccessRequest>();
        DataAccessRequest dataAccessRequest = new DataAccessRequestBuilder().buildDAR(MONITORING_URL, null, true);
        dataAccessRequest.getProductList().add(new ProductBuilder().buildProduct(PRODUCT_URL_NOTEPAD_PLUSPLUS));
        dataAccessRequest.getProductList().add(new ProductBuilder().buildProduct(PRODUCT_URL_UBUNTU));
        
        dataAccessRequestList.add(dataAccessRequest);
        return dataAccessRequestList;
    }
}
