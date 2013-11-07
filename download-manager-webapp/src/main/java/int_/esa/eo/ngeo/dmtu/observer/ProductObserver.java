package int_.esa.eo.ngeo.dmtu.observer;

import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;

public interface ProductObserver {
	void newProduct(Product product);
	void updateProductDownloadStatus(Product product, EDownloadStatus downloadStatus);
}
