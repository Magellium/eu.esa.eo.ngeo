package int_.esa.eo.ngeo.downloadmanager.plugin.model;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileDownloadMetadata {
	private final String uuid;
	private final URL fileURL;
	private final String fileName;
	private final long downloadSize;
	private final Path downloadPath;

	public FileDownloadMetadata(URL fileURL, String fileName, long downloadSize, Path downloadPath) {
		this.uuid = UUID.randomUUID().toString();
		this.fileURL = fileURL;
		this.fileName = fileName;
		this.downloadSize = downloadSize;
		this.downloadPath = downloadPath;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public URL getFileURL() {
		return fileURL;
	}

	public long getDownloadSize() {
		return downloadSize;
	}

	public Path getDownloadPath() {
		return downloadPath;
	}

	public Path getPartiallyDownloadedPath() {
		 return Paths.get(downloadPath.toAbsolutePath().toString(), String.format(".%s", fileName));
	}

	public Path getCompletelyDownloadedPath() {
		 return Paths.get(downloadPath.toAbsolutePath().toString(), fileName);
	}

	public String getFileName() {
		return fileName;
	}
}
