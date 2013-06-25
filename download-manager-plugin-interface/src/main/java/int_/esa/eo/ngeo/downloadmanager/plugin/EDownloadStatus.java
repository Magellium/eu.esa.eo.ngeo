package int_.esa.eo.ngeo.downloadmanager.plugin;

public enum EDownloadStatus {
	NOT_STARTED,

	/*
	 * idle status - this status is used when Product Facility returns a 202
	 * code - request is accepted but product is not ready
	 */
	IDLE,

	/* running status */
	RUNNING, PAUSED,

	/* end status */
	CANCELLED, IN_ERROR, COMPLETED
}
