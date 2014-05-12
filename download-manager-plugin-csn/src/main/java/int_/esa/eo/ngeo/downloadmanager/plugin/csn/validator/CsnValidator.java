package int_.esa.eo.ngeo.downloadmanager.plugin.csn.validator;

import int_.esa.eo.ngeo.downloadmanager.plugin.csn.builder.CsnRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.exception.CsnException;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.exception.InvalidPackageException;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.reader.CsnPackageReader;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.reader.CsnPackageReaderFactory;
import int_.esa.eo.ngeo.downloadmanager.plugin.csn.service.CsnService;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import localhost.csn.TransmitPackage;
import localhost.csn.TransmitResponse;

public class CsnValidator {
    private CsnPackageReaderFactory csnPackageReaderFactory;
    private CsnRequestBuilder csnRequestBuilder;
    private CsnService csnService;
    
    public CsnValidator(CsnPackageReaderFactory csnPackageReaderFactory, CsnRequestBuilder csnRequestBuilder, CsnService csnService) {
        this.csnPackageReaderFactory = csnPackageReaderFactory;
        this.csnRequestBuilder = csnRequestBuilder;
        this.csnService = csnService;
    }
    
    public boolean isCsnPackageValid(Path packagePath, int orderId, URL wsdlUrl) throws CsnException {
        //determine md5 and package list of CSN package
        CsnPackageReader csnPackageReader = csnPackageReaderFactory.getCsnPackageReader(packagePath);

        String md5Checksum;
        List<String> packageList;
        
        try {
            csnPackageReader.createInputStreams(packagePath);

            md5Checksum = csnPackageReader.getMd5Checksum();
            packageList = csnPackageReader.readPackageEntries();
        }catch(IOException ex) {
            throw new CsnException("Unable to read package: ", ex);
        }finally{
            csnPackageReader.closePackageInputStreams();
        }
        
        //build csn request
        String packageFileName = packagePath.getFileName().toString();
        TransmitPackage transmitPackage = csnRequestBuilder.buildTransmitPackage(packageFileName, md5Checksum, orderId, packageList);
        
        //send csn service validation request
        TransmitResponse transmitResponse = csnService.validateCSNPackage(wsdlUrl, transmitPackage);
        
        //determine if the package is valid from service response
        if("ACK".equals(transmitResponse.getResponse())) {
            return true;
        }else{
            throw new InvalidPackageException(String.format("CSN Data Centre response: %s", transmitResponse.getResponse()));
        }
    }
}
