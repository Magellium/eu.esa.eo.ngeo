package int_.esa.eo.ngeo.downloadmanager.cli;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DownloadManagerCliHistoryFileNameProviderTest {
    private DownloadManagerCliHistoryFileNameProvider downloadManagerCliHistoryFileNameProvider = new DownloadManagerCliHistoryFileNameProvider();
    
    @Test
    public void downloadManagerCliPromptProviderTest() {
        System.setProperty("DM_HOME", "C:/tmp/ngEO-download-manager");
        
        assertEquals("C:/tmp/ngEO-download-manager/logs/CLI_command_history.log", downloadManagerCliHistoryFileNameProvider.getHistoryFileName());
        assertEquals("Download Manager CLI history file name provider", downloadManagerCliHistoryFileNameProvider.name());
    }
}
