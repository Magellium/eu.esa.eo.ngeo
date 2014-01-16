package int_.esa.eo.ngeo.downloadmanager.http;

import org.apache.commons.lang.StringUtils;

import com.siemens.pse.umsso.client.UmssoCLEnvironment;


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
public class UmSsoHttpConnectionSettings {
    private final String umSsoUsername, umSsoPassword, proxyHost, proxyUser, proxyPassword;
    private final int proxyPort;

    public UmSsoHttpConnectionSettings(String umssoUsername, String umssoPassword) {
        this(umssoUsername, umssoPassword, "", -1, "", "");
    }

    public UmSsoHttpConnectionSettings(String umssoUsername, String umssoPassword, String proxyHost, int proxyPort) {
        this(umssoUsername, umssoPassword, proxyHost, proxyPort, "", "");
    }

    public UmSsoHttpConnectionSettings(String umSsoUsername, String umSsoPassword, String proxyHost, int proxyPort, String proxyUser, String proxyPassword) {
        this.umSsoUsername = umSsoUsername;
        this.umSsoPassword = umSsoPassword;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;
    }

    public String getUmssoUsername() {
        return umSsoUsername;
    }

    public String getUmssoPassword() {
        return umSsoPassword;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public UmssoCLEnvironment getUmSsoCLEnvironmentFromProxySettings() {
        UmssoCLEnvironment umssoCLEnvironment = null;
        if (!StringUtils.isEmpty(proxyHost)) {
            if (!StringUtils.isEmpty(proxyUser)) {
                umssoCLEnvironment = new UmssoCLEnvironment(proxyHost, proxyPort, proxyUser, proxyPassword);
            } else {
                umssoCLEnvironment = new UmssoCLEnvironment(proxyHost, proxyPort);
            }
        }
        return umssoCLEnvironment;
    }

    public String toString() {
        return String.format("umSsoUserName %s%n umSsoPassword %s%n proxyHost %s%n proxyPort %s%n proxyUser %s%n proxyPassword %s", umSsoUsername, umSsoPassword, proxyHost, proxyPort, proxyUser, proxyPassword);
    }
}