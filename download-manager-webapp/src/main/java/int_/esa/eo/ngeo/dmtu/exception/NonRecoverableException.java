/**
 * 
 */
package int_.esa.eo.ngeo.dmtu.exception;

/**
 * @author Lewis Keen
 * This exception is used to indicate that an exception has occurred that cannot be 
 * rectified without further modification and restart of the application.
 * This includes unable to load configuration, unable to parse files received, etc.
 */
public class NonRecoverableException extends RuntimeException {

	private static final long serialVersionUID = 7917867989020486916L;

	public NonRecoverableException(String message, Throwable cause) {
		super(message,cause);
	}

	public NonRecoverableException(Throwable cause) {
		super(cause);
	}

	public NonRecoverableException(String message) {
		super(message);
	}
	
}
