package int_.esa.eo.ngeo.downloadmanager.plugin;

public class HTTPDownloadPluginInfo implements IDownloadPluginInfo {

    @Override
    public String getName() {
        return "Generic Download Plugin (HTTP)";
    }

    @Override
    public int[] getPluginVersion() {
        return new int[] { 0, 7, 0};
    }

    @Override
    public String[] getMatchingPatterns() {
        return new String[] {"http://.*","https://.*"};
    }

    @Override
    public int[] getDMMinVersion() {
        return new int[] { 0, 7, 0};
    }

    @Override
    public boolean handlePause() {
        return true;
    }
}
