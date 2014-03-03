package int_.esa.eo.ngeo.downloadmanager.dar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.download.DownloadMonitor;
import int_.esa.eo.ngeo.downloadmanager.exception.DataAccessRequestAlreadyExistsException;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.model.dao.DataAccessRequestDaoImpl;
import int_.esa.eo.ngeo.downloadmanager.observer.ProductObserver;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccess;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessList;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessStatus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataAccessRequestManagerTest {
    DataAccessRequestManager dataAccessRequestManager;
    DataAccessRequestDaoImpl dataAccessRequestDao;

    private ProductObserver observer;

    private URL downloadUrl;
    private URL testDarUrl,testDarUrl2;

    @Before
    public void setup() throws MalformedURLException {
        dataAccessRequestDao = mock(DataAccessRequestDaoImpl.class);
        dataAccessRequestManager = new DataAccessRequestManager(dataAccessRequestDao);
        
        this.downloadUrl = new URL("http://download.tuxfamily.org/notepadplus/6.3.1/npp.6.3.1.bin.zip");
        this.testDarUrl = new URL("http://test.dar.url.com/");
        this.testDarUrl2 = new URL("http://test.dar.url2.com/");
        observer = mock(DownloadMonitor.class);
        dataAccessRequestManager.registerObserver(observer);
    }

    @Test
    public void testDataAccessRequestManagerInitialisation() {
        assertEquals(0, dataAccessRequestManager.getVisibleDARList(true).size());
    }

    @Test
    public void testObserver() {
        //XXX: complete this test
    }

    @Test
    public void testGetDataAccessRequestByMonitoringUrl() throws DataAccessRequestAlreadyExistsException {
        dataAccessRequestManager.addDataAccessRequest(testDarUrl2, true);
        dataAccessRequestManager.addDataAccessRequest(testDarUrl, true);
        DataAccessRequest addedDataAccessRequest = dataAccessRequestManager.getVisibleDARList(true).get(1);

        DataAccessRequest retrievedDAR = dataAccessRequestManager.getDataAccessRequestByMonitoringUrl(testDarUrl);
        assertEquals(addedDataAccessRequest, retrievedDAR);
    }

    @Test
    public void testGetDataAccessRequestByMonitoringUrlDARDoesNotExist() {
        when(dataAccessRequestDao.getDarByMonitoringUrl(testDarUrl.toString())).thenReturn(null);
        assertNull(dataAccessRequestManager.getDataAccessRequestByMonitoringUrl(testDarUrl));
    }

    @Test
    public void testUpdateDataAccessRequest() throws DataAccessRequestAlreadyExistsException {
        addTestDAR();

        Date responseDate = new Date();
        ProductAccessList productAccessListObject = new ProductAccessList();
        ProductAccess productAccess = new ProductAccess();
        productAccess.setProductAccessURL(downloadUrl.toString());
        productAccess.setProductAccessStatus(ProductAccessStatus.READY);
        productAccessListObject.getProductAccesses().add(productAccess);
        dataAccessRequestManager.updateDataAccessRequest(testDarUrl, MonitoringStatus.IN_PROGRESS, responseDate, productAccessListObject, ProductPriority.NORMAL);

        DataAccessRequest updatedDAR = dataAccessRequestManager.getDataAccessRequestByMonitoringUrl(testDarUrl);
        assertEquals(MonitoringStatus.IN_PROGRESS, updatedDAR.getMonitoringStatus());
        assertEquals(responseDate, updatedDAR.getLastResponseDate());

        verify(dataAccessRequestDao, times(2)).updateDataAccessRequest(updatedDAR);
    }

    private void addTestDAR() throws DataAccessRequestAlreadyExistsException {
        dataAccessRequestManager.addDataAccessRequest(testDarUrl, true);
    }

    @Test
    public void testUpdateDataAccessRequestDARDoesNotExist() {
        try {
            dataAccessRequestManager.updateDataAccessRequest(testDarUrl, MonitoringStatus.IN_PROGRESS, new Date(), null, ProductPriority.NORMAL);
            fail("Updating a DAR that does not exist should throw an exception.");
        }catch(NonRecoverableException ex) {
            assertEquals(String.format("Data Access Request for url %s does not exist.",  testDarUrl.toString()), ex.getLocalizedMessage());
        }
    }

    @Test
    public void testUpdateDataAccessRequestEmptyProductList() throws DataAccessRequestAlreadyExistsException {
        addTestDAR();

        dataAccessRequestManager.updateDataAccessRequest(testDarUrl, MonitoringStatus.IN_PROGRESS, new Date(), new ProductAccessList(), ProductPriority.NORMAL);
        DataAccessRequest updatedDAR = dataAccessRequestManager.getDataAccessRequestByMonitoringUrl(testDarUrl);
        assertEquals(0, updatedDAR.getProductList().size());
    }

    //Unit Test for DMTU-113
    @Test
    public void testUpdateDataAccessRequestNullProductList() throws DataAccessRequestAlreadyExistsException {
        addTestDAR();
        Date responseDate = new Date();

        dataAccessRequestManager.updateDataAccessRequest(testDarUrl, MonitoringStatus.IN_PROGRESS, responseDate, null, ProductPriority.NORMAL);

        DataAccessRequest updatedDAR = dataAccessRequestManager.getDataAccessRequestByMonitoringUrl(testDarUrl);
        assertEquals(MonitoringStatus.IN_PROGRESS, updatedDAR.getMonitoringStatus());
        assertEquals(responseDate, updatedDAR.getLastResponseDate());
    }

    @Test
    public void testUpdateDataAccessRequestCompletedProgress() throws DataAccessRequestAlreadyExistsException {
        addTestDAR();

        dataAccessRequestManager.updateDataAccessRequest(testDarUrl, MonitoringStatus.COMPLETED, new Date(), new ProductAccessList(), ProductPriority.NORMAL);
        DataAccessRequest updatedDAR = dataAccessRequestManager.getDataAccessRequestByMonitoringUrl(testDarUrl);
        assertEquals(MonitoringStatus.COMPLETED, updatedDAR.getMonitoringStatus());
    }
}
