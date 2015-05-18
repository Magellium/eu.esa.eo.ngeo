package int_.esa.eo.ngeo.downloadmanager.plugin;

public abstract interface IDownloadPluginInfo {
	public abstract String getName();

	public abstract int[] getPluginVersion();

	public abstract String[] getMatchingPatterns();

	public abstract int[] getDMMinVersion();

	public abstract boolean handlePause();
}

/*
 * Location: C:\Users\lby\Desktop\download-manager-plugin-interface-1.6.jar
 * Qualified Name: int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPluginInfo
 * Java Class Version: 5 (49.0) JD-Core Version: 0.7.0.1
 */