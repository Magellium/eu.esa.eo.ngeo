package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpConnectionSettings;
import int_.esa.eo.ngeo.downloadmanager.transform.SchemaRepository;

import java.io.File;
import java.net.URI;
import java.util.Properties;

public class HTTPDownloadProcess extends DownloadProcess {
    public HTTPDownloadProcess(URI productURI, File downloadDir, IProductDownloadListener productDownloadListener, UmSsoHttpConnectionSettings umSsoHttpConnectionSettings, Properties pluginConfig, SchemaRepository schemaRepository) {
        super(productURI, downloadDir, productDownloadListener, umSsoHttpConnectionSettings, pluginConfig, schemaRepository);
    }

    @Override
    public boolean postDownloadProcess() throws DMPluginException {
        // The generic plugin does not require post-download processing
        return true;
    }
}