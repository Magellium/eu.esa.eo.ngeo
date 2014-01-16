package int_.esa.eo.ngeo.downloadmanager.exception;


public class ServiceException extends Exception {
    private static final long serialVersionUID = 7917867989020486916L;

    public ServiceException(String message, Throwable cause) {
        super(message,cause);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message) {
        super(message);
    }

}
