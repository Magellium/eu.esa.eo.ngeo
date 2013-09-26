package int_.esa.eo.ngeo.dmtu.controller;

import int_.esa.eo.ngeo.dmtu.builder.CommandResponse;
import int_.esa.eo.ngeo.dmtu.builder.CommandResponseBuilder;
import int_.esa.eo.ngeo.dmtu.exception.DownloadOperationException;
import int_.esa.eo.ngeo.dmtu.manager.SettingsManager;
import int_.esa.eo.ngeo.dmtu.model.WebServerMonitoringStatus;
import int_.esa.eo.ngeo.dmtu.monitor.DownloadMonitor;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.UserOrder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MonitoringController {
	@Autowired
	private SettingsManager settingsManager;
	
	@Autowired
	private DownloadMonitor downloadMonitor;

	private WebServerMonitoringStatus webServerMonitoringStatus; 

	private MonitoringController() {
		webServerMonitoringStatus = new WebServerMonitoringStatus();
	}
	
	public String getSetting(String key) {
		return settingsManager.getSetting(key);
	}

	public void setSetting(String key, String value) {
		settingsManager.setSetting(key, value);
	}

	public boolean sendUserOrder(UserOrder userOrder) throws DownloadOperationException {
		return sendUserOrder(userOrder, false);
	}
	private boolean sendUserOrder(UserOrder userOrder, boolean includeManualDownloads) throws DownloadOperationException {
		webServerMonitoringStatus.setDarMonitoringRunning(false);
		
		List<EDownloadStatus> statusesToCancel = new ArrayList<>();
		statusesToCancel.add(EDownloadStatus.NOT_STARTED);
		statusesToCancel.add(EDownloadStatus.IDLE);
		statusesToCancel.add(EDownloadStatus.PAUSED);

		if(userOrder == UserOrder.STOP_IMMEDIATELY) {
			statusesToCancel.add(EDownloadStatus.RUNNING);
		}
		return downloadMonitor.cancelDownloadsWithStatuses(statusesToCancel, includeManualDownloads);
	}
	
	@RequestMapping(value="/monitoring/stop", method = RequestMethod.GET)
	@ResponseBody
	public CommandResponse stopMonitoringForDARs(@RequestParam String type) {
		UserOrder userOrder;
		CommandResponseBuilder commandResponseBuilder = new CommandResponseBuilder();
		boolean includeManualDownloads = false;
		try {
			switch (type) {
			case "monitoring":
				userOrder = UserOrder.STOP;
				break;
			case "monitoringNow":
				userOrder = UserOrder.STOP_IMMEDIATELY;
				break;
			case "all":
				userOrder = UserOrder.STOP_IMMEDIATELY;
				includeManualDownloads = true;
				break;
			default:
				throw new DownloadOperationException("No stop type provided.");
			}

			return commandResponseBuilder.buildCommandResponse(sendUserOrder(userOrder, includeManualDownloads), String.format("Unable to execute stop command %s.", type));
		} catch (DownloadOperationException e) {
			return commandResponseBuilder.buildCommandResponse(false, e.getLocalizedMessage());
		}
	}

	public boolean isDarMonitoringRunning() {
		return webServerMonitoringStatus.isDarMonitoringRunning();
	}
}
