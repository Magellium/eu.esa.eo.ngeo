package int_.esa.eo.ngeo.downloadmanager.observer;

import int_.esa.eo.ngeo.downloadmanager.monitor.WebServerMonitoringDetails;

public interface WebServerMonitoringObserver {
    void scheduleWebServerMonitoring(WebServerMonitoringDetails webServerMonitoringDetails);
}
