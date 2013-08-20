package int_.esa.eo.ngeo.mock.webserver.controller;

import int_.esa.eo.ngeo.mock.webserver.resource.FileLoader;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Lewis Keen
 * This controller has been designed as a stub for the sunny day monitoring service scenario.
 */
@Controller
public class MonitorController {
	private boolean darSupplied = false;
	private boolean productsSupplied = false;
	private boolean standingOrderSupplied = false;
	private int standingOrderResponseEntry = 1;
	
	@RequestMapping(value = "/monitoringservice", method = RequestMethod.POST)
	public ResponseEntity<String> monitorForDars(HttpServletResponse servletResponse) throws IOException {
		FileLoader fileLoader = new FileLoader();
		HttpStatus status;
		String response;
		HttpHeaders responseHeaders = new HttpHeaders();
	
		status = HttpStatus.OK;
		if(darSupplied) {
			response = fileLoader.loadFileAsString("/stubs/MonitoringURL-Resp.xml");
		}else if(standingOrderSupplied) {
			response = fileLoader.loadFileAsString("/stubs/standingOrder/MonitoringURL-Resp.xml");
		}else{
			response = fileLoader.loadFileAsString("/stubs/MonitoringURL-Resp-NoDARs.xml");
		}
		responseHeaders.add("Content-Type", "application/xml; charset=utf-8");

		return new ResponseEntity<String>(response, responseHeaders, status);
	}

	@RequestMapping(value = "/monitoringservice/{monitoringUrlUuid}", method = RequestMethod.POST)
	public ResponseEntity<String> monitorForProducts(@PathVariable String monitoringUrlUuid) throws IOException {
		FileLoader fileLoader = new FileLoader();
		HttpStatus status;
		String response;
		HttpHeaders responseHeaders = new HttpHeaders();
	
		status = HttpStatus.OK;
		if(productsSupplied) {
			response = fileLoader.loadFileAsString("/stubs/DataAccessRequestMonitoring-Resp.xml");
		}else{
			response = fileLoader.loadFileAsString("/stubs/DataAccessRequestMonitoring-Resp-NoProducts.xml");
		}
		responseHeaders.add("Content-Type", "application/xml; charset=utf-8");

		return new ResponseEntity<String>(response, responseHeaders, status);
	}

	@RequestMapping(value = "/monitoringservice/standingOrder", method = RequestMethod.POST)
	public ResponseEntity<String> monitorForProductsInStandingOrder() throws IOException {
		FileLoader fileLoader = new FileLoader();
		HttpStatus status;
		String response;
		HttpHeaders responseHeaders = new HttpHeaders();
	
		status = HttpStatus.OK;
		if(standingOrderSupplied) {
			response = fileLoader.loadFileAsString(String.format("/stubs/standingOrder/StandingOrder-Resp-%s.xml", standingOrderResponseEntry));
			if(standingOrderResponseEntry < 3) {
				standingOrderResponseEntry++;
			}
		}else{
			response = fileLoader.loadFileAsString("/stubs/standingOrder/StandingOrder-Resp-1.xml");
		}
		responseHeaders.add("Content-Type", "application/xml; charset=utf-8");

		return new ResponseEntity<String>(response, responseHeaders, status);
	}

	public boolean isDarSupplied() {
		return darSupplied;
	}

	public void setDarSupplied(boolean darSupplied) {
		this.darSupplied = darSupplied;
	}

	public boolean isProductsSupplied() {
		return productsSupplied;
	}

	public void setProductsSupplied(boolean productsSupplied) {
		this.productsSupplied = productsSupplied;
	}

	public boolean isStandingOrderSupplied() {
		return standingOrderSupplied;
	}

	public void setStandingOrderSupplied(boolean standingOrderSupplied) {
		this.standingOrderSupplied = standingOrderSupplied;
	}
}