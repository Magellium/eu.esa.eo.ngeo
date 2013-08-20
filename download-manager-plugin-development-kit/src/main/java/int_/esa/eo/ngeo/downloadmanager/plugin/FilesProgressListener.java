package int_.esa.eo.ngeo.downloadmanager.plugin;

import java.nio.file.Path;

public interface FilesProgressListener {
	void notifySomeBytesTransferred(String fileDownloadMetadataUuid, long numberOfBytes);
}