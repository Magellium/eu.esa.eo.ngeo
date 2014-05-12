package int_.esa.eo.ngeo.downloadmanager.plugin.csn.builder;

import java.util.List;

import localhost.csn.Package;
import localhost.csn.TransmitPackage;
import localhost.csn.TransmitRequest;
import localhost.csn.VectorOfStrings;

public class CsnRequestBuilder {
    public TransmitPackage buildTransmitPackage(String filename, String md5, int orderId) {
        return buildTransmitPackage(filename, md5, orderId, null);
    }

    public TransmitPackage buildTransmitPackage(String filename, String md5, int orderId, List<String> packageList) {
        TransmitPackage transmitPackage = new TransmitPackage();
        
        TransmitRequest transmitRequest = new TransmitRequest();
        
        Package packageData = new Package();
        packageData.setFilename(filename);
        packageData.setMD5(md5);
        packageData.setOrderID(orderId);
        if(packageList != null && !packageList.isEmpty()) {
            packageData.setPackageList(new VectorOfStrings());
            
            for (String packageName : packageList) {
                packageData.getPackageList().getPackageName().add(packageName);
            }
        }
        
        transmitRequest.setPackageData(packageData);
        
        transmitPackage.setInputPackage(transmitRequest);
        
        return transmitPackage;
    }
}
