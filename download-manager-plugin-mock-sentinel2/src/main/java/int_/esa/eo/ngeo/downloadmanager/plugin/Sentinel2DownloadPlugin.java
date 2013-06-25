package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPlugin;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPluginInfo;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;
import int_.esa.eo.ngeo.downloadmanager.plugin.IProductDownloadListener;

import java.io.File;
import java.net.URI;

public class Sentinel2DownloadPlugin implements IDownloadPlugin {
	public IDownloadPluginInfo initialize(File tmpRootDir, File pluginCfgRootDir)
			throws DMPluginException {
		//It is the responsibility of the plugin to create the temporary directory and plugin directory which it requires.
		//This plugin does not either, so does not create these directories.
		
		Sentinel2DownloadPluginInfo pluginInfo = new Sentinel2DownloadPluginInfo();
		
		return pluginInfo;
	}

	public void terminate() throws DMPluginException {
		//since this plugin does not create the directories in the initialize command, no further action is required.
	}

	public IDownloadProcess createDownloadProcess(URI productURI,
			File downloadDir, String user, String password,
			IProductDownloadListener downloadListener, String proxyLocation,
			int proxyPort, String proxyUser, String proxyPassword)
			throws DMPluginException {
		return new Sentinel2DownloadProcess(productURI, downloadDir, downloadListener);
	}

}
