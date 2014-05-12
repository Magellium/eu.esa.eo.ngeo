package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.model.ProductProgress;
import int_.esa.eo.ngeo.downloadmanager.observer.DownloadObserver;
import int_.esa.eo.ngeo.downloadmanager.observer.DownloadSubject;

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
    public void notifyObserversOfProgress(String productUuid, ProductProgress productProgress) {
        for (DownloadObserver o : observers) {
            try {
                o.updateProgress(productUuid, productProgress);
            }catch (Throwable t) {
                LOGGER.error(String.format("Exception whilst notifying product progress to download subject: %s - %s", t.getClass().getName(), t.getMessage(), t));
            }
        }
    }

    @Override
    public void notifyObserversOfProductDetails(String productUuid, String productName, Integer numberOfFiles, Long overallSize) {
        for (DownloadObserver o : observers) {
            try {
                o.updateProductDetails(productUuid, productName, numberOfFiles, overallSize);
            }catch (Throwable t) {
                LOGGER.error(String.format("Exception whilst updating product details to download subject: %s - %s", t.getClass().getName(), t.getMessage(), t));
            }
        }
    }

    @Override
    public void productDetails(String productName, Integer numberOfFiles, Long overallSize) {
        notifyObserversOfProductDetails(productUuid, productName, numberOfFiles, overallSize);
    }

    @Override
    public void progress(Integer progressPercentage, Long downloadedSize,
            EDownloadStatus status, String message, DMPluginException exception) {
        //The DMTU does not use the DMPluginException exception for notifications
        progress(progressPercentage, downloadedSize, status, message);
    }
}
