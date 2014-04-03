package int_.esa.eo.ngeo.dmtu.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.builder.DataAccessRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.builder.ProductBuilder;
import int_.esa.eo.ngeo.downloadmanager.dar.DataAccessRequestManager;
import int_.esa.eo.ngeo.downloadmanager.download.DownloadMonitor;
import int_.esa.eo.ngeo.downloadmanager.exception.ProductAlreadyExistsInDarException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithDarDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
	public void testAddManualProductDownload() throws ProductAlreadyExistsInDarException {
		String downloadUrl = "http://download.tuxfamily.org/notepadplus/6.3.1/npp.6.3.1.bin.zip";
        String darUuid = UUID.randomUUID().toString();
        String productUuid = UUID.randomUUID().toString();
        Pair<String, String> darAndProductUuidPair = new ImmutablePair<>(darUuid, productUuid);
		when(dataAccessRequestManager.addManualProductDownload(downloadUrl, ProductPriority.NORMAL)).thenReturn(darAndProductUuidPair);
		
		CommandResponseWithDarDetails commandResponse = darController.addManualProductDownload(mock(HttpServletResponse.class), downloadUrl, ProductPriority.NORMAL);
		assertTrue(commandResponse.isSuccess());
        assertEquals(darUuid, commandResponse.getDarUuid());
        assertEquals(productUuid, commandResponse.getProductUuid());
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
	
	@Test
	public void testGetProducts() {
		List<DataAccessRequest> dataAccessRequestList = setupDataAccessRequestList();
		when(dataAccessRequestManager.getProductList(dataAccessRequestList.get(0).getUuid())).thenReturn(new ArrayList<Product>(dataAccessRequestList.get(0).getProductList()));
		
		List<Product> productsFromDar = darController.getProducts(dataAccessRequestList.get(0).getUuid(), mock(HttpServletResponse.class));
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
