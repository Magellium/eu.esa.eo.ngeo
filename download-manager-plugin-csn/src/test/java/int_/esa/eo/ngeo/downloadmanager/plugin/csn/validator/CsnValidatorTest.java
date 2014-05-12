package int_.esa.eo.ngeo.downloadmanager.plugin.csn.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.builder.CsnRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.exception.CsnException;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.exception.InvalidPackageException;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.reader.CsnPackageReader;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.reader.CsnPackageReaderFactory;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.service.CsnService;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import localhost.csn.Package;
import localhost.csn.TransmitPackage;
import localhost.csn.TransmitRequest;
import localhost.csn.TransmitResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CsnValidatorTest {
    private CsnValidator csnValidator;
    private CsnPackageReaderFactory csnPackageReaderFactory;
    private CsnRequestBuilder csnRequestBuilder;
    private CsnService csnService;
    private CsnPackageReader csnPackageReader;
    
    @Before
    public void setup() {
        csnPackageReaderFactory = mock(CsnPackageReaderFactory.class);
        csnRequestBuilder = mock(CsnRequestBuilder.class);
        csnService = mock(CsnService.class);
        csnPackageReader = mock(CsnPackageReader.class);
        
        csnValidator = new CsnValidator(csnPackageReaderFactory, csnRequestBuilder, csnService);
    }
    
    @Test
    public void validCsnPackageTest() throws CsnException, IOException {
        URL wsdlUrl = new URL("http://localhost:8088/mockEmsa?WSDL");
        Path packagePath = Paths.get("123_ASA_WSM_1PNACS20100603_203524_000000592090_00043_43183_0001.N1.00114_EMSA_EOP.tgz");
        int orderId = 12345;
        String md5checksum = "d41d8cd98f00b204e9800998ecf8427e";
        List<String> packageEntries = new ArrayList<>();
        
        when(csnPackageReaderFactory.getCsnPackageReader(packagePath)).thenReturn(csnPackageReader);
        when(csnPackageReader.getMd5Checksum()).thenReturn(md5checksum);
        when(csnPackageReader.readPackageEntries()).thenReturn(packageEntries);
        Mockito.doNothing().when(csnPackageReader).closePackageInputStreams();

 
        TransmitPackage transmitPackage = new TransmitPackage();
        TransmitRequest transmitRequest = new TransmitRequest();
        Package packageData = new Package();
        packageData.setFilename(packagePath.toString());
        packageData.setMD5(md5checksum);
        packageData.setOrderID(orderId);
        transmitRequest.setPackageData(packageData);
        transmitPackage.setInputPackage(transmitRequest);        

        when(csnRequestBuilder.buildTransmitPackage(packagePath.toString(), md5checksum, orderId, packageEntries)).thenReturn(transmitPackage);
        
        TransmitResponse transmitResponse = new TransmitResponse();
        transmitResponse.setResponse("ACK");

        when(csnService.validateCSNPackage(wsdlUrl, transmitPackage)).thenReturn(transmitResponse);
        
        assertTrue(csnValidator.isCsnPackageValid(packagePath, orderId, wsdlUrl));
    }

    @Test
    public void invalidCsnPackageTest() throws IOException, CsnException {
        URL wsdlUrl = new URL("http://localhost:8088/mockEmsa?WSDL");
        Path packagePath = Paths.get("123_ASA_WSM_1PNACS20100603_203524_000000592090_00043_43183_0001.N1.00114_EMSA_EOP.tgz");
        int orderId = 12345;
        String md5checksum = "d41d8cd98f00b204e9800998ecf99999";
        List<String> packageEntries = new ArrayList<>();
        String responseMessage = "Error: Checksum does not match with package.";
        
        when(csnPackageReaderFactory.getCsnPackageReader(packagePath)).thenReturn(csnPackageReader);
        when(csnPackageReader.getMd5Checksum()).thenReturn(md5checksum);
        when(csnPackageReader.readPackageEntries()).thenReturn(packageEntries);
        Mockito.doNothing().when(csnPackageReader).closePackageInputStreams();

 
        TransmitPackage transmitPackage = new TransmitPackage();
        TransmitRequest transmitRequest = new TransmitRequest();
        Package packageData = new Package();
        packageData.setFilename(packagePath.toString());
        packageData.setMD5(md5checksum);
        packageData.setOrderID(orderId);
        transmitRequest.setPackageData(packageData);
        transmitPackage.setInputPackage(transmitRequest);        

        when(csnRequestBuilder.buildTransmitPackage(packagePath.toString(), md5checksum, orderId, packageEntries)).thenReturn(transmitPackage);
        
        TransmitResponse transmitResponse = new TransmitResponse();
        transmitResponse.setResponse(responseMessage);

        when(csnService.validateCSNPackage(wsdlUrl, transmitPackage)).thenReturn(transmitResponse);
        
        try {
            csnValidator.isCsnPackageValid(packagePath, orderId, wsdlUrl);
        }catch(InvalidPackageException ex) {
            assertEquals(String.format("CSN Data Centre response: %s", responseMessage), ex.getMessage());
        }
    }
}
