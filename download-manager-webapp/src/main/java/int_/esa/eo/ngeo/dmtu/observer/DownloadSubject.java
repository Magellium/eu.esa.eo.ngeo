package int_.esa.eo.ngeo.dmtu.observer;

import int_.esa.eo.ngeo.dmtu.model.ProductProgress;

public interface DownloadSubject {
	 void registerObserver(DownloadObserver o);
	 void removeObserver(DownloadObserver o);
	 void notifyObservers(String productUuid, ProductProgress productProgress);
}
