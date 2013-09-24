package int_.esa.eo.ngeo.downloadmanager.exception;

public class SchemaNotFoundException extends Exception {
	private static final long serialVersionUID = 7917867989020486916L;

	public SchemaNotFoundException(String message, Throwable cause) {
		super(message,cause);
	}

	public SchemaNotFoundException(Throwable cause) {
		super(cause);
	}

	public SchemaNotFoundException(String message) {
		super(message);
	}

}
