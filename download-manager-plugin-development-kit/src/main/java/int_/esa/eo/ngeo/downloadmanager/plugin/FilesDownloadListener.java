package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.plugin.model.FileDownloadMetadata;

import java.nio.file.Path;
import java.util.List;

public interface FilesDownloadListener {
    void notifyOfProductDetails(String productName, List<FileDownloadMetadata> fileDownloadMetadataList);
    void notifyOfBytesTransferred(String fileDownloadMetadataUuid, long numberOfBytes);
    void notifyOfFileStatusChange(EDownloadStatus fileDownloadStatus, String message);
    void notifyOfCompletedPath(String fileDownloadMetadataUuid, Path completedPath);
}