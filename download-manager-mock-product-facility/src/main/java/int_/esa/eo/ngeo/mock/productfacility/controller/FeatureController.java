package int_.esa.eo.ngeo.mock.productfacility.controller;

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
 * This controller has been designed to enable features of the mock product facility for demo purposes.
 */
@Controller
public class FeatureController {
	@Autowired
	IdleController idleController;
	
	@RequestMapping(value = "/feature/idleProductAvailable", method = RequestMethod.GET)
	public ResponseEntity<String> idleProductAvailable(HttpServletResponse servletResponse) throws IOException {
		HttpStatus status = HttpStatus.OK;
		String response = "OK";
		HttpHeaders responseHeaders = new HttpHeaders();

		idleController.setProductIdle(false);
		
		return new ResponseEntity<String>(response, responseHeaders, status);
	}
}