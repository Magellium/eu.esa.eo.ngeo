package int_.esa.eo.ngeo.downloadmanager.http;

import com.siemens.pse.umsso.client.UmssoUserCredentials;
import com.siemens.pse.umsso.client.UmssoVisualizerCallback;

public class UmSsoUserCredentialsCallback implements UmssoVisualizerCallback {
    private static final int THRESHOLD_FOR_INFINITE_LOOP_DETECTION = 2;
    private int loginFormRenditionCount = 1;
    private String umssoUsername, umssoPassword;
    
    public UmSsoUserCredentialsCallback(String umssoUsername, String umssoPassword) {
        this.umssoUsername = umssoUsername;
        this.umssoPassword = umssoPassword;
    }

    public UmssoUserCredentials showLoginForm(String message, String spResourceUrl, String idpUrl) {
        UmssoIdpAuthenticationCheck umssoIdpAuthenticationCheck = UmssoIdpAuthenticationCheck.getInstance();
        if(!umssoIdpAuthenticationCheck.isAuthenticationAllowed(idpUrl)) {
            String errorMessage = String.format("Invalid UM-SSO credentials.", loginFormRenditionCount);
            throw new RuntimeException(errorMessage);
        }
        if (loginFormRenditionCount >= THRESHOLD_FOR_INFINITE_LOOP_DETECTION) {
            umssoIdpAuthenticationCheck.setIdpAuthenticationEntry(idpUrl, false);
            String errorMessage = String.format("Invalid UM-SSO credentials.");
            throw new RuntimeException(errorMessage);
        }
        loginFormRenditionCount++;
        return new UmssoUserCredentials(this.umssoUsername, this.umssoPassword.toCharArray());
    }
}