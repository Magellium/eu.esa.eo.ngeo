package int_.esa.eo.ngeo.dmtu.controller;

import int_.esa.eo.ngeo.downloadmanager.builder.CommandResponseBuilder;
import int_.esa.eo.ngeo.downloadmanager.controller.DARMonitorController;
import int_.esa.eo.ngeo.downloadmanager.exception.DownloadOperationException;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;
import int_.esa.eo.ngeo.iicd_d_ws._1.UserOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MonitoringController {
    @Autowired
    private DARMonitorController darMonitorController;

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
            case "monitoring_now":
                userOrder = UserOrder.STOP_IMMEDIATELY;
                break;
            case "all":
                userOrder = UserOrder.STOP_IMMEDIATELY;
                includeManualDownloads = true;
                break;
            default:
                throw new DownloadOperationException("No stop type provided.");
            }

            return commandResponseBuilder.buildCommandResponse(darMonitorController.sendUserOrder(userOrder, includeManualDownloads), String.format("Unable to execute stop command %s.", type));
        } catch (DownloadOperationException e) {
            return commandResponseBuilder.buildCommandResponse(false, e.getLocalizedMessage(), e.getClass().getName());
        }
    }

    @RequestMapping(value="/products", method = RequestMethod.GET, params="action=stop")
    @ResponseBody
    public CommandResponse stopAllProducts() {
        return stopMonitoringForDARs("all");
    }
}
