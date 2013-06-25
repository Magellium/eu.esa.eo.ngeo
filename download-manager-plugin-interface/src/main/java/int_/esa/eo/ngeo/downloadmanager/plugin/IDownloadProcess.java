package int_.esa.eo.ngeo.downloadmanager.plugin;


import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;

import java.io.File;

public interface IDownloadProcess {

	EDownloadStatus startDownload() throws DMPluginException;

	EDownloadStatus pauseDownload() throws DMPluginException;

	EDownloadStatus resumeDownload() throws DMPluginException;

	EDownloadStatus cancelDownload() throws DMPluginException;

	EDownloadStatus getStatus();

	File[] getDownloadedFiles();

	void disconnect() throws DMPluginException;
}
