package int_.esa.eo.ngeo.downloadmanager.dar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.builder.DataAccessRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.download.DownloadMonitor;
import int_.esa.eo.ngeo.downloadmanager.exception.DataAccessRequestAlreadyExistsException;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.model.dao.DataAccessRequestDaoImpl;
import int_.esa.eo.ngeo.downloadmanager.observer.ProductObserver;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccess;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessList;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessStatus;

import java.net.MalformedURLException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataAccessRequestManagerTest {
    DataAccessRequestManager dataAccessRequestManager;
    DataAccessRequestDaoImpl dataAccessRequestDao;
    DataAccessRequestBuilder dataAccessRequestBuilder;

    private ProductObserver observer;

    private String downloadUrl;
    private DataAccessRequest testDar, testDar2;

    @Before
    public void setup() throws MalformedURLException {
        dataAccessRequestDao = mock(DataAccessRequestDaoImpl.class);
        dataAccessRequestManager = new DataAccessRequestManager(dataAccessRequestDao);
        dataAccessRequestBuilder = new DataAccessRequestBuilder();
        
        this.downloadUrl = "http://download.tuxfamily.org/notepadplus/6.3.1/npp.6.3.1.bin.zip";
        this.testDar = dataAccessRequestBuilder.buildDAR("http://test.dar.url.com/", null, true);
        this.testDar2 = dataAccessRequestBuilder.buildDAR("http://test.dar.url2.com/", null, true);
        observer = mock(DownloadMonitor.class);
        dataAccessRequestManager.registerObserver(observer);
    }

    @Test
    public void testDataAccessRequestManagerInitialisation() {
        assertEquals(0, dataAccessRequestManager.getVisibleDARList(true).size());
    }

    @Test
    public void testGetDataAccessRequestByMonitoringUrl() throws DataAccessRequestAlreadyExistsException {
        dataAccessRequestManager.addDataAccessRequest(testDar);
        dataAccessRequestManager.addDataAccessRequest(testDar2);
        
        DataAccessRequest addedDataAccessRequest = dataAccessRequestManager.getVisibleDARList(true).get(1);

        DataAccessRequest searchDar = new DataAccessRequest();
        searchDar.setDarURL(testDar2.getDarURL());
                
        DataAccessRequest retrievedDar = dataAccessRequestManager.getDataAccessRequest(searchDar);
        assertEquals(addedDataAccessRequest.getDarURL(), retrievedDar.getDarURL());
    }

    @Test
    public void testGetDataAccessRequestByMonitoringUrlDARDoesNotExist() {
        DataAccessRequest searchDar = new DataAccessRequest();
        searchDar.setDarURL(testDar.getDarURL());
        
        when(dataAccessRequestDao.searchForDar(searchDar)).thenReturn(null);
        assertNull(dataAccessRequestManager.getDataAccessRequest(searchDar));
    }

    @Test
    public void testUpdateDataAccessRequest() throws DataAccessRequestAlreadyExistsException {
        addTestDAR();

        Date responseDate = new Date();
        DataAccessMonitoringResp dataAccessMonitoringResponse = new DataAccessMonitoringResp();
        dataAccessMonitoringResponse.setMonitoringStatus(MonitoringStatus.IN_PROGRESS);
        ProductAccessList productAccessListObject = new ProductAccessList();
        ProductAccess productAccess = new ProductAccess();
        productAccess.setProductAccessURL(downloadUrl.toString());
        productAccess.setProductAccessStatus(ProductAccessStatus.READY);
        productAccessListObject.getProductAccesses().add(productAccess);
        dataAccessMonitoringResponse.setProductAccessList(productAccessListObject);

        dataAccessRequestManager.updateDataAccessRequest(testDar, dataAccessMonitoringResponse, responseDate, ProductPriority.NORMAL);

        DataAccessRequest updatedDAR = dataAccessRequestManager.getDataAccessRequest(testDar);
        assertEquals(MonitoringStatus.IN_PROGRESS, updatedDAR.getMonitoringStatus());
        assertEquals(responseDate, updatedDAR.getLastResponseDate());

        verify(dataAccessRequestDao, times(2)).updateDataAccessRequest(updatedDAR);
    }

    private void addTestDAR() throws DataAccessRequestAlreadyExistsException {
        dataAccessRequestManager.addDataAccessRequest(dataAccessRequestBuilder.buildDAR(testDar.getDarURL(), null, true));
    }

    @Test
    public void testUpdateDataAccessRequestDARDoesNotExist() {
        try {
            dataAccessRequestManager.updateDataAccessRequest(testDar, null, new Date(), ProductPriority.NORMAL);
            fail("Updating a DAR that does not exist should throw an exception.");
        }catch(NonRecoverableException ex) {
            assertEquals(String.format("Data Access Request with url %s does not exist.",  testDar.getDarURL().toString()), ex.getLocalizedMessage());
        }
    }

    @Test
    public void testUpdateDataAccessRequestEmptyProductList() throws DataAccessRequestAlreadyExistsException {
        addTestDAR();
        DataAccessMonitoringResp dataAccessMonitoringResponse = new DataAccessMonitoringResp();
        dataAccessMonitoringResponse.setMonitoringStatus(MonitoringStatus.IN_PROGRESS);
        ProductAccessList productAccessListObject = new ProductAccessList();
        dataAccessMonitoringResponse.setProductAccessList(productAccessListObject);

        dataAccessRequestManager.updateDataAccessRequest(testDar, dataAccessMonitoringResponse, new Date(), ProductPriority.NORMAL);
        DataAccessRequest updatedDAR = dataAccessRequestManager.getDataAccessRequest(testDar);
        assertEquals(0, updatedDAR.getProductList().size());
    }

    //Unit Test for DMTU-113
    @Test
    public void testUpdateDataAccessRequestNullProductList() throws DataAccessRequestAlreadyExistsException {
        addTestDAR();
        Date responseDate = new Date();

        DataAccessMonitoringResp dataAccessMonitoringResponse = new DataAccessMonitoringResp();
        dataAccessMonitoringResponse.setMonitoringStatus(MonitoringStatus.IN_PROGRESS);

        dataAccessRequestManager.updateDataAccessRequest(testDar, dataAccessMonitoringResponse, responseDate, ProductPriority.NORMAL);

        DataAccessRequest updatedDAR = dataAccessRequestManager.getDataAccessRequest(testDar);
        assertEquals(MonitoringStatus.IN_PROGRESS, updatedDAR.getMonitoringStatus());
        assertEquals(responseDate, updatedDAR.getLastResponseDate());
    }

    @Test
    public void testUpdateDataAccessRequestCompletedProgress() throws DataAccessRequestAlreadyExistsException {
        addTestDAR();

        DataAccessMonitoringResp dataAccessMonitoringResponse = new DataAccessMonitoringResp();
        dataAccessMonitoringResponse.setMonitoringStatus(MonitoringStatus.COMPLETED);
        ProductAccessList productAccessListObject = new ProductAccessList();
        dataAccessMonitoringResponse.setProductAccessList(productAccessListObject);

        dataAccessRequestManager.updateDataAccessRequest(testDar, dataAccessMonitoringResponse, new Date(), ProductPriority.NORMAL);
        DataAccessRequest updatedDAR = dataAccessRequestManager.getDataAccessRequest(testDar);
        assertEquals(MonitoringStatus.COMPLETED, updatedDAR.getMonitoringStatus());
    }
}
