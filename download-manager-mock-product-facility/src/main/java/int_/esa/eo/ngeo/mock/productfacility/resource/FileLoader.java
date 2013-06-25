package int_.esa.eo.ngeo.mock.productfacility.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.commons.io.FileUtils;

public class FileLoader {
	public String loadFileAsString(String path) throws IOException {
		URL resource = this.getClass().getResource(path);
		String urlDecodedPath = URLDecoder.decode(resource.getFile(), "UTF-8");
		File resourceFile = new File(urlDecodedPath);
		return FileUtils.readFileToString(resourceFile, "UTF-8");
	}
}
