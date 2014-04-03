package int_.esa.eo.ngeo.dmtu.controller;

import javax.servlet.http.HttpServletResponse;

import int_.esa.eo.ngeo.downloadmanager.builder.CommandResponseBuilder;
import int_.esa.eo.ngeo.downloadmanager.download.DownloadMonitor;
import int_.esa.eo.ngeo.downloadmanager.exception.DownloadOperationException;
import int_.esa.eo.ngeo.downloadmanager.exception.ProductNotFoundException;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ProductController {
    @Autowired
    private DownloadMonitor downloadMonitor;

    private CommandResponseBuilder commandResponseBuilder;

    public ProductController() {
        commandResponseBuilder = new CommandResponseBuilder();
    }

    @RequestMapping(value="/products/{productUuid}", method = RequestMethod.GET, params="action=pause")
    @ResponseBody
    public CommandResponse pauseProductDownload(HttpServletResponse response, @PathVariable String productUuid) {
        setNoCacheHeader(response);
        try {
            return commandResponseBuilder.buildCommandResponse(downloadMonitor.pauseProductDownload(productUuid), "Unable to pause product");
        } catch (DownloadOperationException | ProductNotFoundException e) {
            return commandResponseBuilder.buildCommandResponse(false, e.getLocalizedMessage(), e.getClass().getName());
        }
    }

    @RequestMapping(value="/products/{productUuid}", method = RequestMethod.GET, params="action=resume")
    @ResponseBody
    public CommandResponse resumeProductDownload(HttpServletResponse response, @PathVariable String productUuid) {
        setNoCacheHeader(response);
        try {
            return commandResponseBuilder.buildCommandResponse(downloadMonitor.resumeProductDownload(productUuid), "Unable to resume product");
        } catch (DownloadOperationException | ProductNotFoundException e) {
            return commandResponseBuilder.buildCommandResponse(false, e.getLocalizedMessage(), e.getClass().getName());
        }
    }

    @RequestMapping(value="/products/{productUuid}", method = RequestMethod.GET, params="action=cancel")
    @ResponseBody
    public CommandResponse cancelProductDownload(HttpServletResponse response, @PathVariable String productUuid) {
        setNoCacheHeader(response);
        try {
            return commandResponseBuilder.buildCommandResponse(downloadMonitor.cancelProductDownload(productUuid), "Unable to cancel product");
        } catch (DownloadOperationException | ProductNotFoundException e) {
            return commandResponseBuilder.buildCommandResponse(false, e.getLocalizedMessage(), e.getClass().getName());
        }
    }

    @RequestMapping(value="/products/{productUuid}", method = RequestMethod.GET, params="action=changePriority")
    @ResponseBody
    public CommandResponse changeProductPriority(HttpServletResponse response, @PathVariable String productUuid, @RequestParam ProductPriority newPriority) {
        setNoCacheHeader(response);
        try {
            return commandResponseBuilder.buildCommandResponse(downloadMonitor.changeProductPriority(productUuid, newPriority), "Unable to change priority of product.");
        } catch (ProductNotFoundException e) {
            return commandResponseBuilder.buildCommandResponse(false, e.getLocalizedMessage(), e.getClass().getName());
        }
    }
    
    private void setNoCacheHeader(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
    }
}
