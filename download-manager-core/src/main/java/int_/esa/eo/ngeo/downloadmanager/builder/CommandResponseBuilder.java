package int_.esa.eo.ngeo.downloadmanager.builder;

import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithDarUuid;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithProductUuid;

import org.apache.commons.lang.StringUtils;


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
	
	public CommandResponseWithProductUuid buildCommandResponseWithProductUuid(String productUuid, String errorMessage) {
		return buildCommandResponseWithProductUuid(productUuid, errorMessage, null);
	}

	public CommandResponseWithProductUuid buildCommandResponseWithProductUuid(String productUuid, String errorMessage, String errorType) {
		CommandResponseWithProductUuid response = new CommandResponseWithProductUuid();
		if(StringUtils.isNotEmpty(productUuid)) {
			response.setSuccess(true);
			response.setProductUuid(productUuid);
		}else{
			response.setSuccess(false);
			response.setErrorMessage("Unable to provide product UUID for added product.");
			response.setErrorType(errorType);
		}
		return response;
	}

	public CommandResponseWithDarUuid buildCommandResponseWithDarUuid(String darUuid, String errorMessage) {
		return buildCommandResponseWithDarUuid(darUuid, errorMessage, null);
	}

	public CommandResponseWithDarUuid buildCommandResponseWithDarUuid(String darUuid, String errorMessage, String errorType) {
		CommandResponseWithDarUuid response = new CommandResponseWithDarUuid();
		if(StringUtils.isNotEmpty(darUuid)) {
			response.setSuccess(true);
			response.setDarUuid(darUuid);
		}else{
			response.setSuccess(false);
			response.setErrorMessage("Unable to provide product UUID for added product.");
			response.setErrorType(errorType);
		}
		return response;
	}
}
