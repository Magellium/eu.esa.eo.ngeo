package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;

import java.io.File;

/**
 * The purpose of this interface it to provide a means of creating and running a
 * process for a product download.
 */
public interface IDownloadProcess {

	/**
	 * Starts the download process for a product. This method is called once by
	 * the Download Manager. Plugin is expected to start the download and then
	 * to return the status without waiting the download to end. If the
	 * requested download is an incomplete download from a previous session, the
	 * plugin is expected (if possible) to resume the download. Otherwise it
	 * should restart from scratch.
	 * 
	 * @return the status of the product once it has been started.
	 * @throws DMPluginException
	 *             if the download cannot be started
	 */
	EDownloadStatus startDownload() throws DMPluginException;

	/**
	 * Pause the product download.
	 * 
	 * This method will never be called by the Download Manager if the plugin
	 * doesn’t handle the pause/resume mechanism.
	 * 
	 * The plugin is expected to pause the download, release open connections
	 * and other resources that can be needed for other downloads and then
	 * return the PAUSED status.
	 * 
	 * @return the status of the product once it has been paused.
	 * @throws DMPluginException
	 *             if the download cannot be paused
	 */
	EDownloadStatus pauseDownload() throws DMPluginException;

	/**
	 * Resume the product download.
	 * 
	 * This method will never be called by the Download Manager if the plugin
	 * doesn’t handle the pause/resume mechanism.
	 * 
	 * The plugin is expected to resume the download and return the RUNNING
	 * status.
	 * 
	 * @return the status of the product once it has been resumed.
	 * @throws DMPluginException
	 *             if the download cannot be resumed
	 */
	EDownloadStatus resumeDownload() throws DMPluginException;

	/**
	 * Cancel the product download.
	 * 
	 * Plugin is expected to cancel the download and to return the CANCELLED
	 * status. Plugin is also expected to remove all temporary data related to
	 * the download.
	 * 
	 * @return the status of the product once it has been cancelled.
	 * @throws DMPluginException
	 *             if the download cannot be cancelled
	 */
	EDownloadStatus cancelDownload() throws DMPluginException;

	/**
	 * Return the current status of the download. Note: this status might be
	 * used by the Download Manager to optimize downloads concurrency. In
	 * particular, if the status returned is IDLE (meaning that the server
	 * accepted the request but product is not ready), the Download Manager
	 * might pause temporarily the download in order to free its corresponding
	 * resources (such as its download thread) to start another download in the
	 * meanwhile. Pausing/resuming the download in such a situation is indeed
	 * subject to the response to the handlePause() method described above.
	 * 
	 * @return the current status of the product download
	 */
	EDownloadStatus getStatus();

	/**
	 * Get the files which have been downloaded for this product.
	 * 
	 * Called by the Download Manager when the process has notified the listener
	 * of a COMPLETED status.
	 * 
	 * @return The reference to all downloaded files if the download is
	 *         COMPLETED. Returns null otherwise.
	 * 
	 */
	File[] getDownloadedFiles();

	/**
	 * Finish the download process.
	 * 
	 * This method is the last one called by the Download Manager on a
	 * IDownloadProcess instance. It is called by the Download Manager either:
	 * <ul>
	 * <li>after the status COMPLETED, CANCELLED or IN_ERROR has been notified
	 * by the plugin to the Download Manager and the reference of downloaded
	 * files has been retrieved by the later.</li>
	 * <li>when the Download Manager is shut down. In this second case, the
	 * plugin is expected to :
	 * <ul>
	 * <li>if RUNNING : stop the download</li>
	 * <li>if RUNNING or PAUSED : store onto disk the current download state (if
	 * possible) in order to restart it</li>
	 * </ul>
	 * </ul>
	 * 
	 * @throws DMPluginException
	 *             if an error ocurrs when disconnecting the download process.
	 */
	void disconnect() throws DMPluginException;
}
