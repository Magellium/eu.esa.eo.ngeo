package int_.esa.eo.ngeo.downloadmanager.plugin.csn.reader;

import static org.junit.Assert.assertEquals;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.exception.CsnException;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.exception.InvalidPackageFormatException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ReadCSNPackageTest {
    private CsnPackageReaderFactory csnPackageReaderFactory;
    
    @Before
    public void setup() {
        csnPackageReaderFactory = new CsnPackageReaderFactory();
    }
    
    @Test
    public void readTGZFileTest() throws CsnException, URISyntaxException, IOException {
        URL resourceAsStream = this.getClass().getResource("Inventory_1397557679.7047.tgz");
        Path packagePath = Paths.get(resourceAsStream.toURI());

        CsnPackageReader csnPackageReader = csnPackageReaderFactory.getCsnPackageReader(packagePath);
        csnPackageReader.createInputStreams(packagePath);

        List<String> csnPackageEntries = csnPackageReader.readPackageEntries();

        assertEquals(5, csnPackageEntries.size());
        assertEquals("Inventory_1397557679.7047/133654_RS2_20140414_162717_0076_SCWA_HH_SGF_320223_9019_9563180_OSN.zip", csnPackageEntries.get(0));
        assertEquals("Inventory_1397557679.7047/copernicus.jpg", csnPackageEntries.get(1));
        assertEquals("Inventory_1397557679.7047/img_colorlogo_negative.gif", csnPackageEntries.get(2));
        assertEquals("Inventory_1397557679.7047/Sentinel1_images/Brussels_from_Sentinel-1A_node_full_image.jpg", csnPackageEntries.get(3));
        assertEquals("Inventory_1397557679.7047/Sentinel1_images/Sentinel-1_radar_modes_node_full_image.jpg", csnPackageEntries.get(4));

        assertEquals("40fcb5d6622f8305fee653e4b16563a9", csnPackageReader.getMd5Checksum());
        csnPackageReader.closePackageInputStreams();
    }

    @Test
    public void readTARFileTest() throws IOException, URISyntaxException, CsnException {
        URL resourceAsStream = this.getClass().getResource("Inventory_1397557679.7047.tar");
        Path packagePath = Paths.get(resourceAsStream.toURI());

        CsnPackageReader csnPackageReader = csnPackageReaderFactory.getCsnPackageReader(packagePath);
        csnPackageReader.createInputStreams(packagePath);

        List<String> csnPackageEntries = csnPackageReader.readPackageEntries();

        assertEquals(3, csnPackageEntries.size());
        assertEquals("Inventory_1397557679.7047/133654_RS2_20140414_162717_0076_SCWA_HH_SGF_320223_9019_9563180_QNO.zip", csnPackageEntries.get(0));
        assertEquals("Inventory_1397557679.7047/img_colorlogo_darkblue.gif", csnPackageEntries.get(1));
        assertEquals("Inventory_1397557679.7047/Unpacking_Sentinel-3A_radiometer_medium.jpg", csnPackageEntries.get(2));

        assertEquals("f11d998808ad6bfbe330ecf3671b0c9f", csnPackageReader.getMd5Checksum());
        csnPackageReader.closePackageInputStreams();
    }

    @Test
    public void readZIPFileTest() throws IOException, URISyntaxException, CsnException {
        URL resourceAsStream = this.getClass().getResource("133654_RS2_20140414_162717_0076_SCWA_HH_SGF_320223_9019_9563180_QUA.zip");
        Path packagePath = Paths.get(resourceAsStream.toURI());

        CsnPackageReader csnPackageReader = csnPackageReaderFactory.getCsnPackageReader(packagePath);
        csnPackageReader.createInputStreams(packagePath);

        List<String> csnPackageEntries = csnPackageReader.readPackageEntries();

        assertEquals(2, csnPackageEntries.size());
        assertEquals("133654_RS2_20140414_162717_0076_SCWA_HH_SGF_320223_9019_9563180_PCK.xml", csnPackageEntries.get(0));
        assertEquals("133654_RS2_20140414_162717_0076_SCWA_HH_SGF_320223_9019_9563180_QR.xml", csnPackageEntries.get(1));
        
        assertEquals("4a3953825280471c98d11e839ebbbf26", csnPackageReader.getMd5Checksum());
        csnPackageReader.closePackageInputStreams();
    }
    
    @Test
    public void readInvalidPackageTest() throws URISyntaxException, CsnException {
        URL resourceAsStream = this.getClass().getResource("invalidPackageFormatTest.txt");
        Path packagePath = Paths.get(resourceAsStream.toURI());

        try {
            csnPackageReaderFactory.getCsnPackageReader(packagePath);
        } catch (InvalidPackageFormatException e) {
            assertEquals("Package with mimetype text/plain is not supported.", e.getLocalizedMessage());
        }
        
    }

    @Test
    public void readMissingPackageTest() throws URISyntaxException {
        Path packagePath = Paths.get("missingPackageTest.zip");

        try {
            csnPackageReaderFactory.getCsnPackageReader(packagePath);
        } catch (CsnException e) {
            assertEquals(String.format("Unable to detect mimetype of package, %s (The system cannot find the file specified)", packagePath.toAbsolutePath().toString()), e.getLocalizedMessage());
        }
        
    }
}
