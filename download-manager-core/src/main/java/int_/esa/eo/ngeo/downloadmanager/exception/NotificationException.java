package int_.esa.eo.ngeo.downloadmanager.exception;


public class NotificationException extends Exception {
    private static final long serialVersionUID = 7917867989020486916L;

    public NotificationException(String message, Throwable cause) {
        super(message,cause);
    }

    public NotificationException(Throwable cause) {
        super(cause);
    }

    public NotificationException(String message) {
        super(message);
    }

}
