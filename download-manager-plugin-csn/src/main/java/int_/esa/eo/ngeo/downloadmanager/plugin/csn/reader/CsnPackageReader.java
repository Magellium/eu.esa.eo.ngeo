package int_.esa.eo.ngeo.downloadmanager.plugin.csn.reader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.io.IOUtils;

public abstract class CsnPackageReader {
    ArchiveInputStream packageReaderInputStream;
    InputStream md5ChecksumInputStream;
    
    public abstract void createInputStreams(Path filePath) throws IOException;
    
    public List<String> readPackageEntries() throws IOException {
        List<String> entryList = new ArrayList<>();

        ArchiveEntry entry;
        while (null != (entry = packageReaderInputStream.getNextEntry())) {
            if(!entry.isDirectory()) {
                String entryName = entry.getName();
                /* 
                 * entryName may contain a folder in its path - we do not need to send this to the CSN DC service.
                 * e.g. Inventory_1397557679.7047/133654_RS2_20140414_162717_0076_SCWA_HH_SGF_320223_9019_9563180_PV.tif
                 * should be sent as 133654_RS2_20140414_162717_0076_SCWA_HH_SGF_320223_9019_9563180_PV.tif
                 */
                String[] entryNameArray = entryName.split("[/\\\\]");
                
                entryList.add(entryNameArray[entryNameArray.length - 1]);
            }
        }
        
        return entryList;
    }
    
    public String getMd5Checksum() throws IOException {
        return DigestUtils.md5Hex(IOUtils.toByteArray(md5ChecksumInputStream));
    }
    
    public void closePackageInputStreams() {
        IOUtils.closeQuietly(packageReaderInputStream);
        IOUtils.closeQuietly(md5ChecksumInputStream);
    }
}
