package int_.esa.eo.ngeo.mock.webserver.controller;

import int_.esa.eo.ngeo.mock.webserver.resource.FileLoader;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Lewis Keen
 */
@Controller
public class StaticDataController {
	@RequestMapping(value = "/static/manualDAR", method = RequestMethod.GET)
	public ResponseEntity<String> register(HttpServletResponse servletResponse) throws IOException {
		FileLoader fileLoader = new FileLoader();
		HttpStatus status;
		String response;
		HttpHeaders responseHeaders = new HttpHeaders();
	
		status = HttpStatus.OK;
		response = fileLoader.loadFileAsString("/stubs/static/DataAccessRequestMonitoring-Resp.xml");
		responseHeaders.add("Content-Type", "application/xml; charset=utf-8");

		return new ResponseEntity<String>(response, responseHeaders, status);
	}
}