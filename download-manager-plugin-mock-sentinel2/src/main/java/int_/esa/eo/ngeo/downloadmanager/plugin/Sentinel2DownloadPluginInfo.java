package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPluginInfo;

public class Sentinel2DownloadPluginInfo implements IDownloadPluginInfo {

	public String getName() {
		return "Mock Sentinel-2 Plugin";
	}

	public int[] getPluginVersion() {
	    //Version number is not changed as this is a mock plugin
		return new int[] { 0, 0, 1};
	}

	public String[] getMatchingPatterns() {
		return new String[] {"s2://.*"};
	}

	public int[] getDMMinVersion() {
		return new int[] { 0, 7, 0};
	}

	public boolean handlePause() {
		return true;
	}
}
