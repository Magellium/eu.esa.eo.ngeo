package int_.esa.eo.ngeo.downloadmanager.model.dao;

import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;

import java.util.List;

public interface DataAccessRequestDao {
    List<DataAccessRequest> loadVisibleDars();
    DataAccessRequest getDarByMonitoringUrl(String darURL);
    void updateDataAccessRequest(DataAccessRequest dataAccessRequest);
    void updateProduct(Product product);
}
