package int_.esa.eo.ngeo.downloadmanager.cli.commands;

import int_.esa.eo.ngeo.downloadmanager.cli.exception.CLICommandException;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;

import org.apache.commons.lang.StringUtils;

public abstract class ActionWithCommandResponse {
    public String getMessageFromCommandResponse(CommandResponse commandResponse, String successMessage) {
        if (commandResponse != null && commandResponse.isSuccess()) {
            return successMessage;
        } else {
            throw createCommandExceptionUnsuccessfulCommandResponse(commandResponse);
        }
    }
    
    public CLICommandException createCommandExceptionUnsuccessfulCommandResponse(CommandResponse commandResponse) {
        if (commandResponse != null && StringUtils.isNotEmpty(commandResponse.getErrorMessage())) {
            return new CLICommandException(String.format("Error from Download Manager: %s", commandResponse.getErrorMessage()));
        } else {
            return new CLICommandException("Error received from Download Manager, no message provided.");
        }
    }
}
