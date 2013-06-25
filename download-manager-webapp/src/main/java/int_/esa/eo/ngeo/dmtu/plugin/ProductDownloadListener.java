package int_.esa.eo.ngeo.dmtu.plugin;

import int_.esa.eo.ngeo.dmtu.model.ProductProgress;
import int_.esa.eo.ngeo.dmtu.observer.DownloadObserver;
import int_.esa.eo.ngeo.dmtu.observer.DownloadSubject;
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
		ProductProgress productProgress = new ProductProgress(progressPercentage, downloadedSize, status, message);
		notifyObservers(productUuid, productProgress);
		
//		String downloadedSizeString = (downloadedSize == null) ? "": downloadedSize.toString();
//		String progressPercentageString = (progressPercentage == null) ? "": progressPercentage.toString();
//		String statusString = (status == null) ? "": status.toString();
//		LOGGER.debug(String.format("progress notification: %s (%s %%) %s, %s",downloadedSizeString,progressPercentageString,statusString,message));
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
	public void notifyObservers(String productUuid, ProductProgress productProgress) {
		for (DownloadObserver o : observers) {
			o.updateProgress(productUuid, productProgress);
		}
		
	}
}
