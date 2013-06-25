package int_.esa.eo.ngeo.downloadmanager.plugin;

public interface IProductDownloadListener {
	void progress(Integer progressPercentage, Long downloadedSize,
			EDownloadStatus status, String message);
}
