package int_.esa.eo.ngeo.downloadmanager.plugin.csn.reader;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

public class CsnPackageZipReader extends CsnPackageReader {
    @Override
    public void createInputStreams(Path filePath) throws IOException {
        packageReaderInputStream = new ZipArchiveInputStream(new FileInputStream(filePath.toFile()));
        md5ChecksumInputStream = new FileInputStream(filePath.toFile());
    }
}
