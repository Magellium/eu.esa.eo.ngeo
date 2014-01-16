package int_.esa.eo.ngeo.downloadmanager.download;

import static org.mockito.Mockito.mock;
import int_.esa.eo.ngeo.downloadmanager.dar.DataAccessRequestManager;
import int_.esa.eo.ngeo.downloadmanager.http.ConnectionPropertiesSynchronizedUmSsoHttpClient;
import int_.esa.eo.ngeo.downloadmanager.plugin.PluginManager;
import int_.esa.eo.ngeo.downloadmanager.settings.SettingsManager;

import org.junit.Before;

public class DownloadMonitorTest {
	private DownloadMonitor downloadMonitor;
	private DataAccessRequestManager dataAccessRequestManager;
	private PluginManager pluginManager;
	private SettingsManager settingsManager;
	private ConnectionPropertiesSynchronizedUmSsoHttpClient connectionPropertiesSynchronizedUmSsoHttpClient;
	
	private static final String FILE_URL = "http://download.tuxfamily.org/notepadplus/6.3.1/npp.6.3.1.bin.zip";
	private static final String MOCK_MONITORING_URL = "mockMonitoringURL";

	@Before
	public void setup() {
	    dataAccessRequestManager = mock(DataAccessRequestManager.class);
		pluginManager = mock(PluginManager.class);
		settingsManager = mock(SettingsManager.class);
		connectionPropertiesSynchronizedUmSsoHttpClient = mock(ConnectionPropertiesSynchronizedUmSsoHttpClient.class);
	    downloadMonitor = new DownloadMonitor(pluginManager, settingsManager, connectionPropertiesSynchronizedUmSsoHttpClient, dataAccessRequestManager);
	}
}
