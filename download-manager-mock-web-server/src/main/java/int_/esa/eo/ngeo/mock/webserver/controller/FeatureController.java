package int_.esa.eo.ngeo.mock.webserver.controller;

import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;

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
		monitorController.setDarSupplied(true);
		
		return getHTTP200ResponseEntity();
	}

	@RequestMapping(value = "/feature/pauseDAR", method = RequestMethod.GET)
	public ResponseEntity<String> pauseDar(HttpServletResponse servletResponse) throws IOException {
		monitorController.setMonitoringStatus(MonitoringStatus.PAUSED);
		
		return getHTTP200ResponseEntity();
	}

	@RequestMapping(value = "/feature/resumeDAR", method = RequestMethod.GET)
	public ResponseEntity<String> resumeDar(HttpServletResponse servletResponse) throws IOException {
		monitorController.setMonitoringStatus(MonitoringStatus.IN_PROGRESS);
		
		return getHTTP200ResponseEntity();
	}

	@RequestMapping(value = "/feature/cancelDAR", method = RequestMethod.GET)
	public ResponseEntity<String> cancelDar(HttpServletResponse servletResponse) throws IOException {
		monitorController.setMonitoringStatus(MonitoringStatus.CANCELLED);
		
		return getHTTP200ResponseEntity();
	}

	@RequestMapping(value = "/feature/supplyProducts", method = RequestMethod.GET)
	public ResponseEntity<String> supplyProducts(HttpServletResponse servletResponse) throws IOException {
		monitorController.setProductsSupplied(true);
		
		return getHTTP200ResponseEntity();
	}

	@RequestMapping(value = "/feature/supplyStandingOrder", method = RequestMethod.GET)
	public ResponseEntity<String> supplyStandingOrder(HttpServletResponse servletResponse) throws IOException {
		monitorController.setStandingOrderSupplied(true);
		
		return getHTTP200ResponseEntity();
	}
	
	@RequestMapping(value = "/feature/stop", method = RequestMethod.GET)
	public ResponseEntity<String> stop(HttpServletResponse servletResponse) throws IOException {
		monitorController.setUserOrder("STOP");
		
		return getHTTP200ResponseEntity();
	}

	@RequestMapping(value = "/feature/stop_immediately", method = RequestMethod.GET)
	public ResponseEntity<String> stopImmediately(HttpServletResponse servletResponse) throws IOException {

		monitorController.setUserOrder("STOP_IMMEDIATELY");
		
		return getHTTP200ResponseEntity();
	}
	
	private ResponseEntity<String> getHTTP200ResponseEntity() {
		HttpStatus status = HttpStatus.OK;
		String response = "OK";
		HttpHeaders responseHeaders = new HttpHeaders();

		return new ResponseEntity<String>(response, responseHeaders, status);
	}
}