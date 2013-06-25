package int_.esa.eo.ngeo.dmtu.exception;


public class DownloadOperationException extends Exception {
	private static final long serialVersionUID = 7917867989020486916L;

	public DownloadOperationException(String message, Throwable cause) {
		super(message,cause);
	}

	public DownloadOperationException(Throwable cause) {
		super(cause);
	}

	public DownloadOperationException(String message) {
		super(message);
	}

}
