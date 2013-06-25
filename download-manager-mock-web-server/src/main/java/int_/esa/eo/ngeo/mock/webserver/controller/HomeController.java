package int_.esa.eo.ngeo.mock.webserver.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController {
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ResponseEntity<String> home(HttpServletResponse servletResponse) throws IOException {
		return new ResponseEntity<String>("Welcome!", new HttpHeaders(), HttpStatus.OK);
	}
}