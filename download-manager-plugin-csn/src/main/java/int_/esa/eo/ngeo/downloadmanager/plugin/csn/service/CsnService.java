package int_.esa.eo.ngeo.downloadmanager.plugin.csn.service;

import int_.esa.eo.ngeo.downloadmanager.plugin.csn.exception.CsnServiceException;

import java.net.URL;

import localhost.csn.TransmitPackage;
import localhost.csn.TransmitResponse;
import localhost.emsa.emsa_wsdl.Emsa;
import localhost.emsa.emsa_wsdl.EmsaPortType;

public class CsnService implements CsnServiceInterface {
    @Override
    public TransmitResponse validateCSNPackage(URL wsdlUrl, TransmitPackage transmitPackage) throws CsnServiceException {
        Emsa emsa = new Emsa(wsdlUrl);
        EmsaPortType emsaPortType = emsa.getEmsa();
        
        return emsaPortType.transmitPackage(transmitPackage);
    }
}