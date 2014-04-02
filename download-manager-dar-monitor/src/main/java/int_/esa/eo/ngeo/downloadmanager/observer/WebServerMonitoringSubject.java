package int_.esa.eo.ngeo.downloadmanager.observer;

import int_.esa.eo.ngeo.downloadmanager.monitor.WebServerMonitoringDetails;

public interface WebServerMonitoringSubject {
    void registerObserver(WebServerMonitoringObserver o);
    void notifyObservers(WebServerMonitoringDetails webServerMonitoringDetails);
}
