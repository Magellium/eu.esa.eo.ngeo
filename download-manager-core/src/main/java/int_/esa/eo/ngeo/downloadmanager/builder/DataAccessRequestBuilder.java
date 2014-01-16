package int_.esa.eo.ngeo.downloadmanager.builder;

import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;

import java.util.ArrayList;
import java.util.UUID;

public class DataAccessRequestBuilder {
    public DataAccessRequest buildDAR(String darURL, boolean monitored) {
        DataAccessRequest dataAccessRequest = new DataAccessRequest();
        dataAccessRequest.setDarURL(darURL);
        dataAccessRequest.setMonitored(monitored);
        dataAccessRequest.setUuid(UUID.randomUUID().toString());
        dataAccessRequest.setProductList(new ArrayList<Product>());
        dataAccessRequest.setMonitoringStatus(MonitoringStatus.IN_PROGRESS);
        dataAccessRequest.setVisible(true);
        return dataAccessRequest;
    }
}
