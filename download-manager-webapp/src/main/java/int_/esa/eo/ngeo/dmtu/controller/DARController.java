package int_.esa.eo.ngeo.dmtu.controller;

import int_.esa.eo.ngeo.dmtu.builder.CommandResponse;
import int_.esa.eo.ngeo.dmtu.builder.CommandResponseBuilder;
import int_.esa.eo.ngeo.dmtu.exception.DataAccessRequestAlreadyExistsException;
import int_.esa.eo.ngeo.dmtu.exception.ProductAlreadyExistsInDarException;
import int_.esa.eo.ngeo.dmtu.manager.DataAccessRequestManager;
import int_.esa.eo.ngeo.dmtu.model.DataAccessRequest;
import int_.esa.eo.ngeo.dmtu.model.Product;
import int_.esa.eo.ngeo.dmtu.model.WebServerMonitoringStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessList;

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

	public boolean addDataAccessRequest(URL darMonitoringUrl) throws DataAccessRequestAlreadyExistsException {
		return dataAccessRequestManager.addDataAccessRequest(darMonitoringUrl);
	}
	
	public void updateDAR(URL darMonitoringUrl, MonitoringStatus monitoringStatus, Date responseDate, ProductAccessList productAccessList) {
		dataAccessRequestManager.updateDataAccessRequest(darMonitoringUrl, monitoringStatus, responseDate, productAccessList);
	}

	@RequestMapping(value="/manualProductDownload", method = RequestMethod.POST)
	@ResponseBody
	public CommandResponse addManualProductDownload(@RequestParam("productDownloadUrl") String productDownloadUrl) {
		CommandResponseBuilder commandResponseBuilder = new CommandResponseBuilder();
		try {
			return commandResponseBuilder.buildCommandResponse(dataAccessRequestManager.addManualProductDownload(productDownloadUrl), "Unable to add manual product download");
		} catch (ProductAlreadyExistsInDarException e) {
			return commandResponseBuilder.buildCommandResponse(false, e.getLocalizedMessage());
		}
	}
	
	@RequestMapping(value="/clearActivityHistory", method = RequestMethod.GET)
	@ResponseBody
	public CommandResponse clearActivityHistory() {
		CommandResponseBuilder commandResponseBuilder = new CommandResponseBuilder();
		return commandResponseBuilder.buildCommandResponse(dataAccessRequestManager.clearActivityHistory(), "Unable to clear activity history.");
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
}
