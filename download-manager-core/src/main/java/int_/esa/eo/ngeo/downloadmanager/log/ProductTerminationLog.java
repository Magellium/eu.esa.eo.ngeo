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
	private static final Logger PRODUCT_DOWNLOAD_TERMINATION_NOTIFICATION_LOGGER = LoggerFactory.getLogger("ProductDownloadTermination");
	private static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	
	public void notifyProductDownloadTermination(Product product, DataAccessRequest dataAccessRequest) {
		StringBuilder productNotificationString = new StringBuilder();
		ProductProgress productProgress = product.getProductProgress();
		EDownloadStatus productDownloadStatus = productProgress.getStatus();
		productNotificationString.append(String.format("\"%s\"", productDownloadStatus));
		productNotificationString.append(String.format(",\"%s\"", product.getProductAccessUrl()));
		productNotificationString.append(String.format(",\"%s\"", dataAccessRequest.getDarURL()));
		long numberOfBytesDownloaded;
		if(productDownloadStatus == EDownloadStatus.IN_ERROR) {
			numberOfBytesDownloaded = 0;
		}else{
			numberOfBytesDownloaded = productProgress.getDownloadedSize();
		}
		productNotificationString.append(String.format(",\"%s\"", numberOfBytesDownloaded));
		productNotificationString.append(String.format(",\"%s\"", convertDateToString(product.getStartOfFirstDownloadRequest()))); //TODO: Start date/time of first download request (i.e. when the initial http get request is made)
		productNotificationString.append(String.format(",\"%s\"", convertDateToString(product.getStartOfActualDownload()))); //TODO: Start Date/Time of actual download (if the download is delayed, this time is different from the previous one)
		productNotificationString.append(String.format(",\"%s\"", convertDateToString(product.getStopOfDownload()))); //TODO: Stop Date/Time of download

		if(productDownloadStatus == EDownloadStatus.COMPLETED) {
			productNotificationString.append(String.format(",\"%s\"", product.getCompletedDownloadPath())); //TODO: The Path of the saved product (if completed)
		}else{
			productNotificationString.append(",");
		}
		
		if(productDownloadStatus == EDownloadStatus.IN_ERROR) {
			productNotificationString.append(String.format(",\"%s\"", productProgress.getMessage()));
		}else{
			productNotificationString.append(String.format(",\"%s\"", ""));
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
