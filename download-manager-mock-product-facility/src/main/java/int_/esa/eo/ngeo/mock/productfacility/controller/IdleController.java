package int_.esa.eo.ngeo.mock.productfacility.controller;

import int_.esa.eo.ngeo.mock.productfacility.resource.FileLoader;

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
 * This controller has been designed as a stub for the sunny day "Accepted" scenario.
 */
@Controller
public class IdleController {
	private boolean productIdle = true;
	
	@RequestMapping(value = "/idle", method = {RequestMethod.GET, RequestMethod.HEAD})
	public ResponseEntity<String> idle(HttpServletResponse servletResponse) throws IOException {
		FileLoader fileLoader = new FileLoader();
		HttpStatus status;
		String response;
		HttpHeaders responseHeaders = new HttpHeaders();
	
		if(isProductIdle()) {
			status = HttpStatus.ACCEPTED;
			response = fileLoader.loadFileAsString("/stubs/productDownloadResponse.xml");
			responseHeaders.add("Content-Type", "application/xml; charset=utf-8");
		}else{
			status = HttpStatus.OK;
			response = fileLoader.loadFileAsString("/stubs/idle-product-metalink-example.xml");
			responseHeaders.add("Content-Type", "application/metalink+xml; charset=utf-8");
		}
		return new ResponseEntity<String>(response, responseHeaders, status);
	}

	public boolean isProductIdle() {
		return productIdle;
	}

	public void setProductIdle(boolean productIdle) {
		this.productIdle = productIdle;
	}
}