package int_.esa.eo.ngeo.dmtu.webserver.builder;

import int_.esa.eo.ngeo.dmtu.exception.WebServerServiceException;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.SchemaNotFoundException;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.transform.XMLWithSchemaTransformer;
import int_.esa.eo.ngeo.downloadmanager.utils.XmlFormatter;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.Error;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLResp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.siemens.pse.umsso.client.util.UmssoHttpResponse;
import com.siemens.pse.umsso.client.util.UmssoHttpResponseHelper;

@Component
public class NgeoWebServerResponseParser {
    @Autowired
    private XMLWithSchemaTransformer xmlWithSchemaTransformer;

    private static final Logger LOGGER = LoggerFactory.getLogger(NgeoWebServerResponseParser.class);

    public DMRegistrationMgmntResp parseDMRegistrationMgmntResponse(URL ngEOWebServerUrl, UmssoHttpResponse response) throws ServiceException, ParseException {
        return handleNgEoWebServerResponse(ngEOWebServerUrl, response, DMRegistrationMgmntResp.class);
    }

    public MonitoringURLResp parseMonitoringURLResponse(URL ngEOWebServerUrl, UmssoHttpResponse response) throws ServiceException, ParseException {
        return handleNgEoWebServerResponse(ngEOWebServerUrl, response, MonitoringURLResp.class);
    }

    public DataAccessMonitoringResp parseDataAccessMonitoringResponse(URL ngEOWebServerUrl, UmssoHttpResponse response) throws ServiceException, ParseException {
        return handleNgEoWebServerResponse(ngEOWebServerUrl, response, DataAccessMonitoringResp.class);
    }

    public <T> T handleNgEoWebServerResponse(URL serviceUrl, UmssoHttpResponse response, Class<T> resultType) throws ServiceException, ParseException {
        try {
            String responseBodyAsString = UmssoHttpResponseHelper.getInstance().getResponseBodyAsString(response);
            /* 
             * XXX: The handling of the "error" scenario should be part of the response object itself, not a separate element (as per the schema)
             * This is how Terradue are currently implementing the IICD-D-WS interface.
             */

            InputStream responseBodyAsStream = new ByteArrayInputStream(response.getBody());
            int httpStatusCode = response.getStatusLine().getStatusCode();
            LOGGER.debug(String.format("status code: %s",httpStatusCode));
            switch(httpStatusCode) {
            case HttpStatus.SC_OK:
                T responseAsObject;
                try {
                    responseAsObject = xmlWithSchemaTransformer.deserializeAndInferSchema(responseBodyAsStream, resultType);
                }catch(ParseException e) {
                    LOGGER.debug(String.format("Parse exception occurred, response - %s: %n%s", resultType.getName(), responseBodyAsString));
                    throw e;
                }

                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("response - %s: %n%s", resultType.getName(), new XmlFormatter().format(responseBodyAsString)));
                }

                return responseAsObject;
            default:
                LOGGER.debug(String.format("response - %s: %n%s", resultType.getName(), responseBodyAsString));
                String httpResponseMessage = response.getStatusLine().getReasonPhrase();
                try {
                    Error exceptionReport = xmlWithSchemaTransformer.deserializeAndInferSchema(responseBodyAsStream, Error.class);

                    throw new WebServerServiceException(String.format("%s. Reason was: %s", httpResponseMessage, exceptionReport.getErrorDetail()));
                }catch(ParseException e) {
                    throw new WebServerServiceException(String.format("Unexpected response to a request for %s: HTTP response code %s; \"%s\".",
                            serviceUrl.toString(), httpStatusCode, httpResponseMessage));
                }
            }
        } catch (SchemaNotFoundException e) {
            throw new ServiceException(String.format(e.getLocalizedMessage()));
        }
    }
}
