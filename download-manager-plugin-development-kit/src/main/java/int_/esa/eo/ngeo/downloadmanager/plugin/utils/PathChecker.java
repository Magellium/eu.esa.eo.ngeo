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

import org.apache.commons.io.FilenameUtils;

public class PathChecker {

	public String determineFolderName(URI productURI, File downloadDir) {
		String productUriAsString = productURI.toString();
		String fileName = FilenameUtils.getName(productUriAsString);
		Map<String, String> queryMap = getQueryMap(productURI);
		String ngeoDownloadOptions = queryMap.get("ngEO_DO");
		
		StringBuilder folderName = new StringBuilder();
		folderName.append(fileName);
		if(ngeoDownloadOptions != null && ngeoDownloadOptions.length() > 0) {
			folderName.append(" (");
			int charactersAvailableForFolderName = 200 - (fileName.length() + 3);
			if(ngeoDownloadOptions.length() > charactersAvailableForFolderName) {
				folderName.append(ngeoDownloadOptions.substring(0, charactersAvailableForFolderName - 1));
			}else{
				folderName.append(ngeoDownloadOptions);
			}
			folderName.append(")");
		}
		
		return resolveDuplicatePathForFolder(folderName.toString(), downloadDir);
	}

	public String resolveDuplicatePathForFolder(String folderName, File downloadDir) {
		Path folderPath = Paths.get(downloadDir.toString(), folderName);
		if(Files.exists(folderPath)) {
			
		}
		
		return "";
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
}
