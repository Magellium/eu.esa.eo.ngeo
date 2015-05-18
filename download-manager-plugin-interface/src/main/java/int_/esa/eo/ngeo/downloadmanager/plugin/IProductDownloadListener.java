package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;

public abstract interface IProductDownloadListener {
	public abstract void productDetails(String paramString,
			Integer paramInteger, Long paramLong);

	public abstract void progress(Integer paramInteger, Long paramLong,
			EDownloadStatus paramEDownloadStatus, String paramString);

	public abstract void progress(Integer paramInteger, Long paramLong,
			EDownloadStatus paramEDownloadStatus, String paramString,
			DMPluginException paramDMPluginException);
}

/*
 * Location: C:\Users\lby\Desktop\download-manager-plugin-interface-1.6.jar
 * Qualified Name:
 * int_.esa.eo.ngeo.downloadmanager.plugin.IProductDownloadListener Java Class
 * Version: 5 (49.0) JD-Core Version: 0.7.0.1
 */