package int_.esa.eo.ngeo.downloadmanager.webserver;

import int_.esa.eo.ngeo.downloadmanager.webserver.builder.NgeoWebServerRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.webserver.builder.NgeoWebServerResponseParser;
import int_.esa.eo.ngeo.downloadmanager.webserver.service.NgeoWebServerServiceInterface;

public class NgeoWebServerServiceHelper {
    private final NgeoWebServerRequestBuilder builder;
    private final NgeoWebServerServiceInterface service;
    private final NgeoWebServerResponseParser parser;
    
    public NgeoWebServerServiceHelper(NgeoWebServerRequestBuilder builder, NgeoWebServerServiceInterface service, NgeoWebServerResponseParser parser) {
        this.builder = builder;
        this.parser = parser;
        this.service = service;
    }

    public NgeoWebServerRequestBuilder getRequestBuilder() {
        return builder;
    }

    public NgeoWebServerServiceInterface getService() {
        return service;
    }

    public NgeoWebServerResponseParser getResponseParser() {
        return parser;
    }
}
