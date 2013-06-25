package int_.esa.eo.ngeo.downloadmanager.plugin;

interface DownloadProgressListener {
	void notifySomeBytesTransferred(long numberOfBytes);
}