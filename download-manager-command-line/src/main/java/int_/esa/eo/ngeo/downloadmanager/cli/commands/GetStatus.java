package int_.esa.eo.ngeo.downloadmanager.cli.commands;

import int_.esa.eo.ngeo.downloadmanager.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.downloadmanager.cli.config.DmCliSetting;
import int_.esa.eo.ngeo.downloadmanager.cli.exception.CLICommandException;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerResponseParser;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerService;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductProgress;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.rest.StatusResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;

public class GetStatus implements CommandMarker {
    private DownloadManagerService downloadManagerService;
    private DownloadManagerResponseParser downloadManagerResponseParser;
    private ConfigurationProvider configurationProvider;

    public GetStatus(DownloadManagerService downloadManagerService, DownloadManagerResponseParser downloadManagerResponseParser, ConfigurationProvider configurationProvider) {
        this.downloadManagerService = downloadManagerService;
        this.downloadManagerResponseParser = downloadManagerResponseParser;
        this.configurationProvider = configurationProvider;
    }

    @CliAvailabilityIndicator({"status"})
    public boolean isGetStatusAvailable() {
        return true;
    }

    @CliCommand(value = "status", help = "Get the status of visible DARs")
    public String getStatus() {
        try {
            String urlAsString = String.format("%s/dataAccessRequests", configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL));
            URL commandUrl = new URL(urlAsString);

            HttpURLConnection conn = downloadManagerService.sendGetCommand(commandUrl);
            StatusResponse statusResponse = downloadManagerResponseParser.parseStatusResponse(conn);
            
            List<DataAccessRequest> dataAccessRequests = statusResponse.getDataAccessRequests();
            if(dataAccessRequests.isEmpty()) {
                return "There are currently no visible DARs.";
            }else{
                return formatStatusOutput(dataAccessRequests);
            }
        } catch (IOException | ServiceException e) {
            throw new CLICommandException(e);
        }
    }

    private String formatStatusOutput(List<DataAccessRequest> dataAccessRequests) {
        StringBuilder output = new StringBuilder(100);

        for (DataAccessRequest dataAccessRequest : dataAccessRequests) {
            if(dataAccessRequest.isVisible()) {
                if(StringUtils.isNotEmpty(dataAccessRequest.getDarName())) {
                    output.append(dataAccessRequest.getDarName());
                }else{
                    output.append(dataAccessRequest.getDarURL());
                }
                if(dataAccessRequest.isMonitored()) {
                    output.append("\nMonitoring Status: ");                    
                    output.append(dataAccessRequest.getMonitoringStatus());
                }
                output.append("\n\n");
                for (Product product : dataAccessRequest.getProductList()) {
                    if(product.isVisible()) {
                        ProductProgress productProgress = product.getProductProgress();
                        output.append("\t");
                        output.append(product.getProductAccessUrl());
                        output.append(" (");
                        output.append(product.getUuid());
                        output.append(")");
                        output.append("\n\t\t");

                        output.append("Downloaded: ");
                        output.append(String.format("%,d", productProgress.getDownloadedSize()));
                        output.append(" / ");
                        if(product.getTotalFileSize() > -1) {
                            output.append(String.format("%,d", product.getTotalFileSize()));
                        }else{
                            output.append("Unknown");
                        }
                        output.append(" (");
                        if(productProgress.getProgressPercentage() > -1) {
                            output.append(productProgress.getProgressPercentage());
                        }else{
                            output.append("Unknown ");
                        }
                        output.append("%)\n\t\t");

                        output.append("Status: ");
                        output.append(productProgress.getStatus());
                        List<EDownloadStatus> terminalStatuses = new ArrayList<>();
                        terminalStatuses.add(EDownloadStatus.CANCELLED);
                        terminalStatuses.add(EDownloadStatus.COMPLETED);
                        terminalStatuses.add(EDownloadStatus.IN_ERROR);
                        if(!terminalStatuses.contains(productProgress.getStatus())) {
                            output.append("\n\t\tPriority: ");
                            output.append(product.getPriority());
                        }
                        output.append("\n\n");
                    }
                }
            }
        }

        return output.toString();
    }
}