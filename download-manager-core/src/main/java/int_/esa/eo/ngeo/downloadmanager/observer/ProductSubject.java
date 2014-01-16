package int_.esa.eo.ngeo.downloadmanager.observer;

import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;

public interface ProductSubject {
    void registerObserver(ProductObserver o);
    void notifyObserversOfNewProduct(Product product);
    void notifyObserversOfProductStatusUpdate(Product product, EDownloadStatus downloadStatus);
}
