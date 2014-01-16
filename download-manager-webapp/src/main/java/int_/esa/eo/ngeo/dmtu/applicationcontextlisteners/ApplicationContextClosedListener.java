package int_.esa.eo.ngeo.dmtu.applicationcontextlisteners;

import int_.esa.eo.ngeo.dmtu.monitor.dar.DARMonitor;
import int_.esa.eo.ngeo.downloadmanager.dar.DataAccessRequestManager;
import int_.esa.eo.ngeo.downloadmanager.download.DownloadMonitor;
import int_.esa.eo.ngeo.downloadmanager.plugin.PluginManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextClosedListener implements ApplicationListener<ContextClosedEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContextClosedListener.class);

    @Autowired
    private DataAccessRequestManager dataAccessRequestManager;

    @Autowired
    private DARMonitor darMonitor;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private DownloadMonitor downloadMonitor;

    @Override
    public void onApplicationEvent(ContextClosedEvent arg0) {
        LOGGER.info("Shutting down Download Manager.");
        darMonitor.shutdown();

        downloadMonitor.shutdown();

        pluginManager.cleanUp();
    }
}