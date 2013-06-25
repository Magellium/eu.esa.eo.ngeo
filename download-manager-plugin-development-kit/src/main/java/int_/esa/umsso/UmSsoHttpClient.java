package int_.esa.umsso;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.pse.umsso.client.UmssoCLCore;
import com.siemens.pse.umsso.client.UmssoCLCoreImpl;
import com.siemens.pse.umsso.client.UmssoCLEnvironment;
import com.siemens.pse.umsso.client.UmssoCLException;
import com.siemens.pse.umsso.client.UmssoCLInput;

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
 * TODO: Install SSL certificates, as per SIE-EOOP_UMSSO_JCL_TN_2.1.1.doc. <p/>
 */
public class UmSsoHttpClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(UmSsoHttpClient.class);
	private static boolean isHappyToRiskBufferingWholeOfResponseBody = false;
	
//	private String proxyLocation;
//	private int proxyPort;
//	private String proxyUser;
//	private String proxyPassword;

	
	public UmSsoHttpClient(String proxyLocation, int proxyPort, String proxyUser, String proxyPassword) {
//		this.proxyLocation = proxyLocation;
//		this.proxyPort = proxyPort;
//		this.proxyUser = proxyUser;
//		this.proxyPassword = proxyPassword;

		UmssoCLCore clCore = UmssoCLCoreImpl.getInstance();
		if (!StringUtils.isEmpty(proxyLocation)) {
			if (!StringUtils.isEmpty(proxyUser)) {
				clCore.init(new UmssoCLEnvironment(proxyLocation, proxyPort, proxyUser, proxyPassword));
			}
			else {
				clCore.init(new UmssoCLEnvironment(proxyLocation, proxyPort));
			}
		}

		// Set HttpClient parameters if necessary (this part is optional)
		HttpClientParams clientParams = new HttpClientParams();
		clientParams.setConnectionManagerTimeout(9000);
		clCore.getUmssoHttpClient().setParams(clientParams);
	}

	public void executeHttpRequest(HttpMethod method) throws UmssoCLException {

		UmssoCLCore clCore = UmssoCLCoreImpl.getInstance();
		
		if (!isHappyToRiskBufferingWholeOfResponseBody) {
			try {
				LOGGER.warn("Making an HTTP request *without* support for UM-SSO, 'cos Siemens' UM-SSO Java Client Library buffers the whole of each response's body in memory");
				new HttpClient().executeMethod(method);
			} catch (IOException e) {
				throw new RuntimeException(e); // We're not doing rigorous error handling here, 'cos we will NOT be bypassing the UM-SSO JCL in the delivered system
			}
		}
		else {			
					UmssoCLInput input = new UmssoCLInput(method, null); // 2nd arg is null because we don't need a visualizer callback
			
					/* UmssoCLOutput output = */ clCore.processHttpRequest(input);
			
			//		HttpClient httpClient = null;
			//		if (!output.isResourceAccessed()) {
			//			LOGGER.debug("Resource couldn't be accessed.");
			//			// TODO: throw an exception here?
			//
			//			if (output.getStatus() == UmssoLoginStatus.LOGGEDIN) {
			//				LOGGER.debug("User logged in.");
			//			} else if (output.getStatus() == UmssoLoginStatus.LOGINFAILED) {
			//				LOGGER.debug("User couldn't log in.");
			//			} else if (output.getStatus() == UmssoLoginStatus.NOTLOGGEDIN) {
			//				LOGGER.debug("User is not logged in yet.");
			//			} else {
			//				LOGGER.debug("Unknown login status: " + output.getStatus());
			//			}
			//		}
			
			
			//		method.releaseConnection(); // XXX: We leave it to our caller to do this once they have consumed the HTTP response
			//
			//		//Destroy the security context by cleaning all cookies
			//		clCore.getUmssoHttpClient().getState().clearCookies(); // XXX: We leave it to our caller to consider doing this once they have consumed the HTTP response
		}
	}

}