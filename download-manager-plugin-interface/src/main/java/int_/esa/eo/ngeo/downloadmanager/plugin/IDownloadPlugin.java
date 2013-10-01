package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;

import java.io.File;
import java.net.URI;

/**
 * The purpose of this interface it to provide a means of initializing a plugin
 * and creating a download process. An instance of this interface within a
 * plugin is used by the Download Manager to determine if a plugin is available.
 */
public interface IDownloadPlugin {

	/**
	 * Initialize the plugin.
	 * 
	 * @param tmpRootDir
	 *            the root directory to create temporary files. Note that
	 *            plugins cannot rely on this directory to exist.
	 * @param pluginCfgRootDir
	 *            the root directory where the plugin can store / load their
	 *            configuration data. Note that plugins cannot rely on this
	 *            directory to exist.
	 * @return An instance of
	 *         {@link int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPluginInfo}
	 *         which can be used to determine the name and version of the
	 *         plugin.
	 * @throws DMPluginException
	 *             if an error occurs whilst initializing the plugin.
	 */
	IDownloadPluginInfo initialize(File tmpRootDir, File pluginCfgRootDir)
			throws DMPluginException;

	/**
	 * Create a download process.
	 * 
	 * @param productURI
	 *            The product URI to download (including download options)
	 * @param downloadDir
	 *            The directory to place the downloaded file(s)
	 * @param user
	 *            The UMSSO username.
	 * @param password
	 *            The UMSSO password.
	 * @param downloadListener
	 *            The download listener provided for the particular product
	 *            download.
	 * @param proxyLocation
	 *            The location of the Web proxy.
	 * @param proxyPort
	 *            The port of the Web proxy.
	 * @param proxyUser
	 *            The username to be used with the Web proxy.
	 * @param proxyPassword
	 *            The password to be used with the Web proxy.
	 * @return An instance of
	 *         {@link int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess}
	 *         which can be used to download a product.
	 * @throws DMPluginException
	 *             if an error occurs whilst creating the download process.
	 */
	IDownloadProcess createDownloadProcess(URI productURI, File downloadDir,
			String user, String password,
			IProductDownloadListener downloadListener, String proxyLocation,
			int proxyPort, String proxyUser, String proxyPassword)
			throws DMPluginException;

	/**
	 * Called by the Download Manager at shutdown. It can perform cleaning and
	 * finalization routines.
	 * 
	 * @throws DMPluginException
	 *             if an error occurs whilst terminating the plugin.
	 */
	void terminate() throws DMPluginException;
}
