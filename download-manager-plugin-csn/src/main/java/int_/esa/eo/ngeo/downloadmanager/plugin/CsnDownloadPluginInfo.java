package int_.esa.eo.ngeo.downloadmanager.plugin;


import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPluginInfo;

public class CsnDownloadPluginInfo implements IDownloadPluginInfo {

    public String getName() {
        return "CSN Download Plugin (HTTP)";
    }

    public int[] getPluginVersion() {
        return new int[] { 0, 6, 1};
    }

    public String[] getMatchingPatterns() {
        return new String[] {"csn://.*","csns://.*"};
    }

    public int[] getDMMinVersion() {
        return new int[] { 0, 6, 1};
    }

    public boolean handlePause() {
        return true;
    }
}
