package int_.esa.eo.ngeo.downloadmanager.cli;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.downloadmanager.cli.config.DmCliSetting;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DownloadManagerCliPromptProviderTest {
    private DownloadManagerCliPromptProvider downloadManagerCliPromptProvider;
    
    @Test
    public void downloadManagerCliPromptProviderTest() {
        ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
        downloadManagerCliPromptProvider = new DownloadManagerCliPromptProvider(configurationProvider);
        when(configurationProvider.getProperty(DmCliSetting.DM_CLI_PROMPT)).thenReturn("ngEO-DM");
        when(configurationProvider.getProperty(DmCliSetting.DM_TITLE)).thenReturn("ngEO Download Manager");

        assertEquals("ngEO-DM:>", downloadManagerCliPromptProvider.getPrompt());
        assertEquals("ngEO Download Manager CLI prompt provider", downloadManagerCliPromptProvider.name());
    }
}
