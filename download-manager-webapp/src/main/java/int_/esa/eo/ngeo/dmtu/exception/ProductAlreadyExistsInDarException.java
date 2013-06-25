package int_.esa.eo.ngeo.dmtu.exception;


public class ProductAlreadyExistsInDarException extends Exception {
	private static final long serialVersionUID = 7917867989020486916L;

	public ProductAlreadyExistsInDarException(String message, Throwable cause) {
		super(message,cause);
	}

	public ProductAlreadyExistsInDarException(Throwable cause) {
		super(cause);
	}

	public ProductAlreadyExistsInDarException(String message) {
		super(message);
	}

}
