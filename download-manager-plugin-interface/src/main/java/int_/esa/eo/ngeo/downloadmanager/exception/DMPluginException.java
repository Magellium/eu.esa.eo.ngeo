package int_.esa.eo.ngeo.downloadmanager.exception;

/**
 * This exception is used to indicate that an exception has occurred in a
 * Download Manager Plugin that cannot be rectified without further modification
 * and restart of the application. This includes unable to load configuration,
 * unable to parse files received, etc.
 */
public class DMPluginException extends Exception {

	private static final long serialVersionUID = 7917867989020486916L;

	/**
	 * Constructs an DMPluginException with the specified detail message and
	 * cause.
	 * 
	 * @param message
	 *            the detail message
	 * @param cause
	 *            the cause of the exception
	 */
	public DMPluginException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs an DMPluginException with the specified cause.
	 * 
	 * @param cause
	 *            the cause of the exception
	 */
	public DMPluginException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs an DMPluginException with the specified detail message.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public DMPluginException(String message) {
		super(message);
	}

}
