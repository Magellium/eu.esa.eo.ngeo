package int_.esa.eo.ngeo.dmtu.monitor;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;

public class ProductDownloadThread implements Runnable {
	public IDownloadProcess downloadProcess;

	public ProductDownloadThread(IDownloadProcess downloadProcess) {
		this.downloadProcess = downloadProcess;
	}
	
	@Override
	public void run() {
		try {
			downloadProcess.startDownload();
		} catch (DMPluginException e) {
			throw new NonRecoverableException("Unable to start download.", e);
		}
	}
}