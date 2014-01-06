package int_.esa.eo.ngeo.downloadmanager.http;

import org.apache.http.client.methods.HttpRequestBase;

import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

/*
 * This is a wrapper class for the request and response during a message exchange using the UM-SSO JCL library.
 */
public class UmSsoHttpRequestAndResponse {
    private final HttpRequestBase request;
    private final UmssoHttpResponse response;

    public UmSsoHttpRequestAndResponse(HttpRequestBase request, UmssoHttpResponse response) {
        this.request = request;
        this.response = response;
    }

    public HttpRequestBase getRequest() {
        return request;
    }

    public UmssoHttpResponse getResponse() {
        return response;
    }

    public void cleanupHttpResources() {
        if(request != null) {
            request.abort();
            request.releaseConnection();
        }
    }
}
