package int_.esa.eo.ngeo.downloadmanager.plugin.csn.service;

import int_.esa.eo.ngeo.downloadmanager.plugin.csn.exception.CsnServiceException;

import java.net.URL;

import localhost.csn.TransmitPackage;
import localhost.csn.TransmitResponse;

public interface CsnServiceInterface {
    TransmitResponse validateCSNPackage(URL serviceUrl, TransmitPackage transmitPackage) throws CsnServiceException;
}
