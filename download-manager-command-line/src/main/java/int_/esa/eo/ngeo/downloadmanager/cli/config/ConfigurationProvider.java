package int_.esa.eo.ngeo.downloadmanager.cli.config;

import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationProvider {
    private ResourceBundle resourceBundle;

    protected ResourceBundle loadProperties() {
         return ResourceBundle.getBundle("cli");
    }

    public String getProperty(DmCliSetting key) {
        if(resourceBundle == null) {
            resourceBundle = loadProperties();
        }

        String systemPropertyOverride = System.getProperty(key.name());
        if(StringUtils.isNotBlank(systemPropertyOverride)) {
            return systemPropertyOverride;
        }
        
        String value = resourceBundle.getString(key.toString());
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException(String.format("There is no value for %s", key));
        }
        return value;
    }
}
