package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.configuration.DownloadManagerProperties;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.exception.NoPluginAvailableException;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.settings.NonUserModifiableSetting;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginManager {
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    private SettingsManager settingsManager;
    private DownloadManagerProperties downloadManagerProperties;

    private List<IDownloadPluginInfo> downloadPluginInfoList;
    private Map<String, IDownloadPlugin> mapOfPluginNamesToPlugins;

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);

    public PluginManager(SettingsManager settingsManager, DownloadManagerProperties downloadManagerProperties) {
        this.settingsManager = settingsManager;
        this.downloadManagerProperties = downloadManagerProperties;

        downloadPluginInfoList = new ArrayList<IDownloadPluginInfo>();
        mapOfPluginNamesToPlugins = new HashMap<String, IDownloadPlugin>();
    }

    public void detectPlugins() {
        LOGGER.info("Detecting plugins.");
        final String dmHome = System.getenv("DM_HOME");
        LOGGER.debug(String.format("DM_HOME = \"%s\"", dmHome));
        String dirPlugins = settingsManager.getSetting(NonUserModifiableSetting.DIR_PLUGINS);
        Path pluginFolder = Paths.get(dmHome, dirPlugins);
        String pluginsPathAsString = pluginFolder.toString();
        if (!pluginFolder.toFile().exists()) {
            throw new NonRecoverableException(String.format("Plugin directory %s does not exist", pluginsPathAsString));
        }

        PluginFinder<IDownloadPlugin> pluginFinder = new PluginFinder<IDownloadPlugin>(IDownloadPlugin.class);
        try {
            pluginFinder.search(pluginsPathAsString);
        } catch (Exception e) {
            // TODO: Consider, instead of this, just logging a warning? Depends whether the default downloader is guaranteed to be loaded.
            throw new NonRecoverableException(e);
        }

        List<IDownloadPlugin> downloadPluginList = pluginFinder.getPluginCollection();
        for (IDownloadPlugin downloadPlugin : downloadPluginList) {
            Path tmpRootDir = Paths.get(System.getProperty(JAVA_IO_TMPDIR));
            Path pluginCfgRootDir = Paths.get("conf", "plugins");

            IDownloadPluginInfo downloadPluginInfo;
            try {
                downloadPluginInfo = downloadPlugin.initialize(tmpRootDir.toFile(), pluginCfgRootDir.toFile());
            } catch (DMPluginException e) {
                throw new NonRecoverableException("Unable to initialize plugin. IDownloadPluginInfo not supplied.", e);
            }

            if (arePluginAndDownloadManagerVersionCompatible(downloadPluginInfo)) {
                downloadPluginInfoList.add(downloadPluginInfo); 
                mapOfPluginNamesToPlugins.put(downloadPluginInfo.getName(), downloadPlugin);
            }else{
                LOGGER.info(String.format("Ignoring plugin %s because of incompatibility with version of Download Manager", downloadPluginInfo.getName()));
            }
        }
        LOGGER.info(String.format("%s plugins loaded.", downloadPluginInfoList.size()));
    }

    private boolean arePluginAndDownloadManagerVersionCompatible(IDownloadPluginInfo downloadPluginInfo) {
        boolean areVersionsCompatible = true; 
        int[] dmMinVersion = downloadPluginInfo.getDMMinVersion();
        String dmVersionString = downloadManagerProperties.getDownloadManagerVersion();
        String[] dmVersionParts = dmVersionString.split("\\.");
        for (int i=0; i < dmMinVersion.length; i++) {
            // XXX: Addition of some error handling to cope with non-integer version parts would be desirable.
            if (dmMinVersion[i] > Integer.parseInt(dmVersionParts[i])) {
                areVersionsCompatible = false;
                break;
            }
        }
        return areVersionsCompatible;
    }

    public IDownloadPlugin determinePlugin(String downloadUrl) throws NoPluginAvailableException {
        for (IDownloadPluginInfo downloadPluginInfo : downloadPluginInfoList) {
            String[] matchingPatterns = downloadPluginInfo.getMatchingPatterns();
            for (int i = 0; i < matchingPatterns.length; i++) {
                String matchingPattern = matchingPatterns[i];
                Pattern p = Pattern.compile(matchingPattern);
                Matcher m = p.matcher(downloadUrl);
                if (m.matches()) {
                    return mapOfPluginNamesToPlugins.get(downloadPluginInfo.getName());
                }
            }
        }
        throw new NoPluginAvailableException(String.format("No available plugin for url %s", downloadUrl));
    }

    public void cleanUp() {
        LOGGER.debug("Cleaning up plugins");
        for (IDownloadPlugin downloadPlugin : mapOfPluginNamesToPlugins.values()) {
            try {
                downloadPlugin.terminate();
            } catch (DMPluginException e) {
                LOGGER.error("Unable to terminate plugin.", e);
            }
        }
    }

}