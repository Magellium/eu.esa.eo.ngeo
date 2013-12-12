package int_.esa.eo.ngeo.downloadmanager.plugin.model;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class FileDownloadMetadataTest {

	@Test
	public void testPathsNoFolder() throws MalformedURLException {
		URL fileURL = new URL("http://www.test.url/s1a-ew2-slc-vv-20120101t042031-20120101t042037-001771-000001-002.xml");
		String fileName = "s1a-ew2-slc-vv-20120101t042031-20120101t042037-001771-000001-002.xml";
		Path downloadPath = Paths.get("C:\\test\\download\\path");
		long downloadSize = 4096L;
		FileDownloadMetadata fileDownloadMetadata = new FileDownloadMetadata(fileURL, fileName, downloadSize, downloadPath);
		
		assertEquals("C:\\test\\download\\path\\.s1a-ew2-slc-vv-20120101t042031-20120101t042037-001771-000001-002.xml", fileDownloadMetadata.getPartiallyDownloadedPath().toAbsolutePath().toString());
		assertEquals("C:\\test\\download\\path\\s1a-ew2-slc-vv-20120101t042031-20120101t042037-001771-000001-002.xml", fileDownloadMetadata.getCompletelyDownloadedPath().toAbsolutePath().toString());
	}
	
	@Test
	public void testPathsWithFolder() throws MalformedURLException {
		URL fileURL = new URL("http://www.test.url/S1A_EW_SLC__1ASV_20120101T042030_20120101T042039_001771_000001_7637.SAFE/annotation/s1a-ew2-slc-vv-20120101t042031-20120101t042037-001771-000001-002.xml");
		String fileName = "S1A_EW_SLC__1ASV_20120101T042030_20120101T042039_001771_000001_7637.SAFE/annotation/s1a-ew2-slc-vv-20120101t042031-20120101t042037-001771-000001-002.xml";
		Path downloadPath = Paths.get("C:\\test\\download\\path");
		long downloadSize = 4096L;
		FileDownloadMetadata fileDownloadMetadata = new FileDownloadMetadata(fileURL, fileName, downloadSize, downloadPath);

		assertEquals("C:\\test\\download\\path\\S1A_EW_SLC__1ASV_20120101T042030_20120101T042039_001771_000001_7637.SAFE\\annotation\\.s1a-ew2-slc-vv-20120101t042031-20120101t042037-001771-000001-002.xml", fileDownloadMetadata.getPartiallyDownloadedPath().toAbsolutePath().toString());
		assertEquals("C:\\test\\download\\path\\S1A_EW_SLC__1ASV_20120101T042030_20120101T042039_001771_000001_7637.SAFE\\annotation\\s1a-ew2-slc-vv-20120101t042031-20120101t042037-001771-000001-002.xml", fileDownloadMetadata.getCompletelyDownloadedPath().toAbsolutePath().toString());
	}
}
