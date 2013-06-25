package int_.esa.eo.ngeo.downloadmanager.plugin;

public interface IDownloadPluginInfo {
	String getName();

	int[] getPluginVersion();

	String[] getMatchingPatterns();

	int[] getDMMinVersion();

	boolean handlePause();
}
