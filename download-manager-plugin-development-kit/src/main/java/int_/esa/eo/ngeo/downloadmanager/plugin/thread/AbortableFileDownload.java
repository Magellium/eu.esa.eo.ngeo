package int_.esa.eo.ngeo.downloadmanager.plugin.thread;

import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;

public interface AbortableFileDownload {
    void abortFileDownload(EDownloadStatus fileDownloadStatus);
}
