package int_.esa.eo.ngeo.dmtu.manager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import int_.esa.eo.ngeo.dmtu.monitor.DownloadMonitor;
import int_.esa.eo.ngeo.dmtu.observer.ProductObserver;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

public class DataAccessRequestManagerTest {
	private DataAccessRequestManager dataAccessRequestManager;
	private ProductObserver observer;
	
	private static final String DOWNLOAD_URL = "http://download.tuxfamily.org/notepadplus/6.3.1/npp.6.3.1.bin.zip";
	private static final String MOCK_DAR_MONITORINGURL = "mock dar monitoringurl";

	@Before
	public void setup() {
		dataAccessRequestManager = new DataAccessRequestManager();
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
	public void testAddManualDownload() {
		//FIXME: Fix this test
//		DataAccessRequest manualDataAccessRequest = dataAccessRequestManager.addManualProductDownload(DOWNLOAD_URL);
//		assertNotNull(manualDataAccessRequest);
//		
//		assertEquals(1, dataAccessRequestManager.getDataAccessRequestList().size());
//
//		assertEquals(DataAccessRequestManager.MANUAL_DATA_REQUEST,manualDataAccessRequest.getMonitoringURL());
//		List<Product> productList = manualDataAccessRequest.getProductList();
//		assertEquals(1, productList.size());
//		assertEquals(productList.get(0).getProductAccessUrl(), DOWNLOAD_URL);
//		
//		//add of a second manual download, which should add another product (with the same download URL) to the Manual Data Access Request
//		dataAccessRequestManager.addManualProductDownload(DOWNLOAD_URL);
//		
//		assertEquals(1, dataAccessRequestManager.getDataAccessRequestList().size());
//		assertEquals(2, dataAccessRequestManager.getDataAccessRequestList().get(manualDataAccessRequest.getUuid()).getProductList().size());
//
//		verify(observer,times(2)).update(any(Product.class));
	}
	
	@Test
	public void testAddDataAccessRequest() throws MalformedURLException {
		//FIXME: Fix this test
//		dataAccessRequestManager.addDataAccessRequest(new URL(DOWNLOAD_URL));
//		assertEquals(1, dataAccessRequestManager.getDataAccessRequestList().size());
	}
}
