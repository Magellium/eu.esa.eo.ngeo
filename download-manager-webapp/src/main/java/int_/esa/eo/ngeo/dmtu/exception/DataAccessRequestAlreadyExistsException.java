package int_.esa.eo.ngeo.dmtu.exception;


public class DataAccessRequestAlreadyExistsException extends Exception {
	private static final long serialVersionUID = 7917867989020486916L;

	public DataAccessRequestAlreadyExistsException(String message, Throwable cause) {
		super(message,cause);
	}

	public DataAccessRequestAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	public DataAccessRequestAlreadyExistsException(String message) {
		super(message);
	}

}
