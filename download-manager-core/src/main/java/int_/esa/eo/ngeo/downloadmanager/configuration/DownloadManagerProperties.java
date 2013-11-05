package int_.esa.eo.ngeo.downloadmanager.configuration;

import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DownloadManagerProperties {
	Properties downloadManagerProperties = new Properties();
	
	public void loadDownloadManagerProperties() {
		InputStream downloadManagerPropertiesStream = DownloadManagerProperties.class.getResourceAsStream("/META-INF/download-manager.properties");
		downloadManagerProperties = new Properties();
		try {
			downloadManagerProperties.load(downloadManagerPropertiesStream);
		} catch (IOException e) {
			throw new NonRecoverableException(e);
		}
	}
	
	public Properties getDownloadManagerProperties() {
		return downloadManagerProperties;
	}
	
	//Development versions now include "-SNAPSHOT" in the version - remove this for the version check
	public String getDownloadManagerVersion() {
		String downloadManagerVersion = downloadManagerProperties.getProperty("version");
		downloadManagerVersion = downloadManagerVersion.replaceAll("-SNAPSHOT", "");
		return downloadManagerVersion;
	}
}