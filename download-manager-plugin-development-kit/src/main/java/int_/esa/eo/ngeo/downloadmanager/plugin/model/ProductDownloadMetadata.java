package int_.esa.eo.ngeo.downloadmanager.plugin.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProductDownloadMetadata {
    private List<FileDownloadMetadata> fileMetadataList;
    private Path metalinkDownloadDirectory;

    public ProductDownloadMetadata() {
        fileMetadataList = new ArrayList<>();
    }

    public List<FileDownloadMetadata> getFileMetadataList() {
        return fileMetadataList;
    }

    public void setFileMetadataList(List<FileDownloadMetadata> fileMetadataList) {
        this.fileMetadataList = fileMetadataList;
    }

    public Path getMetalinkDownloadDirectory() {
        return metalinkDownloadDirectory;
    }

    public void setMetalinkDownloadDirectory(Path metalinkDownloadDirectory) {
        this.metalinkDownloadDirectory = metalinkDownloadDirectory;
    }

    public Path getTempMetalinkDownloadDirectory() {
        return Paths.get(metalinkDownloadDirectory.getParent().toAbsolutePath().toString(), String.format(".%s", metalinkDownloadDirectory.getFileName()));
    }
}
