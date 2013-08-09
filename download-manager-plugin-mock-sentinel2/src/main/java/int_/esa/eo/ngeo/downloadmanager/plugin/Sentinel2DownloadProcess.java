package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Sentinel2DownloadProcess implements IDownloadProcess {
	private static final String NO_IMPLEMENTATION_EXISTS_IN_THIS_MOCK = "Mock Sentinel 2 plugin initialised, no implementation exists in this mock.";

	private EDownloadStatus downloadStatus;
	
	private long totalFileDownloadedSize;
	
	private int percentageComplete;

	private List<IProductDownloadListener> productDownloadListeners;
	
	private String message;
	
	public Sentinel2DownloadProcess(URI productURI, File downloadDir, IProductDownloadListener productDownloadListener) {
		this.productDownloadListeners = new ArrayList<IProductDownloadListener>();
		this.productDownloadListeners.add(productDownloadListener);
		this.downloadStatus = EDownloadStatus.NOT_STARTED;
	}
	
	public EDownloadStatus startDownload() throws DMPluginException {
		setStatus(EDownloadStatus.IN_ERROR, NO_IMPLEMENTATION_EXISTS_IN_THIS_MOCK);

		return getStatus();
	}
	
	public EDownloadStatus pauseDownload() throws DMPluginException {
		setStatus(EDownloadStatus.IN_ERROR, NO_IMPLEMENTATION_EXISTS_IN_THIS_MOCK);

		return getStatus();
	}

	public EDownloadStatus resumeDownload() throws DMPluginException {
		setStatus(EDownloadStatus.IN_ERROR, NO_IMPLEMENTATION_EXISTS_IN_THIS_MOCK);

		return getStatus();
	}

	public EDownloadStatus cancelDownload() throws DMPluginException {
		setStatus(EDownloadStatus.IN_ERROR, NO_IMPLEMENTATION_EXISTS_IN_THIS_MOCK);
		
		return getStatus();
	}

	public EDownloadStatus getStatus() {
		return getDownloadStatus();
	}

	public File[] getDownloadedFiles() {
		return null;
	}

	/* This method is the last one called by the Download Manager on a IDownloadProcess instance.
	 * 		It is called by the Download Manager either:
	 * -	after the status COMPLETED, CANCELLED or IN_ERROR has been notified by the plugin to the Download Manager and the reference of downloaded files has been retrieved by the later
	 * -	when the Download Manager ends. In this second case, the plugin is expected to :
	 * 		-	if RUNNING 		: stop the download
	 * 		-	if RUNNING or PAUSED	: store onto disk the current download state (if possible) in order to restart it
	 */
	public void disconnect() throws DMPluginException {

	}
	
	public void setStatus(EDownloadStatus downloadStatus) {
		setStatus(downloadStatus, null);
	}

	public void setStatus(EDownloadStatus downloadStatus, String message) {
		this.setDownloadStatus(downloadStatus);
		this.message = message;
		notifyListeners();
	}
	
	private void notifyListeners() {
		for (IProductDownloadListener productDownloadListener : productDownloadListeners) {
			productDownloadListener.progress(getPercentageComplete(), getTotalFileDownloadedSize(), getDownloadStatus(), message);
		}
	}
	
	public synchronized long getTotalFileDownloadedSize() {
		return totalFileDownloadedSize;
	}

	public synchronized void setTotalFileDownloadedSize(long totalFileDownloadedSize) {
		this.totalFileDownloadedSize = totalFileDownloadedSize;
	}
	
	public synchronized int getPercentageComplete() {
		return percentageComplete;
	}

	public synchronized void setPercentageComplete(int percentageComplete) {
		this.percentageComplete = percentageComplete;
	}
	
	public synchronized EDownloadStatus getDownloadStatus() {
		return downloadStatus;
	}

	public synchronized void setDownloadStatus(EDownloadStatus downloadStatus) {
		this.downloadStatus = downloadStatus;
	}
}
