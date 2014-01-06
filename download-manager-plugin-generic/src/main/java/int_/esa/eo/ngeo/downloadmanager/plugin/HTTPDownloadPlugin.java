package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpConnectionSettings;
import int_.esa.eo.ngeo.downloadmanager.plugin.config.PluginConfigurationLoader;
import int_.esa.eo.ngeo.downloadmanager.transform.SchemaRepository;
import int_.esa.eo.ngeo.schema.ngeobadrequestresponse.BadRequestResponse;
import int_.esa.eo.ngeo.schema.ngeomissingproductresponse.MissingProductResponse;
import int_.esa.eo.ngeo.schema.ngeoproductdownloadresponse.ProductDownloadResponse;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.metalinker.Metalink;

public class HTTPDownloadPlugin implements IDownloadPlugin {
	
	PluginConfigurationLoader pluginConfigurationLoader = new PluginConfigurationLoader();
	
	private Properties pluginConfig;
	private SchemaRepository schemaRepository;
	
	public IDownloadPluginInfo initialize(File tmpRootDir, File pluginCfgRootDir) throws DMPluginException {
		pluginConfig = pluginConfigurationLoader.loadPluginConfiguration(HTTPDownloadPlugin.class.getName(), pluginCfgRootDir);
		createSchemaRepository();
		
		HTTPDownloadPluginInfo pluginInfo = new HTTPDownloadPluginInfo();
		
		return pluginInfo;
	}

	public void terminate() throws DMPluginException {
		//since this plugin does not create the directories in the initialize command, no further action is required.
	}

	public IDownloadProcess createDownloadProcess(URI productURI,
			File downloadDir, String umssoUsername, String umssoPassword,
			IProductDownloadListener downloadListener, String proxyLocation,
			int proxyPort, String proxyUser, String proxyPassword)
			throws DMPluginException {
		
		UmSsoHttpConnectionSettings umSsoHttpConnectionSettings = new UmSsoHttpConnectionSettings(umssoUsername, umssoPassword, proxyLocation, proxyPort, proxyUser, proxyPassword);
		return new HTTPDownloadProcess(productURI, downloadDir, downloadListener, umSsoHttpConnectionSettings, pluginConfig, schemaRepository);
	}

	private void createSchemaRepository() {
		Map<Class<?>, String> schemaMap = new HashMap<>();
		schemaMap.put(Metalink.class, "schemas/metalink/3.0/metalink.xsd");
		schemaMap.put(BadRequestResponse.class, "schemas/DAGICD/ngEOBadRequestResponse.xsd");
		schemaMap.put(MissingProductResponse.class, "schemas/DAGICD/ngEOMissingProductResponse.xsd");
		schemaMap.put(ProductDownloadResponse.class, "schemas/DAGICD/ngEOProductDownloadResponse.xsd");
		
		schemaRepository = new SchemaRepository(schemaMap);
	}
}
