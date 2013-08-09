package int_.esa.eo.ngeo.dmtu.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.dmtu.exception.DataAccessRequestAlreadyExistsException;
import int_.esa.eo.ngeo.dmtu.exception.NonRecoverableException;
import int_.esa.eo.ngeo.dmtu.exception.ProductAlreadyExistsInDarException;
import int_.esa.eo.ngeo.dmtu.model.DataAccessRequest;
import int_.esa.eo.ngeo.dmtu.model.Product;
import int_.esa.eo.ngeo.dmtu.model.dao.DataAccessRequestDAO;
import int_.esa.eo.ngeo.dmtu.monitor.DownloadMonitor;
import int_.esa.eo.ngeo.dmtu.observer.ProductObserver;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccess;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessList;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessStatus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataAccessRequestManagerTest {
	@InjectMocks DataAccessRequestManager dataAccessRequestManager = new DataAccessRequestManager();
	@Mock DataAccessRequestDAO dataAccessRequestDao;
	
	private ProductObserver observer;
	
	private URL downloadUrl;
	private URL testDarUrl,testDarUrl2;

	@Before
	public void setup() throws MalformedURLException {
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
	public void testAddManualDownload() throws ProductAlreadyExistsInDarException, MalformedURLException {
		boolean productAdded = dataAccessRequestManager.addManualProductDownload(downloadUrl.toString());
		assertTrue(productAdded);
		
		assertEquals(1, dataAccessRequestManager.getVisibleDARList(true).size());
		assertEquals(0, dataAccessRequestManager.getVisibleDARList(false).size());

		DataAccessRequest manualDataAccessRequest = dataAccessRequestManager.getVisibleDARList(true).get(0);
		assertNotNull(manualDataAccessRequest);
		
		assertEquals(DataAccessRequestManager.MANUAL_DATA_REQUEST,manualDataAccessRequest.getMonitoringURL());
		List<Product> productList = manualDataAccessRequest.getProductList();
		assertEquals(1, productList.size());
		assertEquals(productList.get(0).getProductAccessUrl(), downloadUrl.toString());
	}

	@Test
	public void testAddManualDownloadTwoDownloads() throws MalformedURLException, ProductAlreadyExistsInDarException {
		dataAccessRequestManager.addManualProductDownload(downloadUrl.toString());
		dataAccessRequestManager.addManualProductDownload("http://ipv4.download.thinkbroadband.com/10MB.zip");

		DataAccessRequest manualDataAccessRequest = dataAccessRequestManager.getVisibleDARList(true).get(0);
		assertEquals(2, manualDataAccessRequest.getProductList().size());
	}

	@Test
	public void testAddManualDownloadSameDownloadTwice() throws MalformedURLException, ProductAlreadyExistsInDarException {
		dataAccessRequestManager.addManualProductDownload(downloadUrl.toString());
		//add of a second manual download, which should throw a ProductAlreadyExistsInDarException
		try {
			dataAccessRequestManager.addManualProductDownload(downloadUrl.toString());
			fail("adding the same manual download twice should cause an exception to be thrown.");
		}catch(ProductAlreadyExistsInDarException ex) {
			assertEquals(String.format("Product %s already exists in DAR %s", downloadUrl.toString(), DataAccessRequestManager.MANUAL_DATA_REQUEST), ex.getLocalizedMessage());
		}
		
	}

	@Test
	public void testAddDataAccessRequest() throws MalformedURLException, DataAccessRequestAlreadyExistsException {
		dataAccessRequestManager.addDataAccessRequest(downloadUrl);
		assertEquals(1, dataAccessRequestManager.getVisibleDARList(true).size());
	}

	@Test
	public void testAddDataAccessRequestTwice() throws MalformedURLException, DataAccessRequestAlreadyExistsException {
		dataAccessRequestManager.addDataAccessRequest(downloadUrl);
		//add of a second manual download, which should throw a ProductAlreadyExistsInDarException
		try {
			dataAccessRequestManager.addDataAccessRequest(downloadUrl);
			fail("adding the same DAR twice should cause an exception to be thrown.");
		}catch(DataAccessRequestAlreadyExistsException ex) {
			assertEquals(String.format("Data Access Request for url %s already exists.", downloadUrl.toString(), DataAccessRequestManager.MANUAL_DATA_REQUEST), ex.getLocalizedMessage());
		}
	}
	
	@Test
	public void testGetDataAccessRequestByUuid() throws ProductAlreadyExistsInDarException {
		dataAccessRequestManager.addManualProductDownload(downloadUrl.toString());
		DataAccessRequest manualDataAccessRequest = dataAccessRequestManager.getVisibleDARList(true).get(0);
		
		DataAccessRequest retrievedDAR = dataAccessRequestManager.getDataAccessRequestByUuid(manualDataAccessRequest.getUuid());
		assertEquals(manualDataAccessRequest, retrievedDAR);
	}
	
	@Test
	public void testGetDataAccessRequestByUuidDARDoesNotExist() {
		String myUuid = "myUuid";
		try {
			dataAccessRequestManager.getDataAccessRequestByUuid(myUuid);
			fail("DAR with uuid \"myUuid\" does not exist, an exception should be thrown here.");
		}catch(NonRecoverableException ex) {
			assertEquals(String.format("Unable to find Data Access Request for uuid %s", myUuid), ex.getLocalizedMessage());
		}
	}
	
	@Test
	public void testGetDataAccessRequestByMonitoringUrl() throws DataAccessRequestAlreadyExistsException {
		dataAccessRequestManager.addDataAccessRequest(testDarUrl2);
		dataAccessRequestManager.addDataAccessRequest(testDarUrl);
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
		productAccessListObject.getProductAccess().add(productAccess);
		dataAccessRequestManager.updateDataAccessRequest(testDarUrl, MonitoringStatus.IN_PROGRESS, responseDate, productAccessListObject);
		
		DataAccessRequest updatedDAR = dataAccessRequestManager.getDataAccessRequestByMonitoringUrl(testDarUrl);
		assertEquals(MonitoringStatus.IN_PROGRESS, updatedDAR.getMonitoringStatus());
		assertEquals(responseDate, updatedDAR.getLastResponseDate());
		
		verify(dataAccessRequestDao, times(2)).updateDataAccessRequest(updatedDAR);
	}

	private void addTestDAR() throws DataAccessRequestAlreadyExistsException {
		dataAccessRequestManager.addDataAccessRequest(testDarUrl);
	}

	@Test
	public void testUpdateDataAccessRequestDARDoesNotExist() {
		try {
			dataAccessRequestManager.updateDataAccessRequest(testDarUrl, MonitoringStatus.IN_PROGRESS, new Date(), null);
			fail("Updating a DAR that does not exist should throw an exception.");
		}catch(NonRecoverableException ex) {
			assertEquals(String.format("Data Access Request for url %s does not exist.",  testDarUrl.toString()), ex.getLocalizedMessage());
		}
	}

	@Test
	public void testUpdateDataAccessRequestEmptyProductList() throws DataAccessRequestAlreadyExistsException {
		addTestDAR();

		dataAccessRequestManager.updateDataAccessRequest(testDarUrl, MonitoringStatus.IN_PROGRESS, new Date(), new ProductAccessList());
		DataAccessRequest updatedDAR = dataAccessRequestManager.getDataAccessRequestByMonitoringUrl(testDarUrl);
		assertEquals(0, updatedDAR.getProductList().size());
	}

	//Unit Test for DMTU-113
	@Test
	public void testUpdateDataAccessRequestNullProductList() throws DataAccessRequestAlreadyExistsException {
		addTestDAR();
		Date responseDate = new Date();

		dataAccessRequestManager.updateDataAccessRequest(testDarUrl, MonitoringStatus.IN_PROGRESS, responseDate, null);

		DataAccessRequest updatedDAR = dataAccessRequestManager.getDataAccessRequestByMonitoringUrl(testDarUrl);
		assertEquals(MonitoringStatus.IN_PROGRESS, updatedDAR.getMonitoringStatus());
		assertEquals(responseDate, updatedDAR.getLastResponseDate());
	}

	@Test
	public void testUpdateDataAccessRequestCompletedProgress() throws DataAccessRequestAlreadyExistsException {
		addTestDAR();

		dataAccessRequestManager.updateDataAccessRequest(testDarUrl, MonitoringStatus.COMPLETED, new Date(), new ProductAccessList());
		DataAccessRequest updatedDAR = dataAccessRequestManager.getDataAccessRequestByMonitoringUrl(testDarUrl);
		assertEquals(MonitoringStatus.COMPLETED, updatedDAR.getMonitoringStatus());
	}
}
