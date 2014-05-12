package int_.esa.eo.ngeo.downloadmanager.plugin.http;

import int_.esa.eo.ngeo.downloadmanager.plugin.DownloadProcess;


public class IdleCheckThread implements Runnable {
    DownloadProcess downloadProcess;

    public IdleCheckThread(DownloadProcess downloadProcess) {
        this.downloadProcess = downloadProcess;
    }

    public void run() {
        downloadProcess.retrieveDownloadDetails();
    }
}