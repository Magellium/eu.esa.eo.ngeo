package int_.esa.eo.ngeo.downloadmanager.cli.exception;

import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;

public class ResourceNotFoundException extends ServiceException {
    private static final long serialVersionUID = -2994334049489534412L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
