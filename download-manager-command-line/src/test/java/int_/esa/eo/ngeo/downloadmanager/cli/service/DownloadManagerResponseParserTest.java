package int_.esa.eo.ngeo.downloadmanager.cli.service;

import static org.junit.Assert.assertEquals;
import int_.esa.eo.ngeo.downloadmanager.transform.JSONTransformer;

import org.junit.Test;

public class DownloadManagerResponseParserTest {
    private DownloadManagerResponseParser downloadManagerResponseParser = new DownloadManagerResponseParser();

    @Test
    public void getJsonTransformerTest() {
        JSONTransformer jsonTransformer = downloadManagerResponseParser.getJsonTransformer();
        assertEquals(jsonTransformer, downloadManagerResponseParser.getJsonTransformer());
    }
}
