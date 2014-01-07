package int_.esa.eo.ngeo.dmtu.controller;

import int_.esa.eo.ngeo.dmtu.exception.DataAccessRequestAlreadyExistsException;
import int_.esa.eo.ngeo.dmtu.exception.ProductAlreadyExistsInDarException;
import int_.esa.eo.ngeo.dmtu.manager.DataAccessRequestManager;
import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerResponseParser;
import int_.esa.eo.ngeo.downloadmanager.builder.CommandResponseBuilder;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.http.ResponseHeaderParser;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpRequestAndResponse;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithDarUuid;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithProductUuid;
import int_.esa.eo.ngeo.downloadmanager.rest.StatusResponse;
import int_.esa.eo.ngeo.downloadmanager.service.StaticDarService;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessList;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.http.impl.cookie.DateParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

@Controller
public class DARController {
	@Autowired
	private DataAccessRequestManager dataAccessRequestManager;

	@Autowired
	private NgeoWebServerResponseParser ngeoWebServerResponseParser;

	@Autowired
	private StaticDarService staticDarService;

	private static final Logger LOGGER = LoggerFactory.getLogger(DARController.class);

	public String addDataAccessRequest(URL darMonitoringUrl, boolean monitored) throws DataAccessRequestAlreadyExistsException {
		return dataAccessRequestManager.addDataAccessRequest(darMonitoringUrl, monitored);
	}
	
	public void updateDAR(URL darMonitoringUrl, MonitoringStatus monitoringStatus, Date responseDate, ProductAccessList productAccessList) {
		dataAccessRequestManager.updateDataAccessRequest(darMonitoringUrl, monitoringStatus, responseDate, productAccessList);
	}

	@RequestMapping(value="/download", method = RequestMethod.POST, params = "productDownloadUrl")
	@ResponseBody
	public CommandResponseWithProductUuid addManualProductDownload(@RequestParam String productDownloadUrl) {
		CommandResponseBuilder commandResponseBuilder = new CommandResponseBuilder();
		try {
			return commandResponseBuilder.buildCommandResponseWithProductUuid(dataAccessRequestManager.addManualProductDownload(productDownloadUrl), "Unable to add manual product download");
		} catch (ProductAlreadyExistsInDarException e) {
			LOGGER.error(String.format("Product already exists in the DAR: %s", productDownloadUrl), e);
			return commandResponseBuilder.buildCommandResponseWithProductUuid(null, e.getLocalizedMessage(), e.getClass().getName());
		}
	}

	@RequestMapping(value="/download", method = RequestMethod.POST, params = "darUrl")
	@ResponseBody
	public CommandResponseWithDarUuid addManualDar(@RequestParam String darUrl) {
		CommandResponseBuilder commandResponseBuilder = new CommandResponseBuilder();
		UmSsoHttpRequestAndResponse staticDarRequestAndResponse = null;
		try {
			URL dataAccessRequestUrl = new URL(darUrl);

			staticDarRequestAndResponse = staticDarService.getStaticDar(dataAccessRequestUrl);
			UmssoHttpResponse response = staticDarRequestAndResponse.getResponse();

			DataAccessMonitoringResp dataAccessMonitoringResponse = ngeoWebServerResponseParser.parseDataAccessMonitoringResponse(dataAccessRequestUrl, response);
			Date responseDate = new ResponseHeaderParser().searchForResponseDate(response.getHeaders());

			MonitoringStatus monitoringStatus = dataAccessMonitoringResponse.getMonitoringStatus();
			ProductAccessList productAccessList = dataAccessMonitoringResponse.getProductAccessList();
			
			String dataAccessRequestUuid = addDataAccessRequest(dataAccessRequestUrl, false);
			updateDAR(dataAccessRequestUrl, monitoringStatus, responseDate, productAccessList);
			return commandResponseBuilder.buildCommandResponseWithDarUuid(dataAccessRequestUuid, "Unable to add manual dar.");
		} catch (ParseException | ServiceException | DateParseException | IOException | DataAccessRequestAlreadyExistsException e) {
			LOGGER.error(String.format("%s whilst adding DAR %s: %s", e.getClass().getName(), darUrl, e.getLocalizedMessage()));
			LOGGER.debug("Manual DAR exception stack trace:", e);
		    return commandResponseBuilder.buildCommandResponseWithDarUuid(null, String.format("Error whilst adding DAR: %s", e.getLocalizedMessage()), NonRecoverableException.class.getName());
		} finally {
			if (staticDarRequestAndResponse != null) {
				staticDarRequestAndResponse.cleanupHttpResources();
			}
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
	public StatusResponse getDataAccessRequestStatus() {
		return getDataAccessRequestStatus(true);
	}

	public StatusResponse getDataAccessRequestStatus(boolean includeManualProductDar) {
		StatusResponse statusResponse = new StatusResponse();
		statusResponse.setDataAccessRequests(dataAccessRequestManager.getVisibleDARList(includeManualProductDar));
		return statusResponse;
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
