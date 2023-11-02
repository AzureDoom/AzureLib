package mod.azure.azurelib.common.internal.common.config.exception;

public class ConfigValueMissingException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6063813873167943417L;

    public ConfigValueMissingException() {
    }

    public ConfigValueMissingException(String message) {
        super(message);
    }

    public ConfigValueMissingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigValueMissingException(Throwable cause) {
        super(cause);
    }
}
