package int_.esa.eo.ngeo.downloadmanager.cli.service;

import int_.esa.eo.ngeo.downloadmanager.cli.exception.ResourceNotFoundException;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithDarUuid;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithProductUuid;
import int_.esa.eo.ngeo.downloadmanager.rest.StatusResponse;
import int_.esa.eo.ngeo.downloadmanager.transform.JSONTransformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class DownloadManagerResponseParser {
    private static final String SERVICE_EXCEPTION_ERROR_STRING = "Error: HTTP %s response code received from the Download Manager";
    private static final Logger LOGGER = Logger.getLogger(DownloadManagerResponseParser.class.getName());
    JSONTransformer jsonTransformer;
    
    public CommandResponse parseCommandResponse(HttpURLConnection conn) throws ServiceException {
        final UnexpectedResponseHandler<CommandResponse> unexpectedResponseHandler = new UnexpectedResponseHandler<CommandResponse>() {
            @Override
            public ServiceException createServiceExceptionForUnexpectedResponse(final int httpResponseCode, final CommandResponse parsedResponse) {
                return new ServiceException(String.format(SERVICE_EXCEPTION_ERROR_STRING + ": %s", httpResponseCode, parsedResponse.getErrorMessage()));
            }
        };
        return parseResponse(conn, CommandResponse.class, unexpectedResponseHandler);
    }

    public CommandResponseWithDarUuid parseCommandResponseWithDarUuid(HttpURLConnection conn) throws ServiceException {
        UnexpectedResponseHandler<CommandResponseWithDarUuid> unexpectedResponseHandler = new UnexpectedResponseHandler<CommandResponseWithDarUuid>() {
            @Override
            public ServiceException createServiceExceptionForUnexpectedResponse(int httpResponseCode, CommandResponseWithDarUuid parsedResponse) {
                return new ServiceException(String.format(SERVICE_EXCEPTION_ERROR_STRING + ": %s", httpResponseCode, parsedResponse.getErrorMessage()));
            }
        };
        return parseResponse(conn, CommandResponseWithDarUuid.class, unexpectedResponseHandler);
    }

    public CommandResponseWithProductUuid parseCommandResponseWithProductUuid(HttpURLConnection conn) throws ServiceException {
        UnexpectedResponseHandler<CommandResponseWithProductUuid> unexpectedResponseHandler = new UnexpectedResponseHandler<CommandResponseWithProductUuid>() {
            @Override
            public ServiceException createServiceExceptionForUnexpectedResponse(int httpResponseCode, CommandResponseWithProductUuid parsedResponse) {
                return new ServiceException(String.format(SERVICE_EXCEPTION_ERROR_STRING + ": %s", httpResponseCode, parsedResponse.getErrorMessage()));
            }
        };
        return parseResponse(conn, CommandResponseWithProductUuid.class, unexpectedResponseHandler);
    }

    public StatusResponse parseStatusResponse(HttpURLConnection conn) throws ServiceException {
        UnexpectedResponseHandler<StatusResponse> unexpectedResponseHandler = new UnexpectedResponseHandler<StatusResponse>() {
            @Override
            public ServiceException createServiceExceptionForUnexpectedResponse(int httpResponseCode, StatusResponse parsedResponse) {
                return new ServiceException(String.format(SERVICE_EXCEPTION_ERROR_STRING + ".", httpResponseCode));
            }
        };
        return parseResponse(conn, StatusResponse.class, unexpectedResponseHandler);
    }

    private <T> T parseResponse(HttpURLConnection conn, Class<T> commandResponseClass, UnexpectedResponseHandler<T> unExpectedResponseHandler)
            throws ServiceException {
        try {
            final int httpResponseCode = conn.getResponseCode();
            LOGGER.debug("HTTP response code = " + httpResponseCode);
            switch (httpResponseCode) {
            case HttpURLConnection.HTTP_OK:
                return getJsonTransformer().deserialize(conn.getInputStream(), commandResponseClass);
            case HttpURLConnection.HTTP_NOT_FOUND:
                throw new ResourceNotFoundException(String.format("Error: The CLI's reference to the relevant Download Manager command (%s) describes a nonexistent resource", conn.getURL().toString()));
            default:
                String errorStreamContent = IOUtils.toString(conn.getErrorStream());
                T response = parseNonOKResponse(errorStreamContent, httpResponseCode, commandResponseClass);
                throw unExpectedResponseHandler.createServiceExceptionForUnexpectedResponse(httpResponseCode, response);
            }
        } catch(IOException e) {
            throw new ServiceException(e);
        }
    }

    private <T> T parseNonOKResponse(String errorStreamContent, int httpResponseCode, Class<T> responseClass) throws ServiceException {
        LOGGER.debug("JSON = " + errorStreamContent);

        //Extract the command response JSON from the response, as the server wraps the response as a "response" parameter.
        Pattern commandResponseFromResponsePattern = Pattern.compile("\\{\"response\":(.*)\\}", Pattern.CASE_INSENSITIVE);
        Matcher commandResponseFromResponseMatcher = commandResponseFromResponsePattern.matcher(errorStreamContent);

        if (commandResponseFromResponseMatcher.find()) {
            String trimmedErrorStreamContent = commandResponseFromResponseMatcher.group(1);

            try {
                return getJsonTransformer().deserialize(new ByteArrayInputStream(trimmedErrorStreamContent.getBytes(StandardCharsets.UTF_8)), responseClass);
            } catch (IOException e) {
                LOGGER.error("Unable to parse a non HTTP 200 response", e);
            }
        }
        throw new ServiceException(String.format("Error: HTTP %s response code received from the Download Manager.", httpResponseCode));
    }
    
    protected JSONTransformer getJsonTransformer() {
        if(jsonTransformer == null) {
            jsonTransformer = new JSONTransformer();
        }
        return jsonTransformer;
    }
}
