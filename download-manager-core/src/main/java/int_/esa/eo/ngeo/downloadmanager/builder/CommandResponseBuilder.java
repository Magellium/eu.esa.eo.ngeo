package int_.esa.eo.ngeo.downloadmanager.builder;

import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithDarDetails;

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

    public CommandResponseWithDarDetails buildCommandResponseWithDarUuid(String darUuid, String errorMessage) {
        return buildCommandResponseWithDarUuid(darUuid, errorMessage, null);
    }

    public CommandResponseWithDarDetails buildCommandResponseWithDarUuid(String darUuid, String errorMessage, String errorType) {
        return buildCommandResponseWithDarAndProductUuid(darUuid, null, errorMessage, errorType);
    }

    public CommandResponseWithDarDetails buildCommandResponseWithDarAndProductUuid(String darUuid, String productUuid, String errorMessage) {
        return buildCommandResponseWithDarAndProductUuid(darUuid, productUuid, errorMessage, null);
    }

    public CommandResponseWithDarDetails buildCommandResponseWithDarAndProductUuid(String darUuid, String productUuid, String errorMessage, String errorType) {
        CommandResponseWithDarDetails response = new CommandResponseWithDarDetails();
        if(StringUtils.isNotEmpty(darUuid)) {
            response.setSuccess(true);
            response.setDarUuid(darUuid);
            if(StringUtils.isNotEmpty(productUuid)) {
                response.setProductUuid(productUuid);
            }
        }else{
            response.setSuccess(false);
            if(StringUtils.isNotEmpty(errorMessage)) {
                response.setErrorMessage(errorMessage);
            }else{
                response.setErrorMessage("Unable to provide product UUID for added product.");
            }
            response.setErrorType(errorType);
        }
        return response;
    }
}
