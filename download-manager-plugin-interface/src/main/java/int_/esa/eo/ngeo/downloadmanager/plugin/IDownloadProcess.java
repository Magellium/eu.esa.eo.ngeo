package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;

import java.io.File;

public abstract interface IDownloadProcess {
	public abstract EDownloadStatus startDownload() throws DMPluginException;

	public abstract EDownloadStatus pauseDownload() throws DMPluginException;

	public abstract EDownloadStatus resumeDownload() throws DMPluginException;

	public abstract EDownloadStatus cancelDownload() throws DMPluginException;

	public abstract EDownloadStatus getStatus();

	public abstract File[] getDownloadedFiles();

	public abstract void disconnect() throws DMPluginException;
}

/*
 * Location: C:\Users\lby\Desktop\download-manager-plugin-interface-1.6.jar
 * Qualified Name: int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess Java
 * Class Version: 5 (49.0) JD-Core Version: 0.7.0.1
 */