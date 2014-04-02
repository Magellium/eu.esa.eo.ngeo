package int_.esa.eo.ngeo.downloadmanager.monitor;

import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;
import int_.esa.eo.ngeo.downloadmanager.exception.WebServerServiceException;
import int_.esa.eo.ngeo.downloadmanager.http.UmSsoHttpRequestAndResponse;
import int_.esa.eo.ngeo.downloadmanager.webserver.NgeoWebServerServiceHelper;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntResp;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

public class DMRegistration {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMRegistration.class);
    private final String downloadManagerId, downloadManagerFriendlyName;
    private final URL registrationUrl;
    private final NgeoWebServerServiceHelper ngeoWebServerServiceHelper;
    
    public DMRegistration(URL registrationUrl, String downloadManagerId, String downloadManagerFriendlyName, NgeoWebServerServiceHelper ngeoWebServerServiceHelper) {
        this.registrationUrl = registrationUrl;
        this.downloadManagerId = downloadManagerId;
        this.downloadManagerFriendlyName = downloadManagerFriendlyName;
        this.ngeoWebServerServiceHelper = ngeoWebServerServiceHelper;
    }
    
    public String register() throws WebServerServiceException {
        LOGGER.info(String.format("Registering download manager with web server %s", registrationUrl));

        DMRegistrationMgmntRequ registrationMgmntRequest = ngeoWebServerServiceHelper.getRequestBuilder().buildDMRegistrationMgmntRequest(downloadManagerId, downloadManagerFriendlyName);
        UmSsoHttpRequestAndResponse webServerRequestAndResponse = null;
        try {
            webServerRequestAndResponse = ngeoWebServerServiceHelper.getService().registrationMgmt(registrationUrl, registrationMgmntRequest);
            UmssoHttpResponse response = webServerRequestAndResponse.getResponse();

            DMRegistrationMgmntResp registrationMgmtResponse = ngeoWebServerServiceHelper.getResponseParser().parseDMRegistrationMgmntResponse(registrationUrl, response);

            return registrationMgmtResponse.getMonitoringServiceUrl();
        } catch (ParseException | ServiceException e) {
            throw new WebServerServiceException(String.format("Exception occurred whilst attempting to register the Download Manager: %s", e.getLocalizedMessage()));
        } finally {
            if (webServerRequestAndResponse != null) {
                webServerRequestAndResponse.cleanupHttpResources();
            }
        }
    }
}
