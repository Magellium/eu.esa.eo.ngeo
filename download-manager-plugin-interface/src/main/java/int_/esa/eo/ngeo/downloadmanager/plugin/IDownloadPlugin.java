package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;

import java.io.File;
import java.net.URI;

public abstract interface IDownloadPlugin {
	public abstract IDownloadPluginInfo initialize(File paramFile1,
			File paramFile2) throws DMPluginException;

	public abstract IDownloadProcess createDownloadProcess(URI paramURI,
			File paramFile, String paramString1, String paramString2,
			IProductDownloadListener paramIProductDownloadListener,
			String paramString3, int paramInt, String paramString4,
			String paramString5) throws DMPluginException;

	public abstract void terminate() throws DMPluginException;
}

/*
 * Location: C:\Users\lby\Desktop\download-manager-plugin-interface-1.6.jar
 * Qualified Name: int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPlugin Java
 * Class Version: 5 (49.0) JD-Core Version: 0.7.0.1
 */