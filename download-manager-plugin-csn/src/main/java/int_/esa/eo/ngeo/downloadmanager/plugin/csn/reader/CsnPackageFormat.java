package int_.esa.eo.ngeo.downloadmanager.plugin.csn.reader;

import int_.esa.eo.ngeo.downloadmanager.plugin.csn.exception.InvalidPackageFormatException;


public enum CsnPackageFormat {
    ZIP("application/zip", new CsnPackageZipReader()),
    TAR("application/x-gtar", new CsnPackageTarReader()),
    TGZ("application/x-gzip", new CsnPackageTgzReader());
    
    private final String mimeType;
    private final CsnPackageReader packageReader;
    
    private CsnPackageFormat(String mimeTypeString, CsnPackageReader packageReader) {
        this.mimeType = mimeTypeString;
        this.packageReader = packageReader;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public CsnPackageReader getPackageReader() {
        return packageReader;
    }
    
    public static CsnPackageFormat getEnumFromMimeType(String mimeType) throws InvalidPackageFormatException {
        for(CsnPackageFormat csnPackageFormat : values())
            if(csnPackageFormat.getMimeType().equalsIgnoreCase(mimeType)) {
                return csnPackageFormat;
            }
        throw new InvalidPackageFormatException(String.format("Package with mimetype %s is not supported.", mimeType));
    }
}
