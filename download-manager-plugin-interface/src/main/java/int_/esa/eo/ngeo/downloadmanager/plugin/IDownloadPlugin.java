package int_.esa.eo.ngeo.downloadmanager.plugin;


import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;

import java.io.File;
import java.net.URI;

public interface IDownloadPlugin {
	
	/**
	 * @param tmpRootDir Note that plugins cannot rely on this directory to exist.
	 * @param pluginCfgRootDir Note that plugins cannot rely on this directory to exist.
	 */
	IDownloadPluginInfo initialize(File tmpRootDir, File pluginCfgRootDir) throws DMPluginException;

	void terminate() throws DMPluginException;

	IDownloadProcess createDownloadProcess(URI productURI, File downloadDir,
			String user, String password,
			IProductDownloadListener downloadListener, String proxyLocation,
			int proxyPort, String proxyUser, String proxyPassword)
			throws DMPluginException;
}
