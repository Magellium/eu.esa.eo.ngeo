package int_.esa.eo.ngeo.dmtu.observer;

import int_.esa.eo.ngeo.dmtu.model.ProductProgress;

public interface DownloadObserver {
	void updateProgress(String productUuid, ProductProgress productProgress);
	void updateProductDetails(String productUuid, String productName, Integer numberOfFiles, Long overallSize);
}
