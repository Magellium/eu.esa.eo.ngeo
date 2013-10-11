package int_.esa.eo.ngeo.dmtu.os.command;

import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Delegates a command line command to the Operating System.
 */
@Component
public class CommandLineExecutor {
	
	private final Logger LOGGER = LoggerFactory.getLogger(CommandLineExecutor.class);

	public CommandResultHandler execute(CommandLine commandLine, long jobTimeoutMillis, boolean execInBackground, int successfulExitCode) throws IOException {

		Executor executor = new DefaultExecutor();
		executor.setExitValue(successfulExitCode); // Specifies the exitValue to be considered as the indicator of success

		ExecuteWatchdog watchdog = null;

		if (jobTimeoutMillis > 0) {
			watchdog = new ExecuteWatchdog(jobTimeoutMillis);
			executor.setWatchdog(watchdog);
		}

		CommandResultHandler resultHandler;
		if (execInBackground) {
			LOGGER.debug(String.format("[job] Executing non-blocking job \"%s\"", commandLine.toString()));
			resultHandler = new CommandResultHandler(watchdog);
			executor.execute(commandLine, resultHandler);       // Execute the OS command asynchronously
		} else {
			LOGGER.debug("[job] Executing blocking job...");
			int actualExitCode = executor.execute(commandLine); // Execute the OS command synchronously
			resultHandler = new CommandResultHandler(actualExitCode);
		}

		return resultHandler;
	}

	public class CommandResultHandler extends DefaultExecuteResultHandler {

		private ExecuteWatchdog watchdog;

		public CommandResultHandler(ExecuteWatchdog watchdog) {
			this.watchdog = watchdog;
		}

		/** Constructor to be called in the scenario where the job has already been executed in a blocking fashion. */
		public CommandResultHandler(int exitValue) {
			super.onProcessComplete(exitValue); // May look incorrect but is believed to be correct.
		}

		public void onProcessComplete(int exitValue) {
			super.onProcessComplete(exitValue);
			LOGGER.debug("[resultHandler] The job was successfully completed ...");
		}

		public void onProcessFailed(ExecuteException e) {
			super.onProcessFailed(e);
			if (watchdog != null && watchdog.killedProcess()) {
				LOGGER.error("[resultHandler] The job timed out");
			} else {
				LOGGER.error("[resultHandler] The job failed: " + e.getMessage());
			}
		}
	}

}