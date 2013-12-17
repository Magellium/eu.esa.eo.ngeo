package int_.esa.eo.ngeo.downloadmanager.plugin.config;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginConfigurationLoader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigurationLoader.class);
	
	public Properties loadPluginConfiguration(String fullyqualifiedPluginClassName, File pluginCfgRootDir) throws DMPluginException {
		Properties configuration = new Properties();
		StringBuilder propertiesFileNameBuilder = new StringBuilder(fullyqualifiedPluginClassName);
		propertiesFileNameBuilder.append(".properties");
		Path propertiesPath = Paths.get(pluginCfgRootDir.toString(), propertiesFileNameBuilder.toString());
		InputStream in = null;
		try {
			File propertiesFile = new File(propertiesPath.toString()); 
			if (propertiesFile.exists()) {
				in = new FileInputStream(propertiesFile);
				// TODO: Investigate whether wrapping the InputStream within a BufferedInputStream is advantageous.
				configuration.load(in);
			} else {
				LOGGER.info(String.format("Plugin %s does not have configuration. To create some, create %s", fullyqualifiedPluginClassName, propertiesPath.toString()));
			}
		} catch (IOException e) {
			throw new DMPluginException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Swallow
				}
			}
		}
		return configuration;
	}
	
}