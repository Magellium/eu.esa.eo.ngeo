package int_.esa.eo.ngeo.dmtu.controller;

import int_.esa.eo.ngeo.dmtu.webserver.builder.NgeoWebServerResponseParser;
import int_.esa.eo.ngeo.downloadmanager.builder.CommandResponseBuilder;
import int_.esa.eo.ngeo.downloadmanager.dar.DataAccessRequestManager;
import int_.esa.eo.ngeo.downloadmanager.exception.DataAccessRequestAlreadyExistsException;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.ProductAlreadyExistsInDarException;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.http.ResponseHeaderParser;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpRequestAndResponse;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithDarDetails;
import int_.esa.eo.ngeo.downloadmanager.rest.StatusResponse;
import int_.esa.eo.ngeo.downloadmanager.service.StaticDarService;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessList;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Pair;
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

    public String addDataAccessRequestWithDarUrl(String darUrl, boolean monitored) throws DataAccessRequestAlreadyExistsException {
        return dataAccessRequestManager.addDataAccessRequest(darUrl, null, monitored);
    }

    public String addDataAccessRequestWithName(String darName) throws DataAccessRequestAlreadyExistsException {
        return dataAccessRequestManager.addDataAccessRequest(null, darName, false);
    }

    public void updateDARWithDarUrl(String darUrl, MonitoringStatus monitoringStatus, Date responseDate, ProductAccessList productAccessList, ProductPriority priority) {
        dataAccessRequestManager.updateDataAccessRequest(darUrl, null, monitoringStatus, responseDate, productAccessList, priority);
    }

    public void updateDARWithName(String darName, MonitoringStatus monitoringStatus, Date responseDate, ProductAccessList productAccessList, ProductPriority priority) {
        dataAccessRequestManager.updateDataAccessRequest(null, darName, monitoringStatus, responseDate, productAccessList, priority);
    }

    @RequestMapping(value="/download", method = RequestMethod.POST, params = {"productDownloadUrl"})
    @ResponseBody
    public CommandResponseWithDarDetails addManualProductDownload(@RequestParam String productDownloadUrl, @RequestParam(value = "priority", defaultValue = "NORMAL") ProductPriority priority) {
        CommandResponseBuilder commandResponseBuilder = new CommandResponseBuilder();
        try {
            Pair<String, String> darAndProductUuidPair = dataAccessRequestManager.addManualProductDownload(productDownloadUrl, priority);
            return commandResponseBuilder.buildCommandResponseWithDarAndProductUuid(darAndProductUuidPair.getLeft(), darAndProductUuidPair.getRight(), "Unable to add manual product download");
        } catch (ProductAlreadyExistsInDarException e) {
            LOGGER.error(String.format("Product already exists in the DAR: %s", productDownloadUrl), e);
            return commandResponseBuilder.buildCommandResponseWithDarAndProductUuid(null, e.getLocalizedMessage(), e.getClass().getName());
        }
    }

    @RequestMapping(value="/download", method = RequestMethod.POST, params = {"darUrl"})
    @ResponseBody
    public CommandResponseWithDarDetails addManualDarByUrl(@RequestParam String darUrl, @RequestParam(value = "priority", defaultValue = "NORMAL") ProductPriority priority) {
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
            String dataAccessRequestUuid = addDataAccessRequestWithDarUrl(darUrl, false);
            updateDARWithDarUrl(darUrl, monitoringStatus, responseDate, productAccessList, priority);
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

    @RequestMapping(value="/download", method = RequestMethod.POST, params = {"dar", "darName"})
    @ResponseBody
    public CommandResponseWithDarDetails addManualDar(@RequestParam String dar, String darName, @RequestParam(value = "priority", defaultValue = "NORMAL") ProductPriority priority) {
        CommandResponseBuilder commandResponseBuilder = new CommandResponseBuilder();
        try {
            DataAccessMonitoringResp dataAccessMonitoringResponse = ngeoWebServerResponseParser.handleDarResponse(dar, DataAccessMonitoringResp.class);
            Date responseDate = new Date();
    
            MonitoringStatus monitoringStatus = dataAccessMonitoringResponse.getMonitoringStatus();
            ProductAccessList productAccessList = dataAccessMonitoringResponse.getProductAccessList();
            String dataAccessRequestUuid = addDataAccessRequestWithName(darName);
            updateDARWithName(darName, monitoringStatus, responseDate, productAccessList, priority);
            return commandResponseBuilder.buildCommandResponseWithDarUuid(dataAccessRequestUuid, "Unable to add manual dar.");
        } catch (ParseException | ServiceException | DataAccessRequestAlreadyExistsException e) {
            LOGGER.error(String.format("%s whilst adding DAR %s: %s", e.getClass().getName(), darName, e.getLocalizedMessage()));
            LOGGER.debug("Manual DAR exception stack trace:", e);
            return commandResponseBuilder.buildCommandResponseWithDarUuid(null, String.format("Error whilst adding DAR: %s", e.getLocalizedMessage()), NonRecoverableException.class.getName());
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
    public StatusResponse getDataAccessRequestStatus(HttpServletResponse response) {
        setNoCacheHeader(response);
        return getDataAccessRequestStatus(true);
    }

    public StatusResponse getDataAccessRequestStatus(boolean includeManualProductDar) {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.setDataAccessRequests(dataAccessRequestManager.getVisibleDARList(includeManualProductDar));
        return statusResponse;
    }

    public DataAccessRequest getDataAccessRequestByMonitoringUrl(String monitoringUrl) {
        return dataAccessRequestManager.getDataAccessRequest(monitoringUrl, null);
    }

    @RequestMapping(value = "/dataAccessRequests/{darUuid}", method = RequestMethod.GET)
    @ResponseBody
    public List<Product> getProducts(@PathVariable String darUuid, HttpServletResponse response) {
        setNoCacheHeader(response);
        return dataAccessRequestManager.getProductList(darUuid);
    }
    
    private void setNoCacheHeader(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
    }
}
