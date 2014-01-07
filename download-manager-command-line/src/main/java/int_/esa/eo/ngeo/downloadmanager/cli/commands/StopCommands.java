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
import org.springframework.stereotype.Component;

@Component
public class StopCommands extends ActionWithCommandResponse implements CommandMarker {
    private static final String TABS = "\t\t\t\t\t\t\t";
    private static final String SUCCESS_MESSAGE = "Stop command initiated. Please use the \"status\" command to monitor the progress of the products.";

    private DownloadManagerService downloadManagerService;
    private DownloadManagerResponseParser downloadManagerResponseParser;
    private ConfigurationProvider configurationProvider;

    public StopCommands(DownloadManagerService downloadManagerService, DownloadManagerResponseParser downloadManagerResponseParser, ConfigurationProvider configurationProvider) {
        this.downloadManagerService = downloadManagerService;
        this.downloadManagerResponseParser = downloadManagerResponseParser;
        this.configurationProvider = configurationProvider;
    }

    @CliAvailabilityIndicator({"stop"})
    public boolean isStopAvailable() {
        return true;
    }

    @CliCommand(value = "stop", help = "stop command for monitoring and downloads.")
    public String stop(
            @CliOption(key = { "type" }, 
            mandatory = true, 
            help = "The type of stop command to send.\n"+
                    TABS + "* MONITORING - Stop monitoring for new downloads. All product downloads received from the monitoring service and which are not currently running will be cancelled immediately.\n" +
                    TABS + "* MONITORING_NOW - Stop monitoring for new downloads. All product downloads received from the monitoring service will be cancelled immediately.\n" +
                    TABS + "* MONITORING_ALL - Stop monitoring for new downloads. All product downloads including manual downloads will be cancelled immediately.") final StopCommandType stopCommandType) {

        String urlAsString = String.format("%s/monitoring/stop?type=%s", configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL), stopCommandType.getTypeString());
        URL commandUrl;
        CommandResponse commandResponse;
        try {
            commandUrl = new URL(urlAsString);

            HttpURLConnection conn = downloadManagerService.sendGetCommand(commandUrl);
            commandResponse = downloadManagerResponseParser.parseCommandResponse(conn);
        } catch (MalformedURLException | ServiceException e) {
            throw new CLICommandException(e);
        }

        return getMessageFromCommandResponse(commandResponse, SUCCESS_MESSAGE);
    }

    enum StopCommandType {
        MONITORING      ("monitoring"),
        MONITORING_NOW  ("monitoring_now"),
        MONITORING_ALL  ("monitoring_all");

        private final String typeString;

        StopCommandType(String typeString) {
            this.typeString = typeString;
        }

        public String getTypeString() {
            return typeString;
        }
    }
}