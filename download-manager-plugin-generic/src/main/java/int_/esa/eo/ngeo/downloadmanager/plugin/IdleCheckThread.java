package int_.esa.eo.ngeo.downloadmanager.plugin;


public class IdleCheckThread implements Runnable {
    HTTPDownloadProcess downloadProcess;

    public IdleCheckThread(HTTPDownloadProcess downloadProcess) {
        this.downloadProcess = downloadProcess;
    }

    public void run() {
        downloadProcess.retrieveDownloadDetails();
    }
}