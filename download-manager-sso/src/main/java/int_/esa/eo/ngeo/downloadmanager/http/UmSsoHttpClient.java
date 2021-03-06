package int_.esa.eo.ngeo.downloadmanager.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
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
import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

/**
 * Testing has shown that this class can be used to access a resource that is NOT protected by UM-SSO.
 * <p/>
 * NB: Once a client has consumed the HTTP response it must do the following: <code>method.releaseConnection()</code>
 *
 * <p/>
 * Clients that wish to "log out" of the environment protected by UM-SSO can do the following:<p/>
 *      clCore.getUmssoHttpClient().getState().clearCookies();
 * <p/>
 * Assumptions:
 *   <ul>
 *      <li>UmssoCLOutput.isResourceAccessed() is redundant; a client can infer this from HTTPMethod.getStatusCode()</li>
 *   </ul>
 *   
 * Note that this class depends on the truststore containing the appropriate server certificate(s), as per SIE-EOOP_UMSSO_JCL_TN_2.1.1.doc. <p/>
 */
public class UmSsoHttpClient {
    private static final int HTTP_MAX_TOTAL_CONNECTIONS = 200;
    private static final int HTTP_DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 20;

    private static final Logger LOGGER = LoggerFactory.getLogger(UmSsoHttpClient.class);
    private UmSsoHttpConnectionSettings umSsoHttpConnectionSettings;
    private SSLSocketFactory sslIgnoreCertificatesSocketFactory;

    public UmSsoHttpClient(UmSsoHttpConnectionSettings umSsoHttpConnectionSettings) {
        this.umSsoHttpConnectionSettings = umSsoHttpConnectionSettings;
        
        try {
            sslIgnoreCertificatesSocketFactory = new SSLSocketFactory(new TrustStrategy() {
                @Override
                public boolean isTrusted(
                        final X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
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
        return new UmssoCLInput(request, new UmSsoUserCredentialsCallback(getUmSsoHttpConnectionSettings().getUmssoUsername(), getUmSsoHttpConnectionSettings().getUmssoPassword()));  
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
        
        if(umSsoHttpConnectionSettings.isIgnoreCertificates()) {
            ignoreCertificates(clCore.getUmssoHttpClient());
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

    private void ignoreCertificates(HttpClient httpclient) {
        httpclient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, sslIgnoreCertificatesSocketFactory));
    }
}