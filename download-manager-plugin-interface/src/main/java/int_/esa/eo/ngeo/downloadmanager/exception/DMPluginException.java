/**
 * 
 */
package int_.esa.eo.ngeo.downloadmanager.exception;

/**
 * @author Lewis Keen
 * This exception is used to indicate that an exception has occurred in a DM Plugin that cannot be 
 * rectified without further modification and restart of the application.
 * This includes unable to load configuration, unable to parse files received, etc.
 */
public class DMPluginException extends Exception {

	private static final long serialVersionUID = 7917867989020486916L;

	public DMPluginException(String message, Throwable cause) {
		super(message,cause);
	}

	public DMPluginException(Throwable cause) {
		super(cause);
	}

	public DMPluginException(String message) {
		super(message);
	}
	
}
