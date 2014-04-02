package int_.esa.eo.ngeo.dmtu.controller;

import int_.esa.eo.ngeo.downloadmanager.builder.CommandResponseBuilder;
import int_.esa.eo.ngeo.downloadmanager.builder.DataAccessRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.controller.DARMonitorController;
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
import int_.esa.eo.ngeo.downloadmanager.transform.XMLWithSchemaTransformer;
import int_.esa.eo.ngeo.downloadmanager.webserver.builder.NgeoWebServerResponseParser;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringResp;

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
    private DARMonitorController darMonitorController;

    @Autowired
    XMLWithSchemaTransformer xmlWithSchemaTransformer;
    
    @Autowired
    private StaticDarService staticDarService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DARController.class);

    @RequestMapping(value="/download", method = RequestMethod.POST, params = {"productDownloadUrl"})
    @ResponseBody
    public CommandResponseWithDarDetails addManualProductDownload(@RequestParam String productDownloadUrl, @RequestParam(value = "priority", defaultValue = "NORMAL") ProductPriority priority) {
        CommandResponseBuilder commandResponseBuilder = new CommandResponseBuilder();
        try {
            Pair<String, String> darAndProductUuidPair = dataAccessRequestManager.addManualProductDownload(productDownloadUrl, priority);
            return commandResponseBuilder.buildCommandResponseWithDarAndProductUuid(darAndProductUuidPair.getLeft(), darAndProductUuidPair.getRight(), "Unable to add manual product download");
        } catch (ProductAlreadyExistsInDarException e) {
            LOGGER.error(String.format("Product already exists in the DAR: %s", productDownloadUrl), e);
            return commandResponseBuilder.buildCommandResponseWithDarAndProductUuid(null, null, e.getLocalizedMessage(), e.getClass().getName());
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

            NgeoWebServerResponseParser ngeoWebServerResponseParser = new NgeoWebServerResponseParser(xmlWithSchemaTransformer);
            DataAccessMonitoringResp dataAccessMonitoringResponse = ngeoWebServerResponseParser.parseDataAccessMonitoringResponse(dataAccessRequestUrl, response);
            Date responseDate = new ResponseHeaderParser().searchForResponseDate(response.getHeaders());

            DataAccessRequest newDar = new DataAccessRequestBuilder().buildDAR(darUrl, null, false);
            darMonitorController.addDataAccessRequest(newDar);

            darMonitorController.updateDataAccessRequest(newDar, dataAccessMonitoringResponse, responseDate, priority);
            return commandResponseBuilder.buildCommandResponseWithDarUuid(newDar.getUuid(), "Unable to add manual dar.");
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
            NgeoWebServerResponseParser ngeoWebServerResponseParser = new NgeoWebServerResponseParser(xmlWithSchemaTransformer);
            DataAccessMonitoringResp dataAccessMonitoringResponse = ngeoWebServerResponseParser.handleDarResponse(dar, DataAccessMonitoringResp.class);
            Date responseDate = new Date();
    
            DataAccessRequest newDar = new DataAccessRequestBuilder().buildDAR(null, darName, false);
            darMonitorController.addDataAccessRequest(newDar);
            
            darMonitorController.updateDataAccessRequest(newDar, dataAccessMonitoringResponse, responseDate, priority);
            return commandResponseBuilder.buildCommandResponseWithDarUuid(newDar.getUuid(), "Unable to add manual dar.");
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
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.setDataAccessRequests(dataAccessRequestManager.getVisibleDARList(true));
        return statusResponse;
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
