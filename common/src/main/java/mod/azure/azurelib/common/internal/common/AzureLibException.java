package mod.azure.azurelib.common.internal.common;

import net.minecraft.resources.ResourceLocation;

import java.io.Serial;

/**
 * Generic {@link Exception} wrapper for AzureLib.<br>
 * Mostly just serves as a marker for internal error handling.
 */
public class AzureLibException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public AzureLibException(ResourceLocation fileLocation, String message) {
        super(fileLocation + ": " + message);
    }

    public AzureLibException(String message, Throwable cause) {
        super(message, cause);
    }

    public AzureLibException(String message) {
        super(message);
    }

    public AzureLibException(Throwable cause) {
        super(cause);
    }

    public AzureLibException(ResourceLocation fileLocation, String message, Throwable cause) {
        super(fileLocation + ": " + message, cause);
    }
}
