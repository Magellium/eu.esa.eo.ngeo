package int_.esa.eo.ngeo.dmtu.configuration;

import int_.esa.eo.ngeo.dmtu.exception.NonRecoverableException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DMTUProperties {
	
	Properties dmtuProperties = new Properties();
	
	public void loadDMTUProperties() {
		InputStream dmtuPropertiesStream = DMTUProperties.class.getResourceAsStream("/META-INF/dmtu.properties");
		dmtuProperties = new Properties();
		try {
			dmtuProperties.load(dmtuPropertiesStream);
		} catch (IOException e) {
			throw new NonRecoverableException(e);
		}
	}
	
	public Properties getDMTUProperties() {
		return dmtuProperties;
	}
	
	public String getDMTUVersion() {
		return dmtuProperties.getProperty("project.version");
	}

}