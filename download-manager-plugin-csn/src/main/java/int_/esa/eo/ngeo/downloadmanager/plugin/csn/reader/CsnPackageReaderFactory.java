package int_.esa.eo.ngeo.downloadmanager.plugin.csn.reader;

import int_.esa.eo.ngeo.downloadmanager.plugin.csn.exception.CsnException;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.tika.Tika;

public class CsnPackageReaderFactory {

    public CsnPackageReader getCsnPackageReader(Path packagePath) throws CsnException {
        Tika tika = new Tika();
        String mimeType;
        try {
            mimeType = tika.detect(packagePath.toFile());
        } catch (IOException e) {
            throw new CsnException(String.format("Unable to detect mimetype of package, %s", e.getLocalizedMessage()), e);
        }

        CsnPackageFormat csnPackageFormat = CsnPackageFormat.getEnumFromMimeType(mimeType);
        return csnPackageFormat.getPackageReader();
    }
}
