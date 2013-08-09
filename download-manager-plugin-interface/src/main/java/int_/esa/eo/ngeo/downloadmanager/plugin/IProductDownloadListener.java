package int_.esa.eo.ngeo.downloadmanager.plugin;

public interface IProductDownloadListener {
	void productDetails(String productName, Integer numberOfFiles, Long overallSize);
	void progress(Integer progressPercentage, Long downloadedSize,
			EDownloadStatus status, String message);
}
