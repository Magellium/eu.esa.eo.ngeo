package int_.esa.eo.ngeo.dmtu.callback;

import int_.esa.eo.ngeo.dmtu.os.command.CommandLineExecutor;
import int_.esa.eo.ngeo.dmtu.os.command.CommandLineExecutor.CommandResultHandler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallbackCommandExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(CallbackCommandExecutor.class);

	private static final String FILESET_MACRO_REF = "${FILESET}";
	private static final String FILE_MACRO_REF    = "${FILE}";

	private static final boolean EXEC_IN_BACKGROUND = true;

	private static final int SUCCESSFUL_OS_COMMAND_EXIT_CODE = 0;

	public void invokeCallbackCommandOnProductFiles(String unresolvedCommand, String completedDownloadPathAsString) {
		if(completedDownloadPathAsString != null) {
			File completedDownloadPath = new File(completedDownloadPathAsString);
			File[] downloadedFiles = new File[]{completedDownloadPath};
			
			invokeCallbackCommandOnProductFiles(unresolvedCommand, downloadedFiles);
		}
	}
	
	public void invokeCallbackCommandOnProductFiles(String unresolvedCommand, File[] downloadedFiles) {
		if (unresolvedCommand == null || unresolvedCommand.isEmpty()) {
			LOGGER.debug("No post-download call-back command has been defined");
			return; // Nothing to do
		}
		if (unresolvedCommand.contains(FILESET_MACRO_REF)) {
			// TODO: Execute the command once, passing in each and every pathname. (How should the pathnames be separated?)
			throw new UnsupportedOperationException(String.format("TODO: Execute the command \"%s\" once, replacing %s with each and every pathname. (How should the pathnames be separated?)",
					unresolvedCommand, FILESET_MACRO_REF));
		} else if(unresolvedCommand.contains(FILE_MACRO_REF)) {
			for (File file : downloadedFiles) {
				LOGGER.debug(String.format("Unresolved call-back command = %s", unresolvedCommand));

				CommandLine commandLine = new CommandLine(getOSShellName()); // FIXME: Specification of the shell wrapper needs to cope with MacOS and Linux/Unix (and maybe Win95...)
				commandLine.addArgument("/c");
				commandLine.addArgument(unresolvedCommand);

				// build up the command line using a 'java.io.File'
				Map<String, Object> map = new HashMap<>();
				map.put("FILE", file);

				commandLine.setSubstitutionMap(map);
				
				LOGGER.debug(String.format("resolved call-back command: %s", commandLine.toString()));
				CommandLineExecutor commandLineExecutor = new CommandLineExecutor();
				CommandResultHandler resultHandler = null;
				try {
					resultHandler = commandLineExecutor.execute(commandLine, 60000, EXEC_IN_BACKGROUND, SUCCESSFUL_OS_COMMAND_EXIT_CODE);
					resultHandler.waitFor();
				} catch (InterruptedException | IOException e) {
					LOGGER.error(String.format("Error invoking command \"%s\": %s", commandLine.toString(), e.getMessage()));
				}
				
				LOGGER.debug(String.format("Process exit code for command \"%s\" = %s", commandLine.toString(), resultHandler.getExitValue()));
			}			
		} 
	}

	public String getOSShellName() {		
		String osName = System.getProperty("os.name");
		return osName.toLowerCase().startsWith("windows") ? "cmd.exe" : "bash";
	}
}
