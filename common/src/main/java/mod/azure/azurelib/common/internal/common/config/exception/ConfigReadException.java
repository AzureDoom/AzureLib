package mod.azure.azurelib.common.internal.common.config.exception;

public class ConfigReadException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3140380119490334328L;

    public ConfigReadException() {
        super();
    }

    public ConfigReadException(String message) {
        super(message);
    }

    public ConfigReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigReadException(Throwable cause) {
        super(cause);
    }
}
