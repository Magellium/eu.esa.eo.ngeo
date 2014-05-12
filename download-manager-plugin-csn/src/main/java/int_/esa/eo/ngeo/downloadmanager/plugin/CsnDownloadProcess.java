package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpConnectionSettings;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.builder.CsnRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.exception.CsnException;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.reader.CsnPackageReaderFactory;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.service.CsnService;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.validator.CsnValidator;
import int_.esa.eo.ngeo.downloadmanager.transform.SchemaRepository;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsnDownloadProcess extends DownloadProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsnDownloadProcess.class);

    private static final String CSN_INVALID_URI = "Invalid uri, orderId and metadata_format parameters must be included.";
    private URI originalProductURI;
    private int orderId = 0;
    private String metadataFormat;
    
    public CsnDownloadProcess(URI productURI, File downloadDir, IProductDownloadListener productDownloadListener, UmSsoHttpConnectionSettings umSsoHttpConnectionSettings, Properties pluginConfig, SchemaRepository schemaRepository) {
        super(productURI, downloadDir, productDownloadListener, umSsoHttpConnectionSettings, pluginConfig, schemaRepository);
    }
    
    @Override
    public EDownloadStatus startDownload() throws DMPluginException {
        try {
            this.originalProductURI = productURI;

            if(!isCsnPackageUrlValid(productURI)) {
                getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, CSN_INVALID_URI);
            }else{
                try {
                    productURI = convertToHttpURI(productURI);
                } catch (URISyntaxException e) {
                    LOGGER.error("Exception when converting CSN URI to HTTP URI.", e);
                    getProductDownloadProgressMonitor().setError(e);
                }
        
                /* 
                 * If the download has been cancelled do not continue with the download process.
                 * This situation can occur when a waiting product is started after a STOP_IMMEDIATELY command is sent.
                 */
                if(getProductDownloadProgressMonitor().getStatusWhenDownloadWasAborted() == EDownloadStatus.CANCELLED) {
                    return EDownloadStatus.CANCELLED;
                }
        
                if(this.productMetadata == null) {
                    retrieveDownloadDetails();
                }
                if(getStatus() == EDownloadStatus.NOT_STARTED) {
                    downloadProduct();
                }
                if(postDownloadProcess()) {
                    getProductDownloadProgressMonitor().setStatus(EDownloadStatus.COMPLETED);
                }
            }
        }catch(DMPluginException dmPluginException) {
            String errorMessage = String.format("CSN plugin Exception: %s", dmPluginException.getMessage());
            getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, errorMessage);
        }catch(Throwable t) {
            LOGGER.error(String.format("CSN plugin Exception: %s - %s", productURI.toString(), t.getClass().getName(), t.getMessage(), t));
            getProductDownloadProgressMonitor().setError(new DMPluginException(String.format("CSN Plugin Exception: %s", t.getMessage()), t));
        }
        return getStatus();
    }
    
    private boolean isCsnPackageUrlValid(URI productURI) {
        List<NameValuePair> queryStringParams = URLEncodedUtils.parse(originalProductURI, StandardCharsets.UTF_8.displayName());
        for (NameValuePair nameValuePair : queryStringParams) {
            switch (nameValuePair.getName()) {
            case "orderId":
                orderId = Integer.parseInt(nameValuePair.getValue());
                break;
            case "metadata_format":
                metadataFormat = nameValuePair.getValue();
                break;
            default:
                break;
            }
        }
        
        return (orderId != 0 && metadataFormat != null);
    }

    private URI convertToHttpURI(URI productURI) throws URISyntaxException {
        String originalProductURIAsString = productURI.toString();
        originalProductURIAsString = originalProductURIAsString.replace("csn://", "http://");
        originalProductURIAsString = originalProductURIAsString.replace("csns://", "https://");
        
        return new URI(originalProductURIAsString);
    }
    
    @Override
    public boolean postDownloadProcess() throws DMPluginException {
        URL csnServiceUrl = null;
        try {
            csnServiceUrl = new URL(pluginConfig.getProperty("CSN_SERVICE_URL"));
        } catch (MalformedURLException e) {
            LOGGER.error("Invalid CSN Service URL", e);
            getProductDownloadProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, e.getMessage());
        }
        
        CsnPackageReaderFactory csnPackageReaderFactory = new CsnPackageReaderFactory();
        CsnRequestBuilder csnRequestBuilder = new CsnRequestBuilder();
        CsnService csnService = new CsnService();

        CsnValidator csnValidator = new CsnValidator(csnPackageReaderFactory, csnRequestBuilder, csnService);

        try {
            return csnValidator.isCsnPackageValid(Paths.get(getDownloadedFiles()[0].getAbsolutePath()), orderId, csnServiceUrl);
        } catch (CsnException e) {
            LOGGER.error(String.format("Unable to validate CSN Package: %s", e.getMessage()), e);
            throw e;
        }
    }
}
