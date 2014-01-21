package int_.esa.eo.ngeo.downloadmanager.notifications;

import int_.esa.eo.ngeo.downloadmanager.configuration.DownloadManagerProperties;
import int_.esa.eo.ngeo.downloadmanager.exception.NotificationException;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;

public class ProductionTerminationEmailFormatter {
    private final DownloadManagerProperties downloadManagerProperties;
    
    public ProductionTerminationEmailFormatter(DownloadManagerProperties downloadManagerProperties) {
        this.downloadManagerProperties = downloadManagerProperties;
    }
    
    public NotificationLevel getNotificationLevelForProduct(Product product) throws NotificationException {
        switch(product.getProductProgress().getStatus()) {
        case CANCELLED:
        case COMPLETED:
            return NotificationLevel.INFO;
        case IN_ERROR:
            return NotificationLevel.ERROR;
        default:
            throw new NotificationException(String.format("Product termination email cannot be created for a product with status %s i.e. non-terminal", product.getProductProgress().getStatus()));
        }
    }
    
    public String getEmailTitleForProduct(Product product) throws NotificationException {
        StringBuilder emailTitle = new StringBuilder();
        
        emailTitle.append("[");
        emailTitle.append(downloadManagerProperties.getDownloadManagerTitle());
        emailTitle.append(" ");
        emailTitle.append(downloadManagerProperties.getDownloadManagerVersion());
        emailTitle.append("] ");
        emailTitle.append(getNotificationLevelForProduct(product));
        emailTitle.append(" - Product termination");
        
        return emailTitle.toString();
    }
    
    public String getEmailMessageForProduct(Product product) {
        StringBuilder emailMessage = new StringBuilder();

        EDownloadStatus downloadStatus = product.getProductProgress().getStatus();

        emailMessage.append("The following product has been terminated:\n\n");
        emailMessage.append("Product URL: ");
        emailMessage.append(product.getProductAccessUrl());
        emailMessage.append("\n");
        emailMessage.append("Product Status: ");
        emailMessage.append(downloadStatus);
        emailMessage.append("\n");
        emailMessage.append("\n");
        
        switch (downloadStatus) {
        case COMPLETED:
            emailMessage.append("Total file size: ");
            emailMessage.append(product.getProductProgress().getDownloadedSize());
            emailMessage.append("\n");

            emailMessage.append("Number of files: ");
            emailMessage.append(product.getNumberOfFiles());
            emailMessage.append("\n");

            emailMessage.append("Product location on file system: ");
            emailMessage.append(product.getCompletedDownloadPath());
            emailMessage.append("\n");
            break;
        case IN_ERROR:
            emailMessage.append("Reason for error: ");
            emailMessage.append(product.getProductProgress().getMessage());
            emailMessage.append("\n");
            break;
        default:
            break;
        }
        
        emailMessage.append("\n");
        emailMessage.append("This is an automated message, please do not reply.");

        return emailMessage.toString();
    }
}
