package int_.esa.eo.ngeo.dmtu.download.monitor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.dmtu.manager.DataAccessRequestManager;
import int_.esa.eo.ngeo.downloadmanager.builder.ProductBuilder;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DownloadMonitorTest {
	private DownloadMonitor downloadMonitor;
	@Mock private DataAccessRequestManager dataAccessRequestManager;
	
	private static final String FILE_URL = "http://download.tuxfamily.org/notepadplus/6.3.1/npp.6.3.1.bin.zip";
	private static final String MOCK_MONITORING_URL = "mockMonitoringURL";

	@Before
	public void setup() {
		downloadMonitor = new DownloadMonitor(dataAccessRequestManager);
	}

	@Test
	public void testUpdate() {
		DataAccessRequest dataAccessRequest = mock(DataAccessRequest.class);
		List<Product> productList = new ArrayList<Product>();
		productList.add(new ProductBuilder().buildProduct(FILE_URL));
		when(dataAccessRequest.getProductList()).thenReturn(productList);
		
		//XXX: uncommment and resolve
		//		downloadMonitor.update(dataAccessRequest);
		
//		assertEquals(1, downloadMonitor.getProductDownloads().size());
	}
}
