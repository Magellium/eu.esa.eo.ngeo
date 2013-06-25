package int_.esa.eo.ngeo.dmtu.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.dmtu.manager.DataAccessRequestManager;
import int_.esa.eo.ngeo.dmtu.model.DataAccessRequest;
import int_.esa.eo.ngeo.dmtu.model.Product;
import int_.esa.eo.ngeo.dmtu.monitor.DownloadMonitor;

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
	public void testAddManualDownload() {
		//FIXME: Fix test
		String downloadUrl = "http://download.tuxfamily.org/notepadplus/6.3.1/npp.6.3.1.bin.zip";
//		when(dataAccessRequestManager.addManualProductDownload(downloadUrl)).thenReturn(new DataAccessRequest(downloadUrl));
//		
//		CommandResponse dataAccessRequest = darController.addManualProductDownload(downloadUrl);
//		assertEquals(downloadUrl, dataAccessRequest.getMonitoringURL());
	}

	public List<DataAccessRequest> setupDataAccessRequestList() {
		List<DataAccessRequest> dataAccessRequestList;
		dataAccessRequestList = new ArrayList<DataAccessRequest>();
		DataAccessRequest dataAccessRequest = new DataAccessRequest(MONITORING_URL);
		dataAccessRequest.addProduct(new Product(PRODUCT_URL_NOTEPAD_PLUSPLUS));
		dataAccessRequest.addProduct(new Product(PRODUCT_URL_UBUNTU));
		
		dataAccessRequestList.add(dataAccessRequest);
		return dataAccessRequestList;
	}
	
	@Test
	public void testGetDataAccessRequests() {
		List<DataAccessRequest> dataAccessRequestList = setupDataAccessRequestList();
		
		when(dataAccessRequestManager.getVisibleDARList(true)).thenReturn(dataAccessRequestList);
		
		List<DataAccessRequest> dataAccessRequests = darController.getDataAccessRequests();
		assertEquals(1,dataAccessRequests.size());
		DataAccessRequest dataAccessRequest = dataAccessRequests.get(0);
		assertEquals(MONITORING_URL, dataAccessRequest.getMonitoringURL());
	}

	@Test
	public void testGetProducts() {
		List<DataAccessRequest> dataAccessRequestList = setupDataAccessRequestList();
		when(dataAccessRequestManager.getProductList(dataAccessRequestList.get(0).getUuid())).thenReturn(dataAccessRequestList.get(0).getProductList());
		
		List<Product> products = darController.getProducts(dataAccessRequestList.get(0).getUuid());
		assertEquals(2, products.size());
		assertEquals(PRODUCT_URL_NOTEPAD_PLUSPLUS, products.get(0).getProductAccessUrl());
		assertEquals(PRODUCT_URL_UBUNTU, products.get(1).getProductAccessUrl());
	}
}
