package int_.esa.eo.ngeo.dmtu.webserver.builder;

import int_.esa.eo.ngeo.dmtu.exception.ParseException;
import int_.esa.eo.ngeo.dmtu.exception.ServiceException;
import int_.esa.eo.ngeo.dmtu.exception.WebServerServiceException;
import int_.esa.eo.ngeo.dmtu.jaxb.JaxbUtils;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.Error;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLResp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NgeoWebServerResponseParser {
	@Autowired
	private JaxbUtils jaxbUtils;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NgeoWebServerResponseParser.class);
	
	public DMRegistrationMgmntResp parseDMRegistrationMgmntResponse(URL ngEOWebServerUrl, HttpMethod response) throws ServiceException, ParseException {
		return handleNgEoWebServerResponse(ngEOWebServerUrl, response, DMRegistrationMgmntResp.class);
	}
	
	public MonitoringURLResp parseMonitoringURLResponse(URL ngEOWebServerUrl, HttpMethod response) throws ServiceException, ParseException {
		return handleNgEoWebServerResponse(ngEOWebServerUrl, response, MonitoringURLResp.class);
	}
	
	public DataAccessMonitoringResp parseDataAccessMonitoringResponse(URL ngEOWebServerUrl, HttpMethod response) throws ServiceException, ParseException {
		return handleNgEoWebServerResponse(ngEOWebServerUrl, response, DataAccessMonitoringResp.class);
	}

	public <T> T handleNgEoWebServerResponse(URL serviceUrl, HttpMethod response, Class<T> resultType) throws ServiceException, ParseException {
	    try {
			String responseBodyAsString = response.getResponseBodyAsString();
			LOGGER.debug(String.format("%s: %s", resultType.getName(), responseBodyAsString));
			/* 
			 * XXX: The handling of the "error" scenario should be part of the response object itself, not a separate element (as per the schema)
			 * This is how Terradue are currently implementing the IICD-D-WS interface.
			 */
			
			InputStream responseBodyAsStream = response.getResponseBodyAsStream();
			int httpStatusCode = response.getStatusCode();
			LOGGER.debug(String.format("status code: %s",httpStatusCode));
			switch(httpStatusCode) {
			case HttpStatus.SC_OK:
			    T responseAsObject = jaxbUtils.deserializeAndInferSchema(responseBodyAsStream, resultType);

			    return responseAsObject;
			default:
				String httpResponseMessage = response.getStatusLine().getReasonPhrase();
				try {
					Error exceptionReport = jaxbUtils.deserializeAndInferSchema(responseBodyAsStream, Error.class);

					throw new WebServerServiceException(String.format("%s. Reason was: %s", httpResponseMessage, exceptionReport.getErrorDetail()));
				}catch(ParseException e) {
					throw new WebServerServiceException(String.format("Unable to parse request from %s: HTTP response code %s; \"%s\".",
							serviceUrl.toString(), httpStatusCode, httpResponseMessage));
				}
			}
		} catch (IOException e) {
			throw new ServiceException(String.format(e.getLocalizedMessage()));
		} finally {
			response.releaseConnection();
		}
	}
}
