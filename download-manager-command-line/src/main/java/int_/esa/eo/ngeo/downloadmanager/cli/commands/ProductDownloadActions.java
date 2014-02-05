package int_.esa.eo.ngeo.downloadmanager.cli.commands;

import int_.esa.eo.ngeo.downloadmanager.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.downloadmanager.cli.config.DmCliSetting;
import int_.esa.eo.ngeo.downloadmanager.cli.exception.CLICommandException;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerResponseParser;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerService;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;

public class ProductDownloadActions extends ActionWithCommandResponse implements CommandMarker {
    private DownloadManagerService downloadManagerService;
    private DownloadManagerResponseParser downloadManagerResponseParser;
    private ConfigurationProvider configurationProvider;

    public ProductDownloadActions(DownloadManagerService downloadManagerService, DownloadManagerResponseParser downloadManagerResponseParser, ConfigurationProvider configurationProvider) {
        this.downloadManagerService = downloadManagerService;
        this.downloadManagerResponseParser = downloadManagerResponseParser;
        this.configurationProvider = configurationProvider;
    }

    @CliAvailabilityIndicator({"resume"})
    public boolean isResumeAvailable() {
        return true;
    }

    @CliAvailabilityIndicator({"cancel"})
    public boolean isCancelAvailable() {
        return true;
    }

    @CliAvailabilityIndicator({"pause"})
    public boolean isPauseAvailable() {
        return true;
    }

    @CliCommand(value = "resume", help = "Resume a paused product download")
    public String resume(
            @CliOption(key = { "uuid" }, mandatory = true, help = "The uuid of the product to resume") final String productUuid) {

        return sendProductAction(ProductAction.RESUME, productUuid);
    }

    @CliCommand(value = "pause", help = "Pause a product download")
    public String pause(
            @CliOption(key = { "uuid" }, mandatory = true, help = "The uuid of the product to pause") final String productUuid) {

        return sendProductAction(ProductAction.PAUSE, productUuid);
    }

    @CliCommand(value = "cancel", help = "Cancel a product download")
    public String cancel(
            @CliOption(key = { "uuid" }, mandatory = true, help = "The uuid of the product to cancel") final String productUuid) {

        return sendProductAction(ProductAction.CANCEL, productUuid);
    }

    private String sendProductAction(ProductAction productAction, String productUuid) throws CLICommandException {
        String urlAsString = String.format("%s/products/%s?action=%s", configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL), productUuid, productAction.getActionString());
        URL commandUrl;
        CommandResponse commandResponse;
        try {
            commandUrl = new URL(urlAsString);

            HttpURLConnection conn = downloadManagerService.sendGetCommand(commandUrl);
            commandResponse = downloadManagerResponseParser.parseCommandResponse(conn);
        } catch (MalformedURLException | ServiceException e) {
            throw new CLICommandException(e);
        }
        return getMessageFromCommandResponse(commandResponse, productAction.getSuccessMessage());
    }

    enum ProductAction {
        PAUSE ("pause", "Product paused."),
        RESUME("resume", "Product resumed."),
        CANCEL("cancel", "Product cancelled.");

        private final String actionString, successMessage;

        ProductAction(String actionString, String successMessage) {
            this.actionString = actionString;
            this.successMessage = successMessage;
        }

        public String getActionString() {
            return actionString;
        }

        public String getSuccessMessage() {
            return successMessage;
        }
    }
}