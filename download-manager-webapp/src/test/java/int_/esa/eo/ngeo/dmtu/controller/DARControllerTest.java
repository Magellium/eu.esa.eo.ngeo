package int_.esa.eo.ngeo.dmtu.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.dmtu.download.monitor.DownloadMonitor;
import int_.esa.eo.ngeo.dmtu.exception.ProductAlreadyExistsInDarException;
import int_.esa.eo.ngeo.dmtu.manager.DataAccessRequestManager;
import int_.esa.eo.ngeo.downloadmanager.builder.DataAccessRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.builder.ProductBuilder;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DARControllerTest {
	@InjectMocks DARController darController = new DARController();
	@Mock DataAccessRequestManager dataAccessRequestManager;
	@Mock DownloadMonitor downloadMonitor;

	private static final String PRODUCT_URL_NOTEPAD_PLUSPLUS = "http://download.tuxfamily.org/notepadplus/6.3.1/npp.6.3.1.bin.zip";
	private static final String PRODUCT_URL_UBUNTU = "http://www.ubuntu.com/start-download?distro=desktop&bits=32&release=latest";
	private static final String MONITORING_URL = "http://dmtu.ngeo.eo.esa.int/monitoringUrl";

	@Test
	public void testAddManualDownload() throws ProductAlreadyExistsInDarException {
		//FIXME: Fix test
		String downloadUrl = "http://download.tuxfamily.org/notepadplus/6.3.1/npp.6.3.1.bin.zip";
		when(dataAccessRequestManager.addManualProductDownload(downloadUrl)).thenReturn(true);
		
		CommandResponse commandResponse = darController.addManualDownload(downloadUrl, null);
		assertTrue(commandResponse.isSuccess());
	}

	public List<DataAccessRequest> setupDataAccessRequestList() {
		List<DataAccessRequest> dataAccessRequestList;
		dataAccessRequestList = new ArrayList<DataAccessRequest>();
		DataAccessRequest dataAccessRequest = new DataAccessRequestBuilder().buildDAR(MONITORING_URL, true);
		dataAccessRequest.getProductList().add(new ProductBuilder().buildProduct(PRODUCT_URL_NOTEPAD_PLUSPLUS));
		dataAccessRequest.getProductList().add(new ProductBuilder().buildProduct(PRODUCT_URL_UBUNTU));
		
		dataAccessRequestList.add(dataAccessRequest);
		return dataAccessRequestList;
	}
	
	@Test
	public void testGetDataAccessRequests() {
		List<DataAccessRequest> dataAccessRequestList = setupDataAccessRequestList();
		
		when(dataAccessRequestManager.getVisibleDARList(true)).thenReturn(dataAccessRequestList);
		
		List<DataAccessRequest> dataAccessRequests = darController.getDataAccessRequestStatus().getDataAccessRequests();
		assertEquals(1,dataAccessRequests.size());
		DataAccessRequest dataAccessRequest = dataAccessRequests.get(0);
		assertEquals(MONITORING_URL, dataAccessRequest.getDarURL());
	}

	@Test
	public void testGetProducts() {
		List<DataAccessRequest> dataAccessRequestList = setupDataAccessRequestList();
		when(dataAccessRequestManager.getProductList(dataAccessRequestList.get(0).getUuid())).thenReturn(new ArrayList<Product>(dataAccessRequestList.get(0).getProductList()));
		
		List<Product> productsFromDar = darController.getProducts(dataAccessRequestList.get(0).getUuid());
		assertEquals(2, productsFromDar.size());
		List<String> productUrlsWhichShouldBeInProductListFromDar = new ArrayList<>(); 
		productUrlsWhichShouldBeInProductListFromDar.add(PRODUCT_URL_NOTEPAD_PLUSPLUS);
		productUrlsWhichShouldBeInProductListFromDar.add(PRODUCT_URL_UBUNTU);
		
		for (Product product : productsFromDar) {
			if(!productUrlsWhichShouldBeInProductListFromDar.remove(product.getProductAccessUrl())) {
				fail(String.format("%s is in the product list from the DAR, but should not be.", product.getProductAccessUrl()));
			}
		}
		
		if(productUrlsWhichShouldBeInProductListFromDar.size() > 0) {
			fail(String.format("%s should be in the product list from the DAR.", productUrlsWhichShouldBeInProductListFromDar));			
		}
	}
}
