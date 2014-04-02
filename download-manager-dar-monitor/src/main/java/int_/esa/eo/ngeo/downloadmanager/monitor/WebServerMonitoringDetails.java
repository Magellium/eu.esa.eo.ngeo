package int_.esa.eo.ngeo.downloadmanager.monitor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WebServerMonitoringDetails {
    private final URL darMonitoringUrl;
    private List<URL> productMonitoringUrls;
    private int refreshPeriod;
    
    public WebServerMonitoringDetails(URL darMonitoringUrl, int initialRefreshPeriod) {
        this.darMonitoringUrl = darMonitoringUrl;
        this.setRefreshPeriod(initialRefreshPeriod);
        this.productMonitoringUrls = new ArrayList<>();
    }

    public URL getDarMonitoringUrl() {
        return darMonitoringUrl;
    }

    public List<URL> getProductMonitoringUrls() {
        return productMonitoringUrls;
    }

    public void setProductMonitoringUrls(List<URL> productMonitoringUrls) {
        this.productMonitoringUrls = productMonitoringUrls;
    }

    public int getRefreshPeriod() {
        return refreshPeriod;
    }

    public void setRefreshPeriod(int refreshPeriod) {
        this.refreshPeriod = refreshPeriod;
    }
}
