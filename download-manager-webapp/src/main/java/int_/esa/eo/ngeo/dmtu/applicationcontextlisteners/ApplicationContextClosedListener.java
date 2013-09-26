package int_.esa.eo.ngeo.dmtu.applicationcontextlisteners;

import int_.esa.eo.ngeo.dmtu.manager.DataAccessRequestManager;
import int_.esa.eo.ngeo.dmtu.monitor.DownloadMonitor;
import int_.esa.eo.ngeo.dmtu.monitor.dar.DARMonitor;
import int_.esa.eo.ngeo.downloadmanager.plugin.PluginManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextClosedListener implements ApplicationListener<ContextClosedEvent> {

	@Autowired
	private DataAccessRequestManager dataAccessRequestManager;
	
	@Autowired
	private DARMonitor darMonitor;
	
	@Autowired
	private PluginManager pluginManager;

	private DownloadMonitor downloadMonitor;
	
	@Override
	public void onApplicationEvent(ContextClosedEvent arg0) {
		downloadMonitor.shutdown();
		
		pluginManager.cleanUp();
	}
}