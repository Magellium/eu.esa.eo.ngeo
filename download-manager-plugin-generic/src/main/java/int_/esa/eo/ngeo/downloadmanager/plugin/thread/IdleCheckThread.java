package int_.esa.eo.ngeo.downloadmanager.plugin.thread;

import int_.esa.eo.ngeo.downloadmanager.plugin.HTTPDownloadProcess;

public class IdleCheckThread implements Runnable {
	HTTPDownloadProcess downloadProcess;

	public IdleCheckThread(HTTPDownloadProcess downloadProcess) {
		this.downloadProcess = downloadProcess;
	}

	public void run() {
		downloadProcess.retrieveDownloadDetails();
	}
}