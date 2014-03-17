package int_.esa.eo.ngeo.downloadmanager.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.exception.ProductAlreadyExistsInDarException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class VisibleDataAccessRequestsTest {
	VisibleDataAccessRequests visibleDataAccessRequests = new VisibleDataAccessRequests();
	
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
		Product productDownload = visibleDataAccessRequests.addManualProductDownload(downloadUrl.toString(), ProductPriority.NORMAL);
		assertNotNull(productDownload);
		
		assertEquals(1, visibleDataAccessRequests.getDARList(true).size());
		assertEquals(0, visibleDataAccessRequests.getDARList(false).size());

		DataAccessRequest manualDataAccessRequest = visibleDataAccessRequests.getDARList(true).get(0);
		assertNotNull(manualDataAccessRequest);
		
		assertEquals(VisibleDataAccessRequests.MANUAL_PRODUCT_DAR, manualDataAccessRequest.getDarName());
		List<Product> productList = manualDataAccessRequest.getProductList();
		assertEquals(1, productList.size());
		assertEquals(productList.get(0).getProductAccessUrl(), downloadUrl.toString());
	}

	@Test
	public void testAddManualDownloadTwoDownloads() throws MalformedURLException, ProductAlreadyExistsInDarException {
		visibleDataAccessRequests.addManualProductDownload(downloadUrl.toString(), ProductPriority.NORMAL);
		visibleDataAccessRequests.addManualProductDownload("http://ipv4.download.thinkbroadband.com/10MB.zip", ProductPriority.NORMAL);

		DataAccessRequest manualDataAccessRequest = visibleDataAccessRequests.getDARList(true).get(0);
		assertEquals(2, manualDataAccessRequest.getProductList().size());
	}

	@Test
	public void testAddManualDownloadSameDownloadTwice() throws MalformedURLException, ProductAlreadyExistsInDarException {
		visibleDataAccessRequests.addManualProductDownload(downloadUrl.toString(), ProductPriority.NORMAL);
		//add of a second manual download, which should throw a ProductAlreadyExistsInDarException
		try {
			visibleDataAccessRequests.addManualProductDownload(downloadUrl.toString(), ProductPriority.NORMAL);
			fail("adding the same manual download twice should cause an exception to be thrown.");
		}catch(ProductAlreadyExistsInDarException ex) {
			assertEquals(String.format("Product %s already exists in Manual Data Access Request", downloadUrl.toString()), ex.getLocalizedMessage());
		}
		
	}

	@Test
	public void testGetDataAccessRequestByUuid() throws ProductAlreadyExistsInDarException {
		visibleDataAccessRequests.addManualProductDownload(downloadUrl.toString(), ProductPriority.NORMAL);
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
	
	@Test
	public void getDataAccessRequestWithMonitoringUrlTest() throws ProductAlreadyExistsInDarException {
	    DataAccessRequest dar = new DataAccessRequest();
	    dar.setDarURL("http://www.test.com/dar");
	    
        visibleDataAccessRequests.addDAR(dar);
        
        DataAccessRequest darToFind = new DataAccessRequest();
        assertNull(visibleDataAccessRequests.getDataAccessRequest(darToFind));
        
        darToFind.setDarURL("test");
        assertNull(visibleDataAccessRequests.getDataAccessRequest(darToFind));

        darToFind.setDarURL("http://www.test.com/dar");
        assertEquals(dar, visibleDataAccessRequests.getDataAccessRequest(darToFind));

        darToFind.setDarURL(null);
        darToFind.setDarName("test");
        assertNull(visibleDataAccessRequests.getDataAccessRequest(darToFind));
	}

    @Test
    public void getDataAccessRequestWithDarNameTest() throws ProductAlreadyExistsInDarException {
        DataAccessRequest dar = new DataAccessRequest();
        dar.setDarName("Test DAR");
        
        visibleDataAccessRequests.addDAR(dar);
        
        DataAccessRequest darToFind = new DataAccessRequest();
        assertNull(visibleDataAccessRequests.getDataAccessRequest(darToFind));

        darToFind.setDarURL("http://www.test.com/dar");
        assertNull(visibleDataAccessRequests.getDataAccessRequest(darToFind));

        darToFind.setDarURL(null);
        darToFind.setDarName("test");
        assertNull(visibleDataAccessRequests.getDataAccessRequest(darToFind));

        darToFind.setDarName("Test DAR");
        assertEquals(dar, visibleDataAccessRequests.getDataAccessRequest(darToFind));
    }

    @Test
    public void getDataAccessRequestWithMonitoringUrlAndDarNameTest() throws ProductAlreadyExistsInDarException {
        DataAccessRequest dar = new DataAccessRequest();
        dar.setDarURL("http://www.test.com/dar");
        dar.setDarName("Test DAR");
        
        visibleDataAccessRequests.addDAR(dar);
        
        DataAccessRequest darToFind = new DataAccessRequest();
        assertNull(visibleDataAccessRequests.getDataAccessRequest(darToFind));

        darToFind.setDarURL("http://www.test.com/dar");
        assertEquals(dar, visibleDataAccessRequests.getDataAccessRequest(darToFind));

        darToFind.setDarURL(null);
        darToFind.setDarName("test");
        assertNull(visibleDataAccessRequests.getDataAccessRequest(darToFind));
        
        darToFind.setDarName("Test DAR");
        assertEquals(dar, visibleDataAccessRequests.getDataAccessRequest(darToFind));
    }
}
