package int_.esa.eo.ngeo.downloadmanager.plugin;

import java.io.File;
import java.net.URL;

public class FileDetails implements DownloadProgressListener {
	private final URL fileURL;
	private final String fileName;
	private final long downloadSize;
	private final File downloadPath;
	private long downloadedSize;
	private boolean downloadComplete;
	
	public FileDetails(URL fileURL, String fileName, long downloadSize, File downloadPath) {
		this.fileURL = fileURL;
		this.fileName = fileName;
		this.downloadSize = downloadSize;
		this.downloadPath = downloadPath;
		this.downloadedSize = 0;
		this.downloadComplete = false;
	}

	public URL getFileURL() {
		return fileURL;
	}

	public long getDownloadSize() {
		return downloadSize;
	}

	public File getDownloadPath() {
		return downloadPath;
	}

	public String getFileName() {
		return fileName;
	}

	public long getDownloadedSize() {
		return downloadedSize;
	}

	public void setDownloadedSize(long downloadedSize) {
		this.downloadedSize = downloadedSize;
	}

	public void incrementDownloadedSize(long bytesRead) {
		this.downloadedSize += bytesRead;
	}

	/**
	 * Have all the bytes of the remote resource been written to the local file system and the local file been given its final name?
	 */
	public boolean isDownloadComplete() {
		return downloadComplete;
	}

	public void setDownloadComplete(boolean downloadComplete) {
		this.downloadComplete = downloadComplete;
	}

	@Override
	public void notifySomeBytesTransferred(long numberOfBytes) {
		incrementDownloadedSize(numberOfBytes);
	}
}
