package int_.esa.eo.ngeo.downloadmanager.plugin.utils;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathResolver.class);

    /**
     *
     * @param productURI
     * @param downloadDir
     * @return
     */
    public Path determineFolderPath(URI productURI, File downloadDir) {
        String productUriPath = productURI.getPath();
        String fileName = FilenameUtils.getName(productUriPath);
        StringBuilder folderName = new StringBuilder();
        folderName.append(fileName);

//        Map<String, String> queryMap = getQueryMap(productURI);
        String ngeoDownloadOptions = null;
        for (NameValuePair param : URLEncodedUtils.parse(productURI, "UTF-8")) {
            if (param.getName().equalsIgnoreCase("ngEO_DO")) {
                ngeoDownloadOptions = param.getValue();
                break;
            }
        }

        if (ngeoDownloadOptions != null && !ngeoDownloadOptions.isEmpty()) {
            ngeoDownloadOptions = ngeoDownloadOptions.replaceAll(":", "=").replaceAll("\\{", "").replaceAll("}", "");
            folderName.append("(");
            int charactersAvailableForFolderName = 200 - (fileName.length() + 2);
            if (ngeoDownloadOptions.length() > charactersAvailableForFolderName) {
                folderName.append(ngeoDownloadOptions.substring(0, charactersAvailableForFolderName - 1));
            } else {
                folderName.append(ngeoDownloadOptions);
            }
            folderName.append(")");
        }

        return resolveDuplicateFolderPath(folderName.toString(), downloadDir);
    }

    private Path resolveDuplicateFolderPath(String folderName, File downloadDir) {
        for (int i = 0; i <= 100; i++) {
            Path completedFolderPath, tempFolderPath;
            if (i == 0) {
                completedFolderPath = Paths.get(downloadDir.getAbsolutePath(), folderName);
                tempFolderPath = Paths.get(downloadDir.getAbsolutePath(), String.format(".%s", folderName));
            } else {
                String duplicateFolderName = String.format("%s (%s)", folderName, i);
                completedFolderPath = Paths.get(downloadDir.getAbsolutePath(), duplicateFolderName);
                tempFolderPath = Paths.get(downloadDir.getAbsolutePath(), String.format(".%s", duplicateFolderName));
            }
            if (!Files.exists(completedFolderPath) && !Files.exists(tempFolderPath)) {
                return completedFolderPath;
            }
        }
        return null;
    }

    //XXX: This should be replaced with something similar to URLEncodedUtils in Apache HTTP Components
//    private Map<String, String> getQueryMap(URI uri) {
//        Map<String, String> map = new HashMap<>();
//        String query = uri.getQuery();
//        if (query != null) {
//            String decodedQueryString = "";
//            try {
//                decodedQueryString = URLDecoder.decode(query, "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                // Unable to decode, map will be empty
//            }
//
//            String[] params = decodedQueryString.split("&");
//            for (String param : params) {
//                String[] paramPair = param.split("=");
//                String name = paramPair[0];
//                String value = "";
//                if (paramPair.length > 1) {
//                    value = paramPair[1];
//                }
//                map.put(name, value);
//            }
//        }
//        return map;
//    }

    /**
     *
     * @param fileNameFromResponseHeader
     * @param actualDownloadUri
     * @param productDownloadDir
     * @return
     */
    public String determineFileName(String fileNameFromResponseHeader, URI actualDownloadUri, File productDownloadDir) {
        String fileName;
        if (fileNameFromResponseHeader != null) {
            fileName = fileNameFromResponseHeader;
        } else {
            fileName = FilenameUtils.getName(actualDownloadUri.getPath());
            if (fileName.length() > 0) {
                //remove query string and move download options if exist to the begining of the file
                //@see NGEOD-891
                String downloadOptions = null;
                for (NameValuePair param : URLEncodedUtils.parse(actualDownloadUri, "UTF-8")) {
                    if (param.getName().equalsIgnoreCase("ngEO_DO")) {
                        downloadOptions = param.getValue();
                        break;
                    }
                }
                if (downloadOptions != null && !downloadOptions.isEmpty()) {
                    fileName = "(" + downloadOptions.replaceAll(":", "=").replaceAll("\\{", "").replaceAll("}", "") + ")" + fileName;
                }
            } else {
                fileName = UUID.randomUUID().toString();
                LOGGER.warn(String.format("Resorting to use of UUID %s as a file name, to avoid zero-length name.", fileName));
            }
        }
        return resolveDuplicateFilePath(fileName, productDownloadDir);
    }

    /**
     *
     * @param fileName
     * @param downloadDir
     * @return
     */
    public String resolveDuplicateFilePath(String fileName, File downloadDir) {
        for (int i = 0; i <= 100; i++) {
            Path completedFolderPath, tempFolderPath;
            String fileNameToUseForDownload = fileName;
            if (i == 0) {
                completedFolderPath = Paths.get(downloadDir.getAbsolutePath(), fileName);
                tempFolderPath = Paths.get(downloadDir.getAbsolutePath(), String.format(".%s", fileName));
            } else {
                String baseName = FilenameUtils.getBaseName(fileName);
                String extension = FilenameUtils.getExtension(fileName);
                fileNameToUseForDownload = String.format("%s (%s).%s", baseName, i, extension);
                completedFolderPath = Paths.get(downloadDir.getAbsolutePath(), fileNameToUseForDownload);
                tempFolderPath = Paths.get(downloadDir.getAbsolutePath(), String.format(".%s", fileNameToUseForDownload));
            }
            if (!Files.exists(completedFolderPath) && !Files.exists(tempFolderPath)) {
                return fileNameToUseForDownload;
            }
        }
        return null;
    }
}
