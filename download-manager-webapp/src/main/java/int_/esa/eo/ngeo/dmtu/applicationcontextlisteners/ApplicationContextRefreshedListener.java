package int_.esa.eo.ngeo.dmtu.applicationcontextlisteners;

import int_.esa.eo.ngeo.dmtu.monitor.dar.DARMonitor;
import int_.esa.eo.ngeo.downloadmanager.configuration.DownloadManagerProperties;
import int_.esa.eo.ngeo.downloadmanager.dar.DataAccessRequestManager;
import int_.esa.eo.ngeo.downloadmanager.download.DownloadMonitor;
import int_.esa.eo.ngeo.downloadmanager.http.ConnectionPropertiesSynchronizedUmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.plugin.PluginManager;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContextRefreshedListener.class);

    @Autowired
    private DataAccessRequestManager dataAccessRequestManager;

    @Autowired
    private DARMonitor darMonitor;

    @Autowired
    DownloadMonitor downloadMonitor;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private DownloadManagerProperties downloadManagerProperties;

    @Autowired
    private ConnectionPropertiesSynchronizedUmSsoHttpClient connectionPropertiesSynchronizedUmSsoHttpClient;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent arg0) {
        LOGGER.info("Starting Download Manager.");
        settingsManager.init();

        downloadManagerProperties.loadDownloadManagerProperties();

        connectionPropertiesSynchronizedUmSsoHttpClient.registerWithSettingsManager();
        connectionPropertiesSynchronizedUmSsoHttpClient.initUmSsoConnectionSettingsFromSettingsManager();

        pluginManager.detectPlugins();

        downloadMonitor.initDowloadPool();

        // Loads DARs from the DB
        dataAccessRequestManager.loadDARs();

        darMonitor.monitorForProductsFromLoadedDARs();
        // We must not start polling for new DARs until we have finished loading DARs from the DB
        darMonitor.monitorForDARs();
    }

}