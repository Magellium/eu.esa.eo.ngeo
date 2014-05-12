package int_.esa.eo.ngeo.downloadmanager.plugin.csn.exception;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;

public class CsnException extends DMPluginException {
    private static final long serialVersionUID = 1771779827370020687L;

    public CsnException(String message) {
        super(message);
    }

    public CsnException(String message, Exception ex) {
        super(message, ex);
    }
}
