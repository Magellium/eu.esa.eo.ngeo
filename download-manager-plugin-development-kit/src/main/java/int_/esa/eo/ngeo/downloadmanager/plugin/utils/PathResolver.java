package int_.esa.eo.ngeo.downloadmanager.plugin.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathResolver {
	private static final int DISPOSITION_SUBSTRING_START_OFFSET = 10;
	private static final Logger LOGGER = LoggerFactory.getLogger(PathResolver.class);

	public Path determineFolderPath(URI productURI, File downloadDir) {
		String productUriPath = productURI.getPath();
		String fileName = FilenameUtils.getName(productUriPath);
		Map<String, String> queryMap = getQueryMap(productURI);
		String ngeoDownloadOptions = queryMap.get("ngEO_DO");
		ngeoDownloadOptions = ngeoDownloadOptions.replaceAll(":", "=");
		
		StringBuilder folderName = new StringBuilder();
		folderName.append(fileName);
		if(ngeoDownloadOptions != null && ngeoDownloadOptions.length() > 0) {
			folderName.append(" ");
			int charactersAvailableForFolderName = 200 - (fileName.length() + 1);
			if(ngeoDownloadOptions.length() > charactersAvailableForFolderName) {
				folderName.append(ngeoDownloadOptions.substring(0, charactersAvailableForFolderName - 1));
			}else{
				folderName.append(ngeoDownloadOptions);
			}
		}
		
		return resolveDuplicateFolderPath(folderName.toString(), downloadDir);
	}

	private Path resolveDuplicateFolderPath(String folderName, File downloadDir) {
		for(int i=0;i<=100;i++) {
			Path completedFolderPath, tempFolderPath;
			if(i == 0) {
				completedFolderPath = Paths.get(downloadDir.getAbsolutePath(), folderName);
				tempFolderPath = Paths.get(downloadDir.getAbsolutePath(), String.format(".%s", folderName));
			}else{
				String duplicateFolderName = String.format("%s (%s)", folderName, i);
				completedFolderPath = Paths.get(downloadDir.getAbsolutePath(), duplicateFolderName);
				tempFolderPath = Paths.get(downloadDir.getAbsolutePath(), String.format(".%s", duplicateFolderName));
			}
			if(!Files.exists(completedFolderPath) && !Files.exists(tempFolderPath)) {
				return completedFolderPath;
			}
		}
		return null;
	}

	//XXX: This should be replaced with something similar to URLEncodedUtils in Apache HTTP Components
	private Map<String,String> getQueryMap(URI uri) {
		String decodedQueryString = "";
		try {
			decodedQueryString = URLDecoder.decode(uri.getQuery(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String[] params = decodedQueryString.split("&");  
	    Map<String, String> map = new HashMap<String, String>();  
	    for (String param : params)  
	    {  
	        String[] paramPair = param.split("=");
			String name = paramPair[0];
			String value = "";
			if(paramPair.length > 1) {
				value = paramPair[1];  
			}
	        map.put(name, value);  
	    }  
	    return map;  
	}
	
	public String determineFileName(String disposition, URI actualDownloadUri, File productDownloadDir) {
		String fileName = null;
		if (disposition != null && disposition.indexOf("filename=") > 0) {
			// Extract file name from header field
			int index = disposition.indexOf("filename=");
			if (index > 0) {
				fileName = disposition.substring(index + DISPOSITION_SUBSTRING_START_OFFSET,disposition.length() - 1);
			}
		} else {
			fileName = FilenameUtils.getName(actualDownloadUri.getPath());
			if (fileName.length() == 0) {
				fileName = UUID.randomUUID().toString();
				LOGGER.warn(String.format("Resorting to use of UUID %s as a file name, to avoid zero-length name.", fileName));
			}
		}
		return resolveDuplicateFilePath(fileName, productDownloadDir);
	}
	
	public String resolveDuplicateFilePath(String fileName, File downloadDir) {
		for(int i=0;i<=100;i++) {
			Path completedFolderPath, tempFolderPath;
			String fileNameToUseForDownload = fileName;
			if(i == 0) {
				completedFolderPath = Paths.get(downloadDir.getAbsolutePath(), fileName);
				tempFolderPath = Paths.get(downloadDir.getAbsolutePath(), String.format(".%s", fileName));
			}else{
				fileNameToUseForDownload = String.format("%s (%s)", fileName, i);
				completedFolderPath = Paths.get(downloadDir.getAbsolutePath(), fileNameToUseForDownload);
				tempFolderPath = Paths.get(downloadDir.getAbsolutePath(), String.format(".%s", fileNameToUseForDownload));
			}
			if(!Files.exists(completedFolderPath) && !Files.exists(tempFolderPath)) {
				return fileNameToUseForDownload;
			}
		}
		return null;
	}
}
