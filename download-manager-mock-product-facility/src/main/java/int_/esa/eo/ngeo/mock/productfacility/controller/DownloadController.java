package int_.esa.eo.ngeo.mock.productfacility.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DownloadController {
	@RequestMapping(value = "/download/chunked", method = {RequestMethod.GET, RequestMethod.HEAD})
	public void downloadFileWithChunkedEncoding(HttpServletResponse servletResponse) throws IOException {
		
        servletResponse.setHeader("Content-type", "application/zip");

		URL resource = this.getClass().getResource("/downloads/20MB.zip");
		String urlDecodedPath = URLDecoder.decode(resource.getFile(), "UTF-8");
		File resourceFile = new File(urlDecodedPath);
        
        InputStream filestream = new FileInputStream(resourceFile);
        servletResponse.setBufferSize(0);

        IOUtils.copy(filestream, servletResponse.getOutputStream());
	}
}