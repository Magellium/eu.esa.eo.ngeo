package int_.esa.eo.ngeo.downloadmanager.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Priority;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductProgress;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.rest.StatusResponse;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

public class StatusResponseTransformerTest {
    @Test
    public void testParseStatusResponseJSON() throws JsonParseException, JsonMappingException, IOException {
    	InputStream resourceAsStream = this.getClass().getResourceAsStream("statusResponse.sample.json");
    	StatusResponse statusResponse = JSONTransformer.getInstance().deserialize(resourceAsStream, StatusResponse.class);

    	List<DataAccessRequest> dataAccessRequests = statusResponse.getDataAccessRequests();
    	assertNotNull(dataAccessRequests);
    	assertEquals(1, dataAccessRequests.size());
    	
    	DataAccessRequest dataAccessRequest = dataAccessRequests.get(0);
    	assertEquals("f392d546-b419-465e-b8c3-3f1ddff10323", dataAccessRequest.getUuid());
    	assertEquals("Manual Data Access Request", dataAccessRequest.getDarURL());
    	assertEquals(MonitoringStatus.IN_PROGRESS, dataAccessRequest.getMonitoringStatus());
    	assertFalse(dataAccessRequest.isMonitored());
    	assertNull(dataAccessRequest.getLastResponseDate());
    	assertTrue(dataAccessRequest.isVisible());
    	
    	List<Product> productList = dataAccessRequest.getProductList();
    	assertNotNull(productList);
    	assertEquals(1, productList.size());
    	
    	Product product = productList.get(0);
    	assertEquals("0ba7d260-0de0-4ba5-af59-72e2062fd5ad", product.getUuid());
    	assertEquals("http://mirror.slitaz.org/iso/4.0/slitaz-4.0.iso", product.getProductAccessUrl());
    	assertNull(product.getDownloadDirectory());
    	assertEquals("C:\\Users\\lkn\\ngEO-Downloads\\slitaz-4.0 (5).iso", product.getCompletedDownloadPath());
    	assertEquals(-1, product.getTotalFileSize());
    	assertFalse(product.isNotified());
    	assertEquals(1, product.getNumberOfFiles());
    	assertEquals("slitaz-4.0 (5).iso", product.getProductName());
    	assertEquals(Priority.NORMAL, product.getPriority());
    	assertEquals(1384516074957L, product.getStartOfFirstDownloadRequest().getTime());
    	assertEquals(1384516074968L, product.getStartOfActualDownload().getTime());
    	assertEquals(1384516108385L, product.getStopOfDownload().getTime());
    	assertTrue(product.isVisible());
    	
    	ProductProgress productProgress = product.getProductProgress();
    	assertNotNull(productProgress);
    	assertEquals(35857862, productProgress.getDownloadedSize());
    	assertNull(productProgress.getMessage());
    	assertEquals(100, productProgress.getProgressPercentage());
    	assertEquals(EDownloadStatus.COMPLETED, productProgress.getStatus());
    	
    }
}
