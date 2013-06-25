package int_.esa.eo.ngeo.dmtu.exception;


public class ParseException extends Exception {
	private static final long serialVersionUID = 7917867989020486916L;

	public ParseException(String message, Throwable cause) {
		super(message,cause);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}

	public ParseException(String message) {
		super(message);
	}

}
