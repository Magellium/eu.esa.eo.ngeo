package int_.esa.eo.ngeo.downloadmanager.plugin;

public interface FilesProgressListener {
	void notifySomeBytesTransferred(String fileDownloadMetadataUuid, long numberOfBytes);
}