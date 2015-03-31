package int_.esa.eo.ngeo.downloadmanager.webserver.builder;

import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.SchemaNotFoundException;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.exception.WebServerServiceException;
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

import com.siemens.pse.umsso.client.util.UmssoHttpResponse;
import com.siemens.pse.umsso.client.util.UmssoHttpResponseHelper;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;

public class NgeoWebServerResponseParser {

    private final XMLWithSchemaTransformer xmlWithSchemaTransformer;

    /**
     *
     * @param xmlWithSchemaTransformer
     */
    public NgeoWebServerResponseParser(XMLWithSchemaTransformer xmlWithSchemaTransformer) {
        this.xmlWithSchemaTransformer = xmlWithSchemaTransformer;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(NgeoWebServerResponseParser.class);

    /**
     *
     * @param ngEOWebServerUrl
     * @param response
     * @return
     * @throws ServiceException
     * @throws ParseException
     */
    public DMRegistrationMgmntResp parseDMRegistrationMgmntResponse(URL ngEOWebServerUrl, UmssoHttpResponse response) throws ServiceException, ParseException {
        return handleNgEoWebServerResponse(response, DMRegistrationMgmntResp.class, ngEOWebServerUrl);
    }

    /**
     *
     * @param ngEOWebServerUrl
     * @param response
     * @return
     * @throws ServiceException
     * @throws ParseException
     */
    public MonitoringURLResp parseMonitoringURLResponse(URL ngEOWebServerUrl, UmssoHttpResponse response) throws ServiceException, ParseException {
        return handleNgEoWebServerResponse(response, MonitoringURLResp.class, ngEOWebServerUrl);
    }

    /**
     *
     * @param ngEOWebServerUrl
     * @param response
     * @return
     * @throws ServiceException
     * @throws ParseException
     */
    public DataAccessMonitoringResp parseDataAccessMonitoringResponse(URL ngEOWebServerUrl, UmssoHttpResponse response) throws ServiceException, ParseException {
        return handleNgEoWebServerResponse(response, DataAccessMonitoringResp.class, ngEOWebServerUrl);
    }

    /**
     *
     * @param darResponse
     * @return
     * @throws ServiceException
     * @throws ParseException
     */
    public DataAccessMonitoringResp parseDataAccessMonitoringResponseFromString(String darResponse) throws ServiceException, ParseException {
        return handleDarResponse(darResponse, DataAccessMonitoringResp.class);
    }

    /**
     *
     * @param <T>
     * @param darResponse
     * @param resultType
     * @return
     * @throws ServiceException
     * @throws ParseException
     */
    public <T> T handleDarResponse(String darResponse, Class<T> resultType) throws ServiceException, ParseException {
        try {
            T responseAsObject;
            try {
                responseAsObject = xmlWithSchemaTransformer.deserializeAndInferSchema(new ByteArrayInputStream(darResponse.getBytes()), resultType);
            } catch (ParseException e) {
                LOGGER.debug(String.format("Parse exception occurred, response - %s: %n%s", resultType.getName(), darResponse));
                throw e;
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("response - %s: %n%s", resultType.getName(), new XmlFormatter().format(darResponse)));
            }
            return responseAsObject;
        } catch (SchemaNotFoundException e) {
            throw new ServiceException(String.format(e.getLocalizedMessage()));
        }
    }

    public <T> T handleNgEoWebServerResponse(UmssoHttpResponse response, Class<T> resultType, URL serviceUrl) throws ServiceException, ParseException {
        try {
            String responseBodyAsString = UmssoHttpResponseHelper.getInstance().getResponseBodyAsString(response);
            /*
             * XXX: The handling of the "error" scenario should be part of the response object itself, not a separate element (as per the schema)
             * This is how Terradue are currently implementing the IICD-D-WS interface.
             */
            InputStream responseBodyAsStream;
            Header[] headers = response.getHeaders();
            boolean gzip = false;
            for (Header header : headers) {
                if (header.getName().equalsIgnoreCase("content-encoding") && header.getValue().contains("gzip")) {
                    gzip = true;
                    break;
                }
            }
            if (gzip) {
                responseBodyAsStream = new GZIPInputStream(new ByteArrayInputStream(response.getBody()));
            } else {
                responseBodyAsStream = new ByteArrayInputStream(response.getBody());
            }
            int httpStatusCode = response.getStatusLine().getStatusCode();
            LOGGER.debug(String.format("status code: %s", httpStatusCode));
            switch (httpStatusCode) {
                case HttpStatus.SC_OK:
                    T responseAsObject;
                    try {
                        responseAsObject = xmlWithSchemaTransformer.deserializeAndInferSchema(responseBodyAsStream, resultType);
                    } catch (ParseException e) {
                        LOGGER.debug(String.format("Parse exception occurred, response - %s: %n%s", resultType.getName(), responseBodyAsString));
                        throw e;
                    }

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("response - %s: %n%s", resultType.getName(), new XmlFormatter().format(responseBodyAsString)));
                    }

                    return responseAsObject;
                default:
                    LOGGER.debug(String.format("response - %s: %n%s", resultType.getName(), responseBodyAsString));
                    String httpResponseMessage = response.getStatusLine().getReasonPhrase();
                    try {
                        Error exceptionReport = xmlWithSchemaTransformer.deserializeAndInferSchema(responseBodyAsStream, Error.class);

                        throw new WebServerServiceException(String.format("%s. Reason was: %s", httpResponseMessage, exceptionReport.getErrorDetail()));
                    } catch (ParseException e) {
                        throw new WebServerServiceException(String.format("Unexpected response to a request for %s: HTTP response code %s; \"%s\".",
                                serviceUrl.toString(), httpStatusCode, httpResponseMessage));
                    }
            }
        } catch (SchemaNotFoundException | IOException e) {
            throw new ServiceException(String.format(e.getLocalizedMessage()));
        }
    }
}
