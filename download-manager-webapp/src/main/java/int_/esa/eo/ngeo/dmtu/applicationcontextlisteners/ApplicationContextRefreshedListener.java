package int_.esa.eo.ngeo.dmtu.applicationcontextlisteners;

import int_.esa.eo.ngeo.dmtu.manager.DataAccessRequestManager;
import int_.esa.eo.ngeo.dmtu.monitor.dar.DARMonitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private DataAccessRequestManager dataAccessRequestManager;
	
	@Autowired
	private DARMonitor darMonitor;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		dataAccessRequestManager.loadDARs(); // Loads DARs from the DB
		
		darMonitor.monitorForProductsFromLoadedDARs();
		// We must not start polling for new DARs until we have finished loading DARs from the DB
		darMonitor.monitorForDARs();
	}

}