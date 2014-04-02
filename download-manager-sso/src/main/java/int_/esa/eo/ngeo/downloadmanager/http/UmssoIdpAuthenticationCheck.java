package int_.esa.eo.ngeo.downloadmanager.http;

import java.util.HashMap;
import java.util.Map;

public class UmssoIdpAuthenticationCheck {
    private static UmssoIdpAuthenticationCheck instance = null;
    private Map<String, Boolean> idpAuthenticationMap;
    
    // Private constructor prevents instantiation from other classes
    private UmssoIdpAuthenticationCheck() {
        idpAuthenticationMap = new HashMap<String, Boolean>();
    }

    public static UmssoIdpAuthenticationCheck getInstance() {
       if(instance == null) {
           instance = new UmssoIdpAuthenticationCheck();
       }
       return instance;
    }
    
    public synchronized void resetAuthenticationCheck() {
        this.idpAuthenticationMap.clear();
    }
    
    public synchronized void setIdpAuthenticationEntry(String idpUrl, boolean authenticationAllowed) {
        idpAuthenticationMap.put(idpUrl, authenticationAllowed);
    }
    
    public synchronized boolean isAuthenticationAllowed(String idpUrl) {
        Boolean isAuthenticationAllowed = idpAuthenticationMap.get(idpUrl);
        if(isAuthenticationAllowed == null) {
            isAuthenticationAllowed = true;
        }
        return isAuthenticationAllowed;
    }
}
