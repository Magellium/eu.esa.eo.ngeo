package int_.esa.eo.ngeo.downloadmanager.cli.exception;

public class CLICommandException extends RuntimeException {
    private static final long serialVersionUID = -2994334049489534412L;

    public CLICommandException(Throwable cause) {
        super(String.format("Unable to execute command, %s: %s", cause.getClass().getSimpleName(), cause.getMessage()), cause);
    }

    public CLICommandException(String message) {
        super(message);
    }
}
