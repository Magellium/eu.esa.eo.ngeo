package int_.esa.eo.ngeo.downloadmanager.exception;


public class NoPluginAvailableException extends Exception {
	private static final long serialVersionUID = 7917867989020486916L;

	public NoPluginAvailableException(String message, Throwable cause) {
		super(message,cause);
	}

	public NoPluginAvailableException(Throwable cause) {
		super(cause);
	}

	public NoPluginAvailableException(String message) {
		super(message);
	}

}
