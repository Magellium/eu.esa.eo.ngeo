package int_.esa.eo.ngeo.downloadmanager.observer;

import int_.esa.eo.ngeo.downloadmanager.model.ProductProgress;

public interface DownloadObserver {
    void updateProgress(String productUuid, ProductProgress productProgress);
    void updateProductDetails(String productUuid, String productName, Integer numberOfFiles, Long overallSize);
}
