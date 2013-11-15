package int_.esa.eo.ngeo.downloadmanager.builder;

import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;


public class CommandResponseBuilder {
	public CommandResponse buildCommandResponse(boolean success, String errorMessage) {
		return buildCommandResponse(success, errorMessage, null);
	}

	public CommandResponse buildCommandResponse(boolean success, String errorMessage, String errorType) {
		CommandResponse response = new CommandResponse();
		response.setSuccess(success);
		if(!success) {
			response.setErrorMessage(errorMessage);
			response.setErrorType(errorType);
		}
		return response;
	}
}
