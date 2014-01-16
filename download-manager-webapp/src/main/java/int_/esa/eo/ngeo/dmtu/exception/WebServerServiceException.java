package int_.esa.eo.ngeo.dmtu.exception;

import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;


public class WebServerServiceException extends ServiceException {
    private static final long serialVersionUID = 7917867989020486916L;

    public WebServerServiceException(String message, Throwable cause) {
        super(message,cause);
    }

    public WebServerServiceException(Throwable cause) {
        super(cause);
    }

    public WebServerServiceException(String message) {
        super(message);
    }

}
