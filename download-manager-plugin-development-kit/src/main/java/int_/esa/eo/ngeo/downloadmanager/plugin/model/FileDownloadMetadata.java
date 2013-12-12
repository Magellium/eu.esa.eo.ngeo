package int_.esa.eo.ngeo.downloadmanager.plugin.model;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public Path getPartiallyDownloadedPath() {
		 return getPathFromFileName(fileName, true);
	}

	public Path getCompletelyDownloadedPath() {
		 return getPathFromFileName(fileName, false);
	}

	/* 
	 * The filename may contain a folder in a metalink scenario. The file at the end of the path is be extracted
	 * from the filename
	 * If the file is partially downloaded (i.e. in progress) a dot will be appended to the filename.
	 */
	private Path getPathFromFileName(String fileName, boolean isPartiallyDownloaded) {
		Pattern potentialFilePathPattern = Pattern.compile("(.*[\\\\/])([^\\\\/]*)");
		Matcher matcher = potentialFilePathPattern.matcher(fileName);
		if(matcher.find()) {
			Path pathFromFileName = Paths.get(downloadPath.toAbsolutePath().toString());
			for(int i=1; i <= matcher.groupCount(); i++) {
				if(i == matcher.groupCount() && isPartiallyDownloaded) {
					pathFromFileName = Paths.get(pathFromFileName.toString(), String.format(".%s", matcher.group(i)));
				}else{
					pathFromFileName = Paths.get(pathFromFileName.toString(), matcher.group(i));
				}
			}
			return pathFromFileName;
		}else{
			if(isPartiallyDownloaded) {
				return Paths.get(downloadPath.toAbsolutePath().toString(), String.format(".%s", fileName));
			}else{
				 return Paths.get(downloadPath.toAbsolutePath().toString(), fileName);
			}
		}
	}
	
	public String getFileName() {
		return fileName;
	}
}
