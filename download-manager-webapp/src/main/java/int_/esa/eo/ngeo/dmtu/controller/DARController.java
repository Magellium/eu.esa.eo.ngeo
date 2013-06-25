package int_.esa.eo.ngeo.dmtu.controller;

import int_.esa.eo.ngeo.dmtu.exception.DataAccessRequestAlreadyExistsException;
import int_.esa.eo.ngeo.dmtu.exception.DownloadOperationException;
import int_.esa.eo.ngeo.dmtu.exception.ProductAlreadyExistsInDarException;
import int_.esa.eo.ngeo.dmtu.manager.DataAccessRequestManager;
import int_.esa.eo.ngeo.dmtu.manager.SettingsManager;
import int_.esa.eo.ngeo.dmtu.model.DataAccessRequest;
import int_.esa.eo.ngeo.dmtu.model.Product;
import int_.esa.eo.ngeo.dmtu.monitor.DownloadMonitor;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessList;
import int_.esa.eo.ngeo.iicd_d_ws._1.UserOrder;

import java.net.URL;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DARController {
	@Autowired
	private DataAccessRequestManager dataAccessRequestManager;

	@Autowired
	private DownloadMonitor downloadMonitor;

	@Autowired
	private SettingsManager settingsManager;

	public String getSetting(String key) {
		return settingsManager.getSetting(key);
	}

	public void setSetting(String key, String value) {
		settingsManager.setSetting(key, value);
	}

	public boolean addDataAccessRequest(URL darMonitoringUrl) throws DataAccessRequestAlreadyExistsException {
		return dataAccessRequestManager.addDataAccessRequest(darMonitoringUrl);
	}
	
	public void sendUserOrder(UserOrder userOrder) {
		//TODO: implement STOP and STOP_IMMEADIATELY commands
	}
	
	public void updateDAR(URL darMonitoringUrl, MonitoringStatus monitoringStatus, Date responseDate, ProductAccessList productAccessList) {
		dataAccessRequestManager.updateDataAccessRequest(darMonitoringUrl, monitoringStatus, responseDate, productAccessList);
	}

	/**
	 * @return The DAR to which this product has been added
	 */
	@RequestMapping(value="/manualProductDownload", method = RequestMethod.POST)
	@ResponseBody
	public CommandResponse addManualProductDownload(@RequestParam("productDownloadUrl") String productDownloadUrl) {
		CommandResponse response;
		try {
			response = createCommandResponse(dataAccessRequestManager.addManualProductDownload(productDownloadUrl), "Unable to add manual product download");
		} catch (ProductAlreadyExistsInDarException e) {
			response = createCommandResponse(false, e.getLocalizedMessage());
		}
		return response;
	}
	
	//TODO: implement clearing of DARs / products
	@RequestMapping(value="/clearActivityHistory", method = RequestMethod.GET)
	@ResponseBody
	public CommandResponse clearActivityHistory() {
		CommandResponse response;

		response = createCommandResponse(dataAccessRequestManager.clearActivityHistory(), "Unable to clear activity history.");

		return response;
	}

	@RequestMapping(value = "/dataAccessRequests", method = RequestMethod.GET)
	@ResponseBody
	public List<DataAccessRequest> getDataAccessRequests() {
		return dataAccessRequestManager.getVisibleDARList(true);
	}

	public List<DataAccessRequest> getDataAccessRequests(boolean includeManualDar) {
		return dataAccessRequestManager.getVisibleDARList(includeManualDar);
	}

	public DataAccessRequest getDataAccessRequestByMonitoringUrl(URL monitoringUrl) {
		return dataAccessRequestManager.getDataAccessRequestByMonitoringUrl(monitoringUrl);
	}

	@RequestMapping(value = "/dataAccessRequests/{darUuid}", method = RequestMethod.GET)
	@ResponseBody
	public List<Product> getProducts(@PathVariable String darUuid) {
		return dataAccessRequestManager.getProductList(darUuid);
	}
	
	@RequestMapping(value="/products/{productUuid}", method = RequestMethod.GET, params="action=pause")
	@ResponseBody
	public CommandResponse pauseProductDownload(@PathVariable String productUuid) {
		CommandResponse response;
		try {
			response = createCommandResponse(downloadMonitor.pauseProductDownload(productUuid), "Unable to pause product");
		} catch (DownloadOperationException e) {
			response = createCommandResponse(false, e.getLocalizedMessage());
		}
		return response;
	}

	@RequestMapping(value="/products/{productUuid}", method = RequestMethod.GET, params="action=resume")
	@ResponseBody
	public CommandResponse resumeProductDownload(@PathVariable String productUuid) {
		CommandResponse response;
		try {
			response = createCommandResponse(downloadMonitor.resumeProductDownload(productUuid), "Unable to pause product");
		} catch (DownloadOperationException e) {
			response = createCommandResponse(false, e.getLocalizedMessage());
		}
		return response;
	}

	@RequestMapping(value="/products/{productUuid}", method = RequestMethod.GET, params="action=cancel")
	@ResponseBody
	public CommandResponse cancelProductDownload(@PathVariable String productUuid) {
		CommandResponse response;
		try {
			response = createCommandResponse(downloadMonitor.cancelProductDownload(productUuid), "Unable to pause product");
		} catch (DownloadOperationException e) {
			response = createCommandResponse(false, e.getLocalizedMessage());
		}
		return response;
	}
	
	private CommandResponse createCommandResponse(boolean success, String messageIfFailed) {
		CommandResponse response;
		if(success) {
			response = new CommandResponse(success);
		}else{
			response = new CommandResponse(success, messageIfFailed);
		}
		return response;
	}
}
