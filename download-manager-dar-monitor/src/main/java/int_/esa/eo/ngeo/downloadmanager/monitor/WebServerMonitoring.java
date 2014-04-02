package int_.esa.eo.ngeo.downloadmanager.monitor;


import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;

public class WebServerMonitoring {

    private HashMap<String, WebServerMonitoringDetails> webServerMonitoringMap;
    private HashMap<String, ScheduledFuture<WebServerMonitoringTask>> webServerMonitoringTaskMap;
    private final int initialRefreshPeriod;

    public WebServerMonitoring(int initialRefreshPeriod) {
        this.initialRefreshPeriod = initialRefreshPeriod;
        this.webServerMonitoringMap = new HashMap<>();
        this.webServerMonitoringTaskMap = new HashMap<>();
    }

    public int getInitialRefreshPeriod() {
        return initialRefreshPeriod;
    }

    public HashMap<String, WebServerMonitoringDetails> getWebServerMonitoringMap() {
        return webServerMonitoringMap;
    }

    public void setWebServerMonitoringMap(HashMap<String, WebServerMonitoringDetails> webServerMonitoringMap) {
        this.webServerMonitoringMap = webServerMonitoringMap;
    }

    public HashMap<String, ScheduledFuture<WebServerMonitoringTask>> getWebServerMonitoringTaskMap() {
        return webServerMonitoringTaskMap;
    }

    public void setWebServerMonitoringTaskMap(
            HashMap<String, ScheduledFuture<WebServerMonitoringTask>> webServerMonitoringTaskMap) {
        this.webServerMonitoringTaskMap = webServerMonitoringTaskMap;
    }
}
