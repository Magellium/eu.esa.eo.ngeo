package int_.esa.eo.ngeo.dmtu.observer;

import int_.esa.eo.ngeo.dmtu.model.Product;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;

public interface ProductSubject {
	 void registerObserver(ProductObserver o);
	 void removeObserver(ProductObserver o);
	 void notifyObserversOfNewProduct(Product product);
	 void notifyObserversOfProductStatusUpdate(Product product, EDownloadStatus downloadStatus);
}
