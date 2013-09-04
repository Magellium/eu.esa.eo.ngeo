package int_.esa.eo.ngeo.downloadmanager.plugin.thread;

import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;

public interface AbortableFileDownload {
	public void abortFileDownload(EDownloadStatus fileDownloadStatus);
}
