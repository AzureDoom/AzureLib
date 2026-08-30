package mod.azure.azurelib.util;

/**
 * Generic {@link Exception} wrapper for AzureLib.<br>
 * Mostly just serves as a marker for internal error handling.
 */
public class AzureLibException extends RuntimeException {

    public AzureLibException(String message) {
        super(message);
    }

    public AzureLibException(String message, Throwable cause) {
        super(message, cause);
    }
}
