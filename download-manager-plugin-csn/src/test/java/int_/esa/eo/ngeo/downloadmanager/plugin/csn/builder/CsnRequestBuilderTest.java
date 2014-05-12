package int_.esa.eo.ngeo.downloadmanager.plugin.csn.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.builder.CsnRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import localhost.csn.TransmitPackage;

import org.junit.Before;
import org.junit.Test;

public class CsnRequestBuilderTest {
    private CsnRequestBuilder csnRequestBuilder;
    
    @Before
    public void setup() {
        csnRequestBuilder = new CsnRequestBuilder();
    }
    
    @Test
    public void buildTransmitPackageTest() {
        String filename = "123_ASA_WSM_1PNACS20100603_203524_000000592090_00043_43183_0001.N1.00114_EMSA_EOP.tgz";
        String md5 = "d41d8cd98f00b204e9800998ecf8427e";
        int orderId = 12345;
        
        TransmitPackage transmitPackage = csnRequestBuilder.buildTransmitPackage(filename, md5, orderId);
        
        assertEquals(filename, transmitPackage.getInputPackage().getPackageData().getFilename());
        assertEquals(md5, transmitPackage.getInputPackage().getPackageData().getMD5());
        assertEquals(orderId, transmitPackage.getInputPackage().getPackageData().getOrderID());
        assertNull(transmitPackage.getInputPackage().getPackageData().getPackageList());
    }
    
    @Test
    public void buildTransmitPackageWithPackageListTest() {
        String filename = "123_ASA_WSM_1PNACS20100603_203524_000000592090_00043_43183_0001.N1.00114_EMSA_EOP.tgz";
        String md5 = "d41d8cd98f00b204e9800998ecf8427e";
        int orderId = 12345;
        List<String> packageNames = new ArrayList<>();
        packageNames.add("filename1.txt");
        packageNames.add("filename2.txt");
        packageNames.add("filename3.txt");
        packageNames.add("filename4.txt");
        
        TransmitPackage transmitPackage = csnRequestBuilder.buildTransmitPackage(filename, md5, orderId, packageNames);
        
        assertEquals(filename, transmitPackage.getInputPackage().getPackageData().getFilename());
        assertEquals(md5, transmitPackage.getInputPackage().getPackageData().getMD5());
        assertEquals(orderId, transmitPackage.getInputPackage().getPackageData().getOrderID());
        assertNotNull(transmitPackage.getInputPackage().getPackageData().getPackageList());
        assertEquals(4, transmitPackage.getInputPackage().getPackageData().getPackageList().getPackageName().size());
        assertEquals("filename1.txt", transmitPackage.getInputPackage().getPackageData().getPackageList().getPackageName().get(0));
        assertEquals("filename2.txt", transmitPackage.getInputPackage().getPackageData().getPackageList().getPackageName().get(1));
        assertEquals("filename3.txt", transmitPackage.getInputPackage().getPackageData().getPackageList().getPackageName().get(2));
        assertEquals("filename4.txt", transmitPackage.getInputPackage().getPackageData().getPackageList().getPackageName().get(3));
    }

    @Test
    public void buildTransmitPackageWithEmptyPackageListTest() {
        String filename = "123_ASA_WSM_1PNACS20100603_203524_000000592090_00043_43183_0001.N1.00114_EMSA_EOP.tgz";
        String md5 = "d41d8cd98f00b204e9800998ecf8427e";
        int orderId = 12345;
        List<String> packageNames = new ArrayList<>();
        
        TransmitPackage transmitPackage = csnRequestBuilder.buildTransmitPackage(filename, md5, orderId, packageNames);
        
        assertEquals(filename, transmitPackage.getInputPackage().getPackageData().getFilename());
        assertEquals(md5, transmitPackage.getInputPackage().getPackageData().getMD5());
        assertEquals(orderId, transmitPackage.getInputPackage().getPackageData().getOrderID());
        assertNull(transmitPackage.getInputPackage().getPackageData().getPackageList());
    }
}
