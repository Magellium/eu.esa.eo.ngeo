package int_.esa.eo.ngeo.downloadmanager.exception;


public class DownloadProcessCreationException extends Exception {
    private static final long serialVersionUID = 7917867989020486916L;

    public DownloadProcessCreationException(String message, Throwable cause) {
        super(message,cause);
    }

    public DownloadProcessCreationException(Throwable cause) {
        super(cause);
    }

    public DownloadProcessCreationException(String message) {
        super(message);
    }

}
