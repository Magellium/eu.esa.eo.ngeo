package int_.esa.eo.ngeo.downloadmanager.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.pse.umsso.client.UmssoCLCore;
import com.siemens.pse.umsso.client.UmssoCLCoreImpl;
import com.siemens.pse.umsso.client.UmssoCLEnvironment;
import com.siemens.pse.umsso.client.UmssoCLException;
import com.siemens.pse.umsso.client.UmssoCLInput;
import com.siemens.pse.umsso.client.UmssoHttpGet;
import com.siemens.pse.umsso.client.UmssoHttpPost;
import com.siemens.pse.umsso.client.UmssoHttpResponseStoreInterface;
import com.siemens.pse.umsso.client.UmssoUserCredentials;
import com.siemens.pse.umsso.client.UmssoVisualizerCallback;
import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

/**
 * Testing has shown that this class can be used to access a resource that is NOT protected by UM-SSO.
 * <p/>
 * NB: Once a client has consumed the HTTP response it must do the following: <code>method.releaseConnection()</code>
 *
 * <p/>
 * Clients that wish to "log out" of the environment protected by UM-SSO can do the following:<p/>
 *   	clCore.getUmssoHttpClient().getState().clearCookies();
 * <p/>
 * Assumptions:
 *   <ul>
 *   	<li>UmssoCLOutput.isResourceAccessed() is redundant; a client can infer this from HTTPMethod.getStatusCode()</li>
 *   </ul>
 *   
 * Note that this class depends on the truststore containing the appropriate server certificate(s), as per SIE-EOOP_UMSSO_JCL_TN_2.1.1.doc. <p/>
 */
public class UmSsoHttpClient {
    private static final int HTTP_MAX_TOTAL_CONNECTIONS = 200;
    private static final int HTTP_DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 20;

    private static final Logger LOGGER = LoggerFactory.getLogger(UmSsoHttpClient.class);
    private UmSsoHttpConnectionSettings umSsoHttpConnectionSettings;

    public UmSsoHttpClient(UmSsoHttpConnectionSettings umSsoHttpConnectionSettings) {
        this.umSsoHttpConnectionSettings = umSsoHttpConnectionSettings;
    }

    public UmSsoHttpRequestAndResponse executeGetRequest(URL requestUrl) throws UmssoCLException, IOException {
        return executeGetRequest(requestUrl, null);
    }

    public UmSsoHttpRequestAndResponse executeGetRequest(URL requestUrl, List<Header> headers) throws UmssoCLException, IOException {
        UmssoHttpGet request = new UmssoHttpGet(requestUrl.toString());
        if(headers != null) {
            for (Header header : headers) {
                request.addHeader(header);
            }
        }
        executeHttpRequest(request);
        return new UmSsoHttpRequestAndResponse(request, getUmssoHttpResponse(request));
    }

    public UmSsoHttpRequestAndResponse executeHeadRequest(URL requestUrl) throws UmssoCLException, IOException {
        /* 
         * Since there is a bug with using Siemens' UM-SSO Java Client Library and HTTP HEAD
         * we use the GET method.
         */
        return executeGetRequest(requestUrl);
    }

    public UmSsoHttpRequestAndResponse executePostRequest(URL requestUrl, ByteArrayOutputStream requestBody) throws UmssoCLException, IOException {
        return executePostRequest(requestUrl, requestBody, null, null);
    }

    public UmSsoHttpRequestAndResponse executePostRequest(URL requestUrl, ByteArrayOutputStream requestBody, String requestMimeType, String expectedResponseMimeType) throws UmssoCLException, IOException {
        UmssoHttpPost request = new UmssoHttpPost(requestUrl.toString());
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(requestBody.toByteArray());

        request.setEntity(byteArrayEntity);
        if(requestMimeType != null) {
            request.addHeader("Content-Type", requestMimeType);
        }
        if(expectedResponseMimeType != null) {
            request.addHeader("Accept", expectedResponseMimeType);
        }

        executeHttpRequest(request);
        return new UmSsoHttpRequestAndResponse(request, getUmssoHttpResponse(request));
    }

    public void executeHttpRequest(HttpRequestBase request) throws UmssoCLException, IOException {
        UmssoCLInput input = initializeInput(request);
        UmssoCLCore clCore = initializeCore();

        LOGGER.debug(String.format("Making the following HTTP request with support for UM-SSO%n%s%nConnection Settings: %s", request.getURI().toString(), umSsoHttpConnectionSettings.toString()));
        clCore.processHttpRequest(input);
    }

    private UmssoCLInput initializeInput(HttpRequestBase request) {
        request.setHeader("Accept-Encoding", "gzip,deflate,sdch");
        return new UmssoCLInput(request, new CommandLineCallback());  
    }

    private UmssoCLCore initializeCore() {
        UmssoCLCore clCore = UmssoCLCoreImpl.getInstance();
        HttpParams params = clCore.getUmssoHttpClient().getParams();
        HttpConnectionParams.setConnectionTimeout(params, 10000);
        PoolingClientConnectionManager cm = clCore.getConnectionManager();
        cm.setMaxTotal(HTTP_MAX_TOTAL_CONNECTIONS);
        cm.setDefaultMaxPerRoute(HTTP_DEFAULT_MAX_CONNECTIONS_PER_ROUTE);

        UmssoCLEnvironment umssoCLEnvironment = umSsoHttpConnectionSettings.getUmSsoCLEnvironmentFromProxySettings();
        if(umssoCLEnvironment != null) {
            clCore.init(umssoCLEnvironment);
        }
        return clCore;
    }

    private UmssoHttpResponse getUmssoHttpResponse(UmssoHttpResponseStoreInterface request) {
        return request.getHttpResponseStore().getHttpResponse();
    }

    public UmSsoHttpConnectionSettings getUmSsoHttpConnectionSettings() {
        return umSsoHttpConnectionSettings;
    }

    public void setUmSsoHttpConnectionSettings(UmSsoHttpConnectionSettings umSsoHttpConnectionSettings) {
        this.umSsoHttpConnectionSettings = umSsoHttpConnectionSettings;
    }

    public class CommandLineCallback implements UmssoVisualizerCallback {
        private static final int THRESHOLD_FOR_INFINITE_LOOP_DETECTION = 2;
        private int loginFormRenditionCount = 1;

        public UmssoUserCredentials showLoginForm(String message, String spResourceUrl, String idpUrl) {
            if (loginFormRenditionCount >= THRESHOLD_FOR_INFINITE_LOOP_DETECTION) {
                String errorMessage = String.format("Invalid UM-SSO credentials.", loginFormRenditionCount);
                throw new RuntimeException(errorMessage);
            }
            loginFormRenditionCount++;
            return new UmssoUserCredentials(getUmSsoHttpConnectionSettings().getUmssoUsername(), getUmSsoHttpConnectionSettings().getUmssoPassword().toCharArray());
        }
    }
}