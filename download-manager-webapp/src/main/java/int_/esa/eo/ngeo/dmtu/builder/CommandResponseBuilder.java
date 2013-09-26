package int_.esa.eo.ngeo.dmtu.builder;


public class CommandResponseBuilder {
	public CommandResponse buildCommandResponse(boolean success, String messageIfFailed) {
		CommandResponse response;
		if(success) {
			response = new CommandResponse(success);
		}else{
			response = new CommandResponse(success, messageIfFailed);
		}
		return response;
	}
}
