package int_.esa.eo.ngeo.downloadmanager.builder;

import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;


public class CommandResponseBuilder {
	public CommandResponse buildCommandResponse(boolean success, String messageIfFailed) {
		CommandResponse response = new CommandResponse();
		response.setSuccess(success);
		if(!success) {
			response.setMessage(messageIfFailed);
		}
		return response;
	}
}
