package int_.esa.eo.ngeo.downloadmanager.cli.commands;

import int_.esa.eo.ngeo.downloadmanager.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.downloadmanager.cli.config.DmCliSetting;
import int_.esa.eo.ngeo.downloadmanager.cli.exception.CLICommandException;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerResponseParser;
import int_.esa.eo.ngeo.downloadmanager.cli.service.DownloadManagerService;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithDarDetails;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang.StringUtils;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;

public class AddManualDownload extends ActionWithCommandResponse implements CommandMarker {
    private DownloadManagerService downloadManagerService;
    private DownloadManagerResponseParser downloadManagerResponseParser;
    private ConfigurationProvider configurationProvider;

    public AddManualDownload(DownloadManagerService downloadManagerService, DownloadManagerResponseParser downloadManagerResponseParser, ConfigurationProvider configurationProvider) {
        this.downloadManagerService = downloadManagerService;
        this.downloadManagerResponseParser = downloadManagerResponseParser;
        this.configurationProvider = configurationProvider;
    }
    
    @CliAvailabilityIndicator({"add-dar"})
    public boolean isAddDarAvailable() {
        return true;
    }

    @CliCommand(value = "add-dar", help = "Manually add a DAR")
    public String addDar(
            @CliOption(key = { "url" }, mandatory = true, help = "URL of a Data Access Request") final String darUrl,
            @CliOption(key = { "priority" }, mandatory = false, help = "Priority which all products should be set to.") final ProductPriority priority) {
        ManualDownloadType manualDownloadType = ManualDownloadType.DAR;
        HttpURLConnection conn = getHttpConnectionForAdd(manualDownloadType.getPostParameterString(), darUrl, priority);
        
        return getMessageFromHttpConnection(conn, manualDownloadType);
    }

    private HttpURLConnection getHttpConnectionForAdd(String postParameterString, String url, ProductPriority priority) {
        if(priority == null) {
            priority = ProductPriority.NORMAL;
        }
        
        String urlAsString = String.format("%s/download", configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL));
        HttpURLConnection conn;
        try {
            URL commandUrl = new URL(urlAsString);
            String parameters = String.format(postParameterString, URLEncoder.encode(url, StandardCharsets.UTF_8.displayName()), priority.name());
            conn = downloadManagerService.sendPostCommand(commandUrl, parameters);
        } catch (MalformedURLException | UnsupportedEncodingException | ServiceException e) {
            throw new CLICommandException(e);
        }
        return conn;
    }

    @CliAvailabilityIndicator({"add-product"})
    public boolean isAddProductAvailable() {
        return true;
    }

    @CliCommand(value = "add-product", help = "Manually add a product")
    public String addProduct(
            @CliOption(key = { "url" }, mandatory = true, help = "URL of a single product to be downloaded") final String productDownloadUrl,
            @CliOption(key = { "priority" }, mandatory = false, help = "Priority which all products should be set to.") final ProductPriority priority) {

        ManualDownloadType manualDownloadType = ManualDownloadType.PRODUCT;
        HttpURLConnection conn = getHttpConnectionForAdd(manualDownloadType.getPostParameterString(), productDownloadUrl, priority);

        return getMessageFromHttpConnection(conn, manualDownloadType);
    }
    
    enum ManualDownloadType {
        DAR ("darUrl=%s&priority=%s", "DAR added, UUID is %s. Please use the \"status\" command to monitor the progress."),
        PRODUCT ("productDownloadUrl=%s&priority=%s", "Product added, DAR UUID is %s and Product UUID is %s. Please use the \"status\" command to monitor the progress.");

        private final String postParameterString, successMessage;

        ManualDownloadType(String postParameterString, String successMessage) {
            this.postParameterString = postParameterString;
            this.successMessage = successMessage;
        }

        public String getPostParameterString() {
            return postParameterString;
        }

        public String getSuccessMessage() {
            return successMessage;
        }
    }
    
    public String getMessageFromHttpConnection(HttpURLConnection conn, ManualDownloadType manualDownloadType) {
        CommandResponseWithDarDetails commandResponseWithDarDetails;
        try {
            commandResponseWithDarDetails = downloadManagerResponseParser.parseCommandResponseWithDarDetails(conn);
        } catch (ServiceException  e) {
            throw new CLICommandException(e);
        }

        return getMessageFromCommandResponse(commandResponseWithDarDetails, manualDownloadType.getSuccessMessage());
    }
    
    @Override
    public String getMessageFromCommandResponse(CommandResponse commandResponse, String successMessage) {
        if (commandResponse != null && commandResponse.isSuccess()) {
            if(commandResponse instanceof CommandResponseWithDarDetails) {
                CommandResponseWithDarDetails commandResponseWithDarDetails = (CommandResponseWithDarDetails)commandResponse;
                if(StringUtils.isNotEmpty(commandResponseWithDarDetails.getProductUuid())) {
                    return String.format(successMessage, commandResponseWithDarDetails.getDarUuid(), commandResponseWithDarDetails.getProductUuid());
                }else{
                    return String.format(successMessage, commandResponseWithDarDetails.getDarUuid());
                }
            }else{
                throw createCommandExceptionUnsuccessfulCommandResponse(commandResponse);
            }
        } else {
            throw createCommandExceptionUnsuccessfulCommandResponse(commandResponse);
        }
    }
}