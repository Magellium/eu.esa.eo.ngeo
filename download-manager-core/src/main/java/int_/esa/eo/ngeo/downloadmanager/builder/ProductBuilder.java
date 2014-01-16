package int_.esa.eo.ngeo.downloadmanager.builder;

import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.model.ProductProgress;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class ProductBuilder {
    public Product buildProduct(String productAccessUrl) {
        return buildProduct(productAccessUrl, null);
    }

    public Product buildProduct(String productAccessUrl, String downloadDirectory) {
        Product product = new Product();
        product.setProductAccessUrl(productAccessUrl);
        product.setUuid(UUID.randomUUID().toString());
        product.setCreationTimestamp(new Timestamp(new Date().getTime()));
        product.setNotified(false);
        product.setDownloadDirectory(downloadDirectory);
        product.setPriority(ProductPriority.NORMAL);
        product.setVisible(true);

        ProductProgress productProgress = new ProductProgress();
        productProgress.setProgressPercentage(0);
        productProgress.setDownloadedSize(0);
        productProgress.setStatus(EDownloadStatus.NOT_STARTED);
        productProgress.setMessage(null);

        product.setProductProgress(productProgress);
        return product;
    }
}

