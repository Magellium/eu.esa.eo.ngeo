package int_.esa.eo.ngeo.downloadmanager.plugin.csn.reader;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

public class CsnPackageTgzReader extends CsnPackageReader {
    @Override
    public void createInputStreams(Path filePath) throws IOException {
        packageReaderInputStream = new TarArchiveInputStream(
            new GzipCompressorInputStream(
                new BufferedInputStream(
                    new FileInputStream(
                        filePath.toFile()
                    )
                )
            )
        );
        md5ChecksumInputStream = new FileInputStream(filePath.toFile());
    }
}
