package int_.esa.umsso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.pse.umsso.client.UmssoCLCore;
import com.siemens.pse.umsso.client.UmssoCLCoreImpl;
import com.siemens.pse.umsso.client.UmssoCLEnvironment;
import com.siemens.pse.umsso.client.UmssoCLException;
import com.siemens.pse.umsso.client.UmssoCLInput;
import com.siemens.pse.umsso.client.UmssoUserCredentials;
import com.siemens.pse.umsso.client.UmssoVisualizerCallback;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(UmSsoHttpClient.class);
	private boolean enableUmssoJclUse;
	private CommandLineCallback commandLineCallback;
	
	public UmSsoHttpClient(String umssoUsername, String umssoPassword, String proxyLocation, int proxyPort, String proxyUser, String proxyPassword, boolean enableUmssoJclUse) {
		commandLineCallback = new CommandLineCallback(umssoUsername, umssoPassword);
		UmssoCLCore clCore = UmssoCLCoreImpl.getInstance();
		if (!StringUtils.isEmpty(proxyLocation)) {
			if (!StringUtils.isEmpty(proxyUser)) {
				clCore.init(new UmssoCLEnvironment(proxyLocation, proxyPort, proxyUser, proxyPassword));
			}
			else {
				clCore.init(new UmssoCLEnvironment(proxyLocation, proxyPort));
			}
		}
		this.enableUmssoJclUse = enableUmssoJclUse;

		// Set HttpClient parameters if necessary (this part is optional)
		HttpClientParams clientParams = new HttpClientParams();
		clientParams.setConnectionManagerTimeout(9000);
		clCore.getUmssoHttpClient().setParams(clientParams);
	}

	public HttpMethod executeGetRequest(URL requestUrl) throws UmssoCLException, IOException {
		HttpMethod method = new GetMethod(requestUrl.toString());
	    executeHttpRequest(method);
		return method;
	}

	public HttpMethod executeHeadRequest(URL requestUrl) throws UmssoCLException, IOException {
		HttpMethod method;
		//XXX: Since there is a bug with using Siemens' UM-SSO Java Client Library and HTTP HEAD, we use the GET method
		if(enableUmssoJclUse) {
			method = new GetMethod(requestUrl.toString());
		}else{
			method = new HeadMethod(requestUrl.toString());
		}
	    executeHttpRequest(method);
		return method;
	}

	public HttpMethod executePostRequest(URL requestUrl, ByteArrayOutputStream requestBody) throws UmssoCLException, IOException {
		return executePostRequest(requestUrl, requestBody, null, null);
	}
	
	public HttpMethod executePostRequest(URL requestUrl, ByteArrayOutputStream requestBody, String requestMimeType, String expectedResponseMimeType) throws UmssoCLException, IOException {
	    PostMethod method = new PostMethod(requestUrl.toString());
	    ByteArrayRequestEntity byteArrayRequestEntity = new ByteArrayRequestEntity(requestBody.toByteArray());
	    
	    method.setRequestEntity(byteArrayRequestEntity);
	    if(requestMimeType != null) {
		    method.addRequestHeader("Content-Type", requestMimeType);
	    }
	    if(expectedResponseMimeType != null) {
		    method.addRequestHeader("Accept", expectedResponseMimeType);
	    }

	    executeHttpRequest(method);
		return method;
	}
	
	public void executeHttpRequest(HttpMethod method) throws UmssoCLException, IOException {
		if (enableUmssoJclUse) {
			LOGGER.debug(String.format("Making an HTTP request with support for UM-SSO", method.getURI().toString()));
			UmssoCLInput input = new UmssoCLInput(method, commandLineCallback);  
			
			UmssoCLCore clCore = UmssoCLCoreImpl.getInstance();
			clCore.processHttpRequest(input);
		} else {
			LOGGER.debug(String.format("Making an HTTP request *without* support for UM-SSO", method.getURI().toString()));
			new HttpClient().executeMethod(method);
		}
	}

	private class CommandLineCallback implements UmssoVisualizerCallback {
		private static final int THRESHOLD_FOR_INFINITE_LOOP_DETECTION = 2;
		private int loginFormRenditionCount = 1;
		private UmssoUserCredentials umssoUserCredentials;
		
		public CommandLineCallback(String umssoUsername, String umssoPassword) {
			umssoUserCredentials = new UmssoUserCredentials(umssoUsername, umssoPassword.toCharArray());
		}
		
		public UmssoUserCredentials showLoginForm(String message, String spResourceUrl, String idpUrl) {
			if (loginFormRenditionCount >= THRESHOLD_FOR_INFINITE_LOOP_DETECTION) {
				String errorMessage = String.format("Invalid UM-SSO credentials.", loginFormRenditionCount);
				throw new RuntimeException(errorMessage);
			}
			loginFormRenditionCount++;
			return umssoUserCredentials;
		}
	}
}