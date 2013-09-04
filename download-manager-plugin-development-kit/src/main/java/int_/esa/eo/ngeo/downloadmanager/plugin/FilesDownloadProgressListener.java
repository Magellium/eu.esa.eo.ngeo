package int_.esa.eo.ngeo.downloadmanager.plugin;

import java.nio.file.Path;

public interface FilesDownloadProgressListener {
	void notifySomeBytesTransferred(String fileDownloadMetadataUuid, long numberOfBytes);
	void notifyOfFileStatusChange(EDownloadStatus fileDownloadStatus, String message);
	void notifyOfCompletedPath(String fileDownloadMetadataUuid, Path completedPath);
}