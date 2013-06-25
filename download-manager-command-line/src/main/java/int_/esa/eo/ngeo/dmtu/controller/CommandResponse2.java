package int_.esa.eo.ngeo.dmtu.controller;

/**
 * XXX: This class is essentially a duplicate of one that is defined within the dmtu core project.
 */
public class CommandResponse2 {
	private /*final*/ boolean success;
	private /*final*/ String message;
	
	public CommandResponse2() {
		// Raison d'etre of this constructor is that Jackson's JSON to Java conversion depends on it.
	}
	
	public CommandResponse2(boolean success) {
		this(success, null);
	}
	
	public CommandResponse2(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}
}
