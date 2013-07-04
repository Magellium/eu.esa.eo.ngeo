package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadManager.plugin.PluginConfigurationLoader;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;

import java.io.File;
import java.net.URI;
import java.util.Properties;

public class HTTPDownloadPlugin implements IDownloadPlugin {
	
	PluginConfigurationLoader pluginConfigurationLoader = new PluginConfigurationLoader();
	
	private Properties pluginConfig;
	
	public IDownloadPluginInfo initialize(File tmpRootDir, File pluginCfgRootDir) throws DMPluginException {
		pluginConfig = pluginConfigurationLoader.loadPluginConfiguration(HTTPDownloadPlugin.class.getName(), pluginCfgRootDir);
		
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
		
		return new HTTPDownloadProcess(productURI, downloadDir, downloadListener, proxyLocation, proxyPort, proxyUser, proxyPassword, umssoUsername, umssoPassword, pluginConfig);
	}

}
