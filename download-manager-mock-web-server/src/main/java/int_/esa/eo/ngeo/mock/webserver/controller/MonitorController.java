package int_.esa.eo.ngeo.mock.webserver.controller;

import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;
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
	private MonitoringStatus monitoringStatus = MonitoringStatus.IN_PROGRESS;
	private boolean productsSupplied = false;
	private boolean standingOrderSupplied = false;
	private String userOrder = "";
	private int standingOrderResponseEntry = 1;
	
	@RequestMapping(value = "/monitoringservice", method = RequestMethod.POST)
	public ResponseEntity<String> monitorForDars(HttpServletResponse servletResponse) throws Exception {
		if(userOrder != null && !userOrder.isEmpty()) {
			return sendUserOrder(userOrder);
		}
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
	public ResponseEntity<String> monitorForProducts(@PathVariable String monitoringUrlUuid) throws Exception {
		FileLoader fileLoader = new FileLoader();
		HttpStatus status;
		String response;
		HttpHeaders responseHeaders = new HttpHeaders();
	
		status = HttpStatus.OK;
		switch (monitoringStatus) {
		case IN_PROGRESS:
			if(productsSupplied) {
				response = fileLoader.loadFileAsString("/stubs/DataAccessRequestMonitoring-Resp.xml");
			}else{
				response = fileLoader.loadFileAsString("/stubs/DataAccessRequestMonitoring-NoProducts-Resp.xml");
			}
			break;
		case PAUSED:
			response = fileLoader.loadFileAsString("/stubs/DataAccessRequestMonitoring-Paused-Resp.xml");
			break;
		case CANCELLED:
			response = fileLoader.loadFileAsString("/stubs/DataAccessRequestMonitoring-Cancelled-Resp.xml");
			break;
		default:
			throw new RuntimeException(String.format("Unknown progress: ", monitoringStatus));
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

	public ResponseEntity<String> sendUserOrder(String userOrder) throws Exception {
		FileLoader fileLoader = new FileLoader();
		HttpStatus status;
		String response;
		HttpHeaders responseHeaders = new HttpHeaders();
	
		status = HttpStatus.OK;
	
		switch (userOrder) {
		case "STOP":
			response = fileLoader.loadFileAsString("/stubs/MonitoringURL-STOP.xml");
			break;
		case "STOP_IMMEDIATELY":
			response = fileLoader.loadFileAsString("/stubs/MonitoringURL-STOP_IMMEDIATELY.xml");
			break;
		default:
			throw new Exception(String.format("Invalid user order: %s", userOrder));
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

	public String getUserOrder() {
		return userOrder;
	}

	public void setUserOrder(String userOrder) {
		this.userOrder = userOrder;
	}

	public MonitoringStatus getMonitoringStatus() {
		return monitoringStatus;
	}

	public void setMonitoringStatus(MonitoringStatus monitoringStatus) {
		this.monitoringStatus = monitoringStatus;
	}
}