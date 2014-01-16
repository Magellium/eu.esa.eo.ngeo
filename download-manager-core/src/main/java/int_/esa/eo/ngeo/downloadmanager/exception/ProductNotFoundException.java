package int_.esa.eo.ngeo.downloadmanager.exception;


public class ProductNotFoundException extends Exception {
    private static final long serialVersionUID = 7917867989020486916L;

    public ProductNotFoundException(String message, Throwable cause) {
        super(message,cause);
    }

    public ProductNotFoundException(Throwable cause) {
        super(cause);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }

}
