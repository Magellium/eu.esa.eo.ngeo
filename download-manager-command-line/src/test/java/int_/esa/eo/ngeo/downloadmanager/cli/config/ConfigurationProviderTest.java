package int_.esa.eo.ngeo.downloadmanager.cli.config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.Test;

public class ConfigurationProviderTest {
    private ConfigurationProvider configurationProvider;

    @Before
    public void setup() {
        configurationProvider = spy(new ConfigurationProvider());
    }
    
    @Test
    public void configurationProviderTest() {
        assertEquals("ngEO-DM", configurationProvider.getProperty(DmCliSetting.DM_CLI_PROMPT));
        assertEquals("Test Download Manager Title", configurationProvider.getProperty(DmCliSetting.DM_TITLE));
        assertEquals("http://localhost:8082/download-manager", configurationProvider.getProperty(DmCliSetting.DM_WEBAPP_URL));
        assertEquals("1.0.0", configurationProvider.getProperty(DmCliSetting.VERSION));
    }

    @Test
    public void configurationProviderTestBlankProperties() {
        when(configurationProvider.loadProperties()).thenReturn(ResourceBundle.getBundle("blank"));

        try {
            assertEquals("ngEO-DM", configurationProvider.getProperty(DmCliSetting.DM_CLI_PROMPT));
        }catch(IllegalArgumentException ex) {
            assertEquals("There is no value for DM_CLI_PROMPT", ex.getMessage());
        }
    }
}
