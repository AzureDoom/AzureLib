/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.common.internal.common.config.exception;

public class ConfigValueMissingException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -6063813873167943417L;

    public ConfigValueMissingException() {}

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
