package int_.esa.eo.ngeo.downloadmanager.exception;


public class InvalidSettingValueException extends Exception {
    private static final long serialVersionUID = 7917867989020486916L;

    public InvalidSettingValueException(String message, Throwable cause) {
        super(message,cause);
    }

    public InvalidSettingValueException(Throwable cause) {
        super(cause);
    }

    public InvalidSettingValueException(String message) {
        super(message);
    }

}
