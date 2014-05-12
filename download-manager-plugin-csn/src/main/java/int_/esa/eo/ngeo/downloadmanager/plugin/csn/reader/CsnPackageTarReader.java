package int_.esa.eo.ngeo.downloadmanager.plugin.csn.reader;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

public class CsnPackageTarReader extends CsnPackageReader {
    @Override
    public void createInputStreams(Path filePath) throws IOException {
        packageReaderInputStream = new TarArchiveInputStream(new FileInputStream(filePath.toFile()));
        md5ChecksumInputStream = new FileInputStream(filePath.toFile());
    }
}
