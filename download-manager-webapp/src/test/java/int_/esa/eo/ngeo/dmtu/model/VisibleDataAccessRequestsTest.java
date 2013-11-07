package int_.esa.eo.ngeo.dmtu.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import int_.esa.eo.ngeo.dmtu.exception.ProductAlreadyExistsInDarException;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

public class VisibleDataAccessRequestsTest {
	@InjectMocks VisibleDataAccessRequests visibleDataAccessRequests = new VisibleDataAccessRequests();
	
	private URL downloadUrl;

	@Before
	public void setup() throws MalformedURLException {
		this.downloadUrl = new URL("http://download.tuxfamily.org/notepadplus/6.3.1/npp.6.3.1.bin.zip");
	}

	@Test
	public void testVisibleDataAccessRequestsInitialisation() {
		assertEquals(0, visibleDataAccessRequests.getDARList(true).size());
	}

	@Test
	public void testAddManualDownload() throws ProductAlreadyExistsInDarException, MalformedURLException {
		Product productDownload = visibleDataAccessRequests.addManualProductDownload(downloadUrl.toString());
		assertNotNull(productDownload);
		
		assertEquals(1, visibleDataAccessRequests.getDARList(true).size());
		assertEquals(0, visibleDataAccessRequests.getDARList(false).size());

		DataAccessRequest manualDataAccessRequest = visibleDataAccessRequests.getDARList(true).get(0);
		assertNotNull(manualDataAccessRequest);
		
		assertEquals(VisibleDataAccessRequests.MANUAL_DATA_REQUEST,manualDataAccessRequest.getMonitoringURL());
		List<Product> productList = manualDataAccessRequest.getProductList();
		assertEquals(1, productList.size());
		assertEquals(productList.get(0).getProductAccessUrl(), downloadUrl.toString());
	}

	@Test
	public void testAddManualDownloadTwoDownloads() throws MalformedURLException, ProductAlreadyExistsInDarException {
		visibleDataAccessRequests.addManualProductDownload(downloadUrl.toString());
		visibleDataAccessRequests.addManualProductDownload("http://ipv4.download.thinkbroadband.com/10MB.zip");

		DataAccessRequest manualDataAccessRequest = visibleDataAccessRequests.getDARList(true).get(0);
		assertEquals(2, manualDataAccessRequest.getProductList().size());
	}

	@Test
	public void testAddManualDownloadSameDownloadTwice() throws MalformedURLException, ProductAlreadyExistsInDarException {
		visibleDataAccessRequests.addManualProductDownload(downloadUrl.toString());
		//add of a second manual download, which should throw a ProductAlreadyExistsInDarException
		try {
			visibleDataAccessRequests.addManualProductDownload(downloadUrl.toString());
			fail("adding the same manual download twice should cause an exception to be thrown.");
		}catch(ProductAlreadyExistsInDarException ex) {
			assertEquals(String.format("Product %s already exists in DAR %s", downloadUrl.toString(), VisibleDataAccessRequests.MANUAL_DATA_REQUEST), ex.getLocalizedMessage());
		}
		
	}

	@Test
	public void testGetDataAccessRequestByUuid() throws ProductAlreadyExistsInDarException {
		visibleDataAccessRequests.addManualProductDownload(downloadUrl.toString());
		DataAccessRequest manualDataAccessRequest = visibleDataAccessRequests.getDARList(true).get(0);
		
		DataAccessRequest retrievedDAR = visibleDataAccessRequests.findDataAccessRequestByUuid(manualDataAccessRequest.getUuid());
		assertEquals(manualDataAccessRequest, retrievedDAR);
	}
	
	@Test
	public void testGetDataAccessRequestByUuidDARDoesNotExist() {
		String myUuid = "myUuid";
		try {
			visibleDataAccessRequests.findDataAccessRequestByUuid(myUuid);
			fail("DAR with uuid \"myUuid\" does not exist, an exception should be thrown here.");
		}catch(NonRecoverableException ex) {
			assertEquals(String.format("Unable to find Data Access Request for uuid %s", myUuid), ex.getLocalizedMessage());
		}
	}
}
