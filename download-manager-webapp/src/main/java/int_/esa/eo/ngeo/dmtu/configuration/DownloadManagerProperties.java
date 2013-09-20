package int_.esa.eo.ngeo.dmtu.configuration;

import int_.esa.eo.ngeo.dmtu.exception.NonRecoverableException;

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
	
	public String getDownloadManagerVersion() {
		return downloadManagerProperties.getProperty("version");
	}

}