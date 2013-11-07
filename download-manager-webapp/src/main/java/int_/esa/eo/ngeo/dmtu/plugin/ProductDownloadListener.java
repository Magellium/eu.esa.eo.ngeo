package int_.esa.eo.ngeo.dmtu.plugin;

import int_.esa.eo.ngeo.dmtu.observer.DownloadObserver;
import int_.esa.eo.ngeo.dmtu.observer.DownloadSubject;
import int_.esa.eo.ngeo.downloadmanager.model.ProductProgress;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.IProductDownloadListener;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductDownloadListener implements IProductDownloadListener, DownloadSubject {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductDownloadListener.class);

	private List<DownloadObserver> observers;

	public static final String MANUAL_DATA_REQUEST = "Manual Data Request";

	private String productUuid;
	
	public ProductDownloadListener(String productUuid) {
		this.productUuid = productUuid;
		this.observers = new ArrayList<DownloadObserver>();
	}
	
	@Override
	public void progress(Integer progressPercentage, Long downloadedSize,
			EDownloadStatus status, String message) {
		ProductProgress productProgress = new ProductProgress();
		productProgress.setProgressPercentage(progressPercentage);
		productProgress.setDownloadedSize(downloadedSize);
		productProgress.setStatus(status);
		productProgress.setMessage(message);

		notifyObserversOfProgress(productUuid, productProgress);
	}
	
	@Override
	public void registerObserver(DownloadObserver o) {
		this.observers.add(o);
	}

	@Override
	public void removeObserver(DownloadObserver o) {
		int i = observers.indexOf(o);
		if(i >= 0) {
			observers.remove(i);
		}
	}

	@Override
	public void notifyObserversOfProgress(String productUuid, ProductProgress productProgress) {
		for (DownloadObserver o : observers) {
			o.updateProgress(productUuid, productProgress);
		}
	}

	@Override
	public void notifyObserversOfProductDetails(String productUuid, String productName, Integer numberOfFiles, Long overallSize) {
		for (DownloadObserver o : observers) {
			o.updateProductDetails(productUuid, productName, numberOfFiles, overallSize);
		}
	}

	@Override
	public void productDetails(String productName, Integer numberOfFiles, Long overallSize) {
		notifyObserversOfProductDetails(productUuid, productName, numberOfFiles, overallSize);
	}
}
