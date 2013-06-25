package int_.esa.eo.ngeo.mock.webserver.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Lewis Keen
 * This controller has been designed to enable features of the mock web server for demo purposes.
 */
@Controller
public class FeatureController {
	@Autowired
	MonitorController monitorController;
	
	@RequestMapping(value = "/feature/supplyDAR", method = RequestMethod.GET)
	public ResponseEntity<String> supplyDar(HttpServletResponse servletResponse) throws IOException {
		HttpStatus status = HttpStatus.OK;
		String response = "OK";
		HttpHeaders responseHeaders = new HttpHeaders();

		monitorController.setDarSupplied(true);
		
		return new ResponseEntity<String>(response, responseHeaders, status);
	}

	@RequestMapping(value = "/feature/supplyProducts", method = RequestMethod.GET)
	public ResponseEntity<String> supplyProducts(HttpServletResponse servletResponse) throws IOException {
		HttpStatus status = HttpStatus.OK;
		String response = "OK";
		HttpHeaders responseHeaders = new HttpHeaders();

		monitorController.setProductsSupplied(true);
		
		return new ResponseEntity<String>(response, responseHeaders, status);
	}
}