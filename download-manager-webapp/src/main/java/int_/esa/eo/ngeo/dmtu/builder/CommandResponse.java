package int_.esa.eo.ngeo.dmtu.builder;

public class CommandResponse {
	private final boolean success;
	private final String message;
	
	public CommandResponse(boolean success) {
		this(success, "");
	}
	
	public CommandResponse(boolean success, String message) {
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
