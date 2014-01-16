package int_.esa.eo.ngeo.downloadmanager.download;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductDownloadThread implements Runnable {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ProductDownloadThread.class);

    private final IDownloadProcess downloadProcess;
    private final Product product;

    public ProductDownloadThread(IDownloadProcess downloadProcess, Product product) {
        this.downloadProcess = downloadProcess;
        this.product = product;
    }

    @Override
    public void run() {
        try {
            getDownloadProcess().startDownload();
        } catch (DMPluginException e) {
            throw new NonRecoverableException("Unable to start download.", e);
        }
    }


    public void pauseDownloadThread() {
        try {
            getDownloadProcess().pauseDownload();
            getProduct().setPausedByDownloadManager(true);
        } catch (DMPluginException e) {
            LOGGER.error("Unable to start download.", e);
        }
    }

    public Product getProduct() {
        return product;
    }

    public IDownloadProcess getDownloadProcess() {
        return downloadProcess;
    }
}