package int_.esa.eo.ngeo.downloadmanager.webserver.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductProgress;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessSubsetting;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductDownloadNotification;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductDownloadStatus;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;

public class NgeoWebServerRequestBuilderTest {
    private static final String DOWNLOAD_MANAGER_ID = "80dcf46b4f4d4daaae606f4a605f06b5";
    private static final String DOWNLOAD_MANAGER_FRIENDLY_NAME = "Test Download Manager";

    private NgeoWebServerRequestBuilder ngeoWebServerRequestBuilder;
    
    @Before
    public void setup() {
        ngeoWebServerRequestBuilder = new NgeoWebServerRequestBuilder();
    }
    
    @Test
    public void buildDMRegistrationMgmntRequestTest() {
        DMRegistrationMgmntRequ dmRegistrationMgmntRequest = ngeoWebServerRequestBuilder.buildDMRegistrationMgmntRequest(DOWNLOAD_MANAGER_ID, DOWNLOAD_MANAGER_FRIENDLY_NAME);
        assertEquals(DOWNLOAD_MANAGER_ID, dmRegistrationMgmntRequest.getDownloadManagerId());
        assertEquals(DOWNLOAD_MANAGER_FRIENDLY_NAME, dmRegistrationMgmntRequest.getDownloadManagerFriendlyName());
    }

    @Test
    public void buildMonitoringURLRequestTest() {
        GregorianCalendar calendar = new GregorianCalendar(2013,1,28,13,24,56);
        MonitoringURLRequ monitoringURLRequest = ngeoWebServerRequestBuilder.buildMonitoringURLRequest(DOWNLOAD_MANAGER_ID, calendar);
        assertEquals(DOWNLOAD_MANAGER_ID, monitoringURLRequest.getDownloadManagerId());
        XMLGregorianCalendar downloadManagerSetTime = monitoringURLRequest.getDownloadManagerSetTime();
        assertNotNull(downloadManagerSetTime);
        assertEquals(2013, downloadManagerSetTime.getYear());
        assertEquals(2, downloadManagerSetTime.getMonth());
        assertEquals(28, downloadManagerSetTime.getDay());
        assertEquals(13, downloadManagerSetTime.getHour());
        assertEquals(24, downloadManagerSetTime.getMinute());
        assertEquals(56, downloadManagerSetTime.getSecond());
    }

    @Test
    public void buildMonitoringURLRequestNullTimeTest() {
        MonitoringURLRequ monitoringURLRequest = ngeoWebServerRequestBuilder.buildMonitoringURLRequest(DOWNLOAD_MANAGER_ID, null);
        assertEquals(DOWNLOAD_MANAGER_ID, monitoringURLRequest.getDownloadManagerId());
        assertNull(monitoringURLRequest.getDownloadManagerSetTime());
    }

    @Test
    public void buildDataAccessMonitoringRequTest() {
        DataAccessRequest dataAccessRequest = new DataAccessRequest();
        dataAccessRequest.setProductList(null);
        dataAccessRequest.setLastResponseDate(new Timestamp(1396011580000L));
        ProductAccessStatus productAccessStatus = ProductAccessStatus.READY_ACCESSED;
        DataAccessMonitoringRequ dataAccessMonitoringRequ = ngeoWebServerRequestBuilder.buildDataAccessMonitoringRequ(DOWNLOAD_MANAGER_ID, dataAccessRequest, productAccessStatus);
        assertEquals(DOWNLOAD_MANAGER_ID, dataAccessMonitoringRequ.getDownloadManagerId());
        ProductAccessSubsetting productAccessSubsetting = dataAccessMonitoringRequ.getProductAccessSubsetting();
        assertEquals(ProductAccessStatus.READY_ACCESSED, productAccessSubsetting.getReadyProductsOrAll());
        assertNotNull(productAccessSubsetting.getDownloadManagerSetTime());
        
        XMLGregorianCalendar downloadManagerSetTime = productAccessSubsetting.getDownloadManagerSetTime();
        assertNotNull(downloadManagerSetTime);
        assertEquals(2014, downloadManagerSetTime.getYear());
        assertEquals(3, downloadManagerSetTime.getMonth());
        assertEquals(28, downloadManagerSetTime.getDay());
        assertEquals(12, downloadManagerSetTime.getHour());
        assertEquals(59, downloadManagerSetTime.getMinute());
        assertEquals(40, downloadManagerSetTime.getSecond());

        assertNull(dataAccessMonitoringRequ.getProductDownloadNotificationList());
    }

    @Test
    public void buildDataAccessMonitoringRequNullResponseDateTest() {
        DataAccessRequest dataAccessRequest = new DataAccessRequest();
        ProductAccessStatus productAccessStatus = ProductAccessStatus.READY_ACCESSED;
        DataAccessMonitoringRequ dataAccessMonitoringRequ = ngeoWebServerRequestBuilder.buildDataAccessMonitoringRequ(DOWNLOAD_MANAGER_ID, dataAccessRequest, productAccessStatus);
        assertEquals(DOWNLOAD_MANAGER_ID, dataAccessMonitoringRequ.getDownloadManagerId());
        ProductAccessSubsetting productAccessSubsetting = dataAccessMonitoringRequ.getProductAccessSubsetting();
        assertNotNull(productAccessSubsetting);
        assertEquals(ProductAccessStatus.READY_ACCESSED, productAccessSubsetting.getReadyProductsOrAll());
        assertNull(productAccessSubsetting.getDownloadManagerSetTime());
        assertEquals(0, dataAccessMonitoringRequ.getProductDownloadNotificationList().getProductDownloadNotifications().size());
    }

    @Test
    public void buildDataAccessMonitoringRequNullProductAccessStatusTest() {
        DataAccessRequest dataAccessRequest = new DataAccessRequest();
        dataAccessRequest.setLastResponseDate(new Timestamp(new Date().getTime()));

        DataAccessMonitoringRequ dataAccessMonitoringRequ = ngeoWebServerRequestBuilder.buildDataAccessMonitoringRequ(DOWNLOAD_MANAGER_ID, dataAccessRequest, null);
        assertEquals(DOWNLOAD_MANAGER_ID, dataAccessMonitoringRequ.getDownloadManagerId());
        ProductAccessSubsetting productAccessSubsetting = dataAccessMonitoringRequ.getProductAccessSubsetting();
        assertEquals(ProductAccessStatus.READY_ACCESSED, productAccessSubsetting.getReadyProductsOrAll());
        assertEquals(0, dataAccessMonitoringRequ.getProductDownloadNotificationList().getProductDownloadNotifications().size());
    }

    @Test
    public void buildDataAccessMonitoringRequWithProductsTest() {
        DataAccessRequest dataAccessRequest = new DataAccessRequest();
        ProductAccessStatus productAccessStatus = ProductAccessStatus.READY_ACCESSED;

        List<Product> productList = buildProductList();
        dataAccessRequest.setProductList(productList);
        
        DataAccessMonitoringRequ dataAccessMonitoringRequ = ngeoWebServerRequestBuilder.buildDataAccessMonitoringRequ(DOWNLOAD_MANAGER_ID, dataAccessRequest, productAccessStatus);
        assertEquals(DOWNLOAD_MANAGER_ID, dataAccessMonitoringRequ.getDownloadManagerId());
        ProductAccessSubsetting productAccessSubsetting = dataAccessMonitoringRequ.getProductAccessSubsetting();
        assertEquals(ProductAccessStatus.READY_ACCESSED, productAccessSubsetting.getReadyProductsOrAll());
        assertNull(productAccessSubsetting.getDownloadManagerSetTime());
        
        List<ProductDownloadNotification> productDownloadNotifications = dataAccessMonitoringRequ.getProductDownloadNotificationList().getProductDownloadNotifications();
        assertEquals(7, productDownloadNotifications.size());
        
        verifyProductList(productList, productDownloadNotifications);
    }
    
    private List<Product> buildProductList() {
        List<Product> productList = new ArrayList<>();

        Product notStartedProduct = new Product();
        notStartedProduct.setProductAccessUrl("not started product access URL");
        ProductProgress productProgress = new ProductProgress();
        productProgress.setStatus(EDownloadStatus.NOT_STARTED);
        notStartedProduct.setProductProgress(productProgress);
        productList.add(notStartedProduct);

        Product idleProduct = new Product();
        idleProduct.setProductAccessUrl("idle product access URL");
        ProductProgress idleProductProgress = new ProductProgress();
        idleProductProgress.setStatus(EDownloadStatus.IDLE);
        idleProduct.setProductProgress(idleProductProgress);
        productList.add(idleProduct);

        Product runningProduct = new Product();
        runningProduct.setProductAccessUrl("running product access URL");
        runningProduct.setTotalFileSize(61725);
        ProductProgress runningProductProgress = new ProductProgress();
        runningProductProgress.setStatus(EDownloadStatus.RUNNING);
        runningProductProgress.setDownloadedSize(12345);
        runningProductProgress.setProgressPercentage(20);
        runningProduct.setProductProgress(runningProductProgress);
        productList.add(runningProduct);

        Product pausedProduct = new Product();
        pausedProduct.setProductAccessUrl("paused product access URL");
        pausedProduct.setTotalFileSize(61725);
        ProductProgress pausedProductProgress = new ProductProgress();
        pausedProductProgress.setStatus(EDownloadStatus.PAUSED);
        pausedProductProgress.setDownloadedSize(22221);
        pausedProductProgress.setProgressPercentage(36);
        pausedProduct.setProductProgress(pausedProductProgress);
        productList.add(pausedProduct);

        Product cancelledProduct = new Product();
        cancelledProduct.setProductAccessUrl("cancelled product access URL");
        cancelledProduct.setTotalFileSize(61725);
        ProductProgress cancelledProductProgress = new ProductProgress();
        cancelledProductProgress.setStatus(EDownloadStatus.CANCELLED);
        cancelledProductProgress.setDownloadedSize(0);
        cancelledProductProgress.setProgressPercentage(0);
        cancelledProduct.setProductProgress(cancelledProductProgress);
        productList.add(cancelledProduct);

        Product inErrorProduct = new Product();
        inErrorProduct.setProductAccessUrl("In error product access URL");
        inErrorProduct.setTotalFileSize(61725);
        ProductProgress inErrorProductProgress = new ProductProgress();
        inErrorProductProgress.setStatus(EDownloadStatus.IN_ERROR);
        inErrorProductProgress.setDownloadedSize(0);
        inErrorProductProgress.setProgressPercentage(0);
        inErrorProductProgress.setMessage("Invalid authentication credentials.");
        inErrorProduct.setProductProgress(inErrorProductProgress);
        productList.add(inErrorProduct);

        Product completedProduct = new Product();
        completedProduct.setProductAccessUrl("completed product access URL");
        completedProduct.setTotalFileSize(61725);
        ProductProgress completedProductProgress = new ProductProgress();
        completedProductProgress.setStatus(EDownloadStatus.COMPLETED);
        completedProductProgress.setDownloadedSize(61725);
        completedProductProgress.setProgressPercentage(100);
        completedProduct.setProductProgress(completedProductProgress);
        productList.add(completedProduct);

        Product completedAndNotifiedProduct = new Product();
        completedAndNotifiedProduct.setProductAccessUrl("completed and notified product access URL");
        completedAndNotifiedProduct.setTotalFileSize(61725);
        completedAndNotifiedProduct.setNotified(true);
        ProductProgress completedAndNotifiedProductProgress = new ProductProgress();
        completedAndNotifiedProductProgress.setStatus(EDownloadStatus.COMPLETED);
        completedAndNotifiedProductProgress.setDownloadedSize(61725);
        completedAndNotifiedProductProgress.setProgressPercentage(100);
        completedAndNotifiedProduct.setProductProgress(completedAndNotifiedProductProgress);
        productList.add(completedAndNotifiedProduct);

        Product noProgressProduct = new Product();
        noProgressProduct.setProductAccessUrl("no progress product access URL");
        noProgressProduct.setTotalFileSize(61725);
        productList.add(noProgressProduct);

        return productList;
    }

    private void verifyProductList(List<Product> productList, List<ProductDownloadNotification> productDownloadNotifications) {
        assertEquals(productList.get(0).getProductAccessUrl(), productDownloadNotifications.get(0).getProductAccessURL());
        assertEquals(ProductDownloadStatus.NOT_STARTED, productDownloadNotifications.get(0).getProductDownloadStatus());
        assertEquals("Product download has not started.", productDownloadNotifications.get(0).getProductDownloadMessage());
        assertNull(productDownloadNotifications.get(0).getProductDownloadProgress());
        assertEquals(0, productDownloadNotifications.get(0).getProductDownloadSize().intValue());
        
        assertEquals(productList.get(1).getProductAccessUrl(), productDownloadNotifications.get(1).getProductAccessURL());
        assertEquals(ProductDownloadStatus.NOT_STARTED, productDownloadNotifications.get(1).getProductDownloadStatus());
        assertEquals("Product download has been requested, but product is not ready at this time.", productDownloadNotifications.get(1).getProductDownloadMessage());
        assertNull(productDownloadNotifications.get(1).getProductDownloadProgress());
        assertEquals(0, productDownloadNotifications.get(1).getProductDownloadSize().intValue());

        assertEquals(productList.get(2).getProductAccessUrl(), productDownloadNotifications.get(2).getProductAccessURL());
        assertEquals(ProductDownloadStatus.DOWNLOADING, productDownloadNotifications.get(2).getProductDownloadStatus());
        assertEquals("Product download in progress.", productDownloadNotifications.get(2).getProductDownloadMessage());
        assertEquals(20, productDownloadNotifications.get(2).getProductDownloadProgress().intValue());
        assertEquals(61725, productDownloadNotifications.get(2).getProductDownloadSize().intValue());

        assertEquals(productList.get(3).getProductAccessUrl(), productDownloadNotifications.get(3).getProductAccessURL());
        assertEquals(ProductDownloadStatus.DOWNLOADING, productDownloadNotifications.get(3).getProductDownloadStatus());
        assertEquals("Product download paused.", productDownloadNotifications.get(3).getProductDownloadMessage());
        assertEquals(36, productDownloadNotifications.get(3).getProductDownloadProgress().intValue());
        assertEquals(61725, productDownloadNotifications.get(3).getProductDownloadSize().intValue());

        assertEquals(productList.get(4).getProductAccessUrl(), productDownloadNotifications.get(4).getProductAccessURL());
        assertEquals(ProductDownloadStatus.COMPLETED, productDownloadNotifications.get(4).getProductDownloadStatus());
        assertEquals("Product cancelled.", productDownloadNotifications.get(4).getProductDownloadMessage());
        assertEquals(0, productDownloadNotifications.get(4).getProductDownloadProgress().intValue());
        assertEquals(61725, productDownloadNotifications.get(4).getProductDownloadSize().intValue());

        assertEquals(productList.get(5).getProductAccessUrl(), productDownloadNotifications.get(5).getProductAccessURL());
        assertEquals(ProductDownloadStatus.ERROR, productDownloadNotifications.get(5).getProductDownloadStatus());
        assertEquals("Invalid authentication credentials.", productDownloadNotifications.get(5).getProductDownloadMessage());
        assertNull(productDownloadNotifications.get(5).getProductDownloadProgress());
        assertEquals(61725, productDownloadNotifications.get(5).getProductDownloadSize().intValue());

        assertEquals(productList.get(6).getProductAccessUrl(), productDownloadNotifications.get(6).getProductAccessURL());
        assertEquals(ProductDownloadStatus.COMPLETED, productDownloadNotifications.get(6).getProductDownloadStatus());
        assertEquals("Product completed.", productDownloadNotifications.get(6).getProductDownloadMessage());
        assertEquals(100, productDownloadNotifications.get(6).getProductDownloadProgress().intValue());
        assertEquals(61725, productDownloadNotifications.get(6).getProductDownloadSize().intValue());
    }

    @Test
    public void buildDataAccessMonitoringRequWithProductNoStatusTest() {
        DataAccessRequest dataAccessRequest = new DataAccessRequest();
        ProductAccessStatus productAccessStatus = ProductAccessStatus.READY_ACCESSED;

        List<Product> productList = new ArrayList<>();

        Product noStatusProduct = new Product();
        noStatusProduct.setProductAccessUrl("no status product access URL");
        ProductProgress productProgress = new ProductProgress();
        noStatusProduct.setProductProgress(productProgress);
        productList.add(noStatusProduct);

        dataAccessRequest.setProductList(productList);
        
        DataAccessMonitoringRequ dataAccessMonitoringRequ = ngeoWebServerRequestBuilder.buildDataAccessMonitoringRequ(DOWNLOAD_MANAGER_ID, dataAccessRequest, productAccessStatus);
        assertEquals(DOWNLOAD_MANAGER_ID, dataAccessMonitoringRequ.getDownloadManagerId());
        ProductAccessSubsetting productAccessSubsetting = dataAccessMonitoringRequ.getProductAccessSubsetting();
        assertEquals(ProductAccessStatus.READY_ACCESSED, productAccessSubsetting.getReadyProductsOrAll());
        assertNull(productAccessSubsetting.getDownloadManagerSetTime());
        
        List<ProductDownloadNotification> productDownloadNotifications = dataAccessMonitoringRequ.getProductDownloadNotificationList().getProductDownloadNotifications();
        assertEquals(0, productDownloadNotifications.size());
    }

}
