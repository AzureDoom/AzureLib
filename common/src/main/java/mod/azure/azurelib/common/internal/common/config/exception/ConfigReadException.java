/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
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
