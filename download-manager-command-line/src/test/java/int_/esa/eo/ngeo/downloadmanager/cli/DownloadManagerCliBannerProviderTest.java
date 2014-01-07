package int_.esa.eo.ngeo.downloadmanager.cli;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.cli.config.ConfigurationProvider;
import int_.esa.eo.ngeo.downloadmanager.cli.config.DmCliSetting;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.shell.support.util.OsUtils;

@RunWith(MockitoJUnitRunner.class)
public class DownloadManagerCliBannerProviderTest {
    private DownloadManagerCliBannerProvider downloadManagerCliBannerProvider;
    private static final String BANNER_WRAPPER = "===========================================================";
    
    @Test
    public void downloadManagerCliPromptProviderTest() {
        ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
        downloadManagerCliBannerProvider = new DownloadManagerCliBannerProvider(configurationProvider);
        when(configurationProvider.getProperty(DmCliSetting.VERSION)).thenReturn("1.0.0");
        when(configurationProvider.getProperty(DmCliSetting.DM_TITLE)).thenReturn("ngEO Download Manager");
        
        assertEquals("Welcome to the ngEO Download Manager CLI", downloadManagerCliBannerProvider.getWelcomeMessage());
        assertEquals(BANNER_WRAPPER + OsUtils.LINE_SEPARATOR + "ngEO Download Manager CLI v1.0.0" + OsUtils.LINE_SEPARATOR + BANNER_WRAPPER + OsUtils.LINE_SEPARATOR, downloadManagerCliBannerProvider.getBanner());
    }
}
