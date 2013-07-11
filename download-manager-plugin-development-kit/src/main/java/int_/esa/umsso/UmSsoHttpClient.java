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

	public void executeHttpRequest(HttpMethod method) throws UmssoCLException, IOException {
		if (enableUmssoJclUse) {
			UmssoCLInput input = new UmssoCLInput(method, commandLineCallback);  
			
			UmssoCLCore clCore = UmssoCLCoreImpl.getInstance();
			clCore.processHttpRequest(input);
		} else {			
			LOGGER.warn("Making an HTTP request *without* support for UM-SSO");
			LOGGER.warn("curently the Siemens' UM-SSO Java Client Library buffers the whole of each response's body in memory");
			new HttpClient().executeMethod(method);
		}
	}

	private class CommandLineCallback implements UmssoVisualizerCallback {
		private static final int THRESHOLD_FOR_INFINITE_LOOP_DETECTION = 20;
		private int loginFormRenditionCount = 1;
		private UmssoUserCredentials umssoUserCredentials;
		
		public CommandLineCallback(String umssoUsername, String umssoPassword) {
			umssoUserCredentials = new UmssoUserCredentials(umssoUsername, umssoPassword.toCharArray());
		}
		
		public UmssoUserCredentials showLoginForm(String message, String spResourceUrl, String idpUrl) {
			if (loginFormRenditionCount >= THRESHOLD_FOR_INFINITE_LOOP_DETECTION) {
				LOGGER.error(String.format("Possible infinite loop because of bad UM-SSO credentials? There have been %s consecutive unsuccessful attempts to login using these credentials.", loginFormRenditionCount));
			}
			loginFormRenditionCount++;
			return umssoUserCredentials;
		}
	}
}