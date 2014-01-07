package int_.esa.eo.ngeo.downloadmanager.cli.commands;

import int_.esa.eo.ngeo.downloadmanager.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.downloadmanager.cli.config.DmCliSetting;
import int_.esa.eo.ngeo.downloadmanager.cli.exception.CLICommandException;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerResponseParser;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerService;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class ChangeProductPriority extends ActionWithCommandResponse implements CommandMarker {
    private static final String SUCCESS_MESSAGE = "Product priority changed.";
    private DownloadManagerService downloadManagerService;
    private DownloadManagerResponseParser downloadManagerResponseParser;
    private ConfigurationProvider configurationProvider;

    public ChangeProductPriority(DownloadManagerService downloadManagerService, DownloadManagerResponseParser downloadManagerResponseParser, ConfigurationProvider configurationProvider) {
        this.downloadManagerService = downloadManagerService;
        this.downloadManagerResponseParser = downloadManagerResponseParser;
        this.configurationProvider = configurationProvider;
    }

    @CliAvailabilityIndicator({"change-priority"})
    public boolean isCancelAvailable() {
        return true;
    }

    @CliCommand(value = "change-priority", help = "Change the priority of a product download")
    public String changePriority(
            @CliOption(key = { "uuid" }, mandatory = true, help = "The uuid of the product to change priority") final String productUuid,
            @CliOption(key = { "priority" }, mandatory = true, help = "The priority to change this product to") final ProductPriority newPriority) {

        CommandResponse commandResponse;
        try {
            String urlAsString = String.format("%s/products/%s?action=changePriority&newPriority=%s", configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL), productUuid, newPriority.name());
            URL commandUrl = new URL(urlAsString);

            HttpURLConnection conn = downloadManagerService.sendGetCommand(commandUrl);
            commandResponse = downloadManagerResponseParser.parseCommandResponse(conn);
        } catch (MalformedURLException | ServiceException e) {
            throw new CLICommandException(e);
        }

        return getMessageFromCommandResponse(commandResponse, SUCCESS_MESSAGE);
    }

}