package int_.esa.eo.ngeo.downloadmanager.log;

import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductProgress;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductTerminationLog {
    private static final String CSN_ENTRY = "\"%s\"";
    private static final String CSV_ENTRY_WITH_COMMA = ",\"%s\"";
    private static final Logger PRODUCT_DOWNLOAD_TERMINATION_NOTIFICATION_LOGGER = LoggerFactory.getLogger("ProductDownloadTermination");
    private static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public void notifyProductDownloadTermination(Product product, DataAccessRequest dataAccessRequest) {
        StringBuilder productNotificationString = new StringBuilder();
        ProductProgress productProgress = product.getProductProgress();
        EDownloadStatus productDownloadStatus = productProgress.getStatus();
        productNotificationString.append(String.format(CSN_ENTRY, productDownloadStatus));
        productNotificationString.append(String.format(CSV_ENTRY_WITH_COMMA, product.getProductAccessUrl()));
        productNotificationString.append(String.format(CSV_ENTRY_WITH_COMMA, dataAccessRequest.getDarURL()));
        long numberOfBytesDownloaded;
        if(productDownloadStatus == EDownloadStatus.IN_ERROR) {
            numberOfBytesDownloaded = 0;
        }else{
            numberOfBytesDownloaded = productProgress.getDownloadedSize();
        }
        productNotificationString.append(String.format(CSV_ENTRY_WITH_COMMA, numberOfBytesDownloaded));
        productNotificationString.append(String.format(CSV_ENTRY_WITH_COMMA, convertDateToString(product.getStartOfFirstDownloadRequest())));
        productNotificationString.append(String.format(CSV_ENTRY_WITH_COMMA, convertDateToString(product.getStartOfActualDownload())));
        productNotificationString.append(String.format(CSV_ENTRY_WITH_COMMA, convertDateToString(product.getStopOfDownload())));

        if(productDownloadStatus == EDownloadStatus.COMPLETED) {
            productNotificationString.append(String.format(CSV_ENTRY_WITH_COMMA, product.getCompletedDownloadPath()));
        }else{
            productNotificationString.append(",");
        }

        if(productDownloadStatus == EDownloadStatus.IN_ERROR) {
            productNotificationString.append(String.format(CSV_ENTRY_WITH_COMMA, productProgress.getMessage()));
        }else{
            productNotificationString.append(String.format(CSV_ENTRY_WITH_COMMA, ""));
        }

        PRODUCT_DOWNLOAD_TERMINATION_NOTIFICATION_LOGGER.info(productNotificationString.toString());
    }

    private String convertDateToString(Date date) {
        if(date == null) {
            return "";
        }
        DateFormat df = new SimpleDateFormat(UTC_DATE_FORMAT);
        return df.format(date);
    }
}
