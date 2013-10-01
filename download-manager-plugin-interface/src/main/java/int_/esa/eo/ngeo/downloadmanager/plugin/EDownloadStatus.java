package int_.esa.eo.ngeo.downloadmanager.plugin;

/**
 * Defines the possible statuses of a product download.
 */
public enum EDownloadStatus {
	/**
	 * The product download has not started. This status may be shown when:
	 * <ul>
	 * <li>the product download process is being initiated.</li>
	 * <li>the product download process has been queued by the Download Manager
	 * due to limited concurrent downloads.</li>
	 * </ul>
	 */
	NOT_STARTED,

	/**
	 * The product download is in an idle status. This status is used when
	 * Product Facility returns a 202 code - request is accepted but product is
	 * not ready
	 */
	IDLE,

	/* running statuses */
	/**
	 * The product download is running. This status is used when the download of
	 * a product is in progress.
	 */
	RUNNING,
	/**
	 * The product download is currently paused.
	 */
	PAUSED,

	/* Terminal statuses */
	/**
	 * The product download has been cancelled.
	 */
	CANCELLED,
	/**
	 * The product download is in error. This status is used when an error has
	 * occurred whilst downloading a product.
	 */
	IN_ERROR,
	/**
	 * The product download has been completed.
	 */
	COMPLETED
}
