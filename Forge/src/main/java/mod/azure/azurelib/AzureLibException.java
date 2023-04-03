package mod.azure.azurelib;

import net.minecraft.util.ResourceLocation;

/**
 * Generic {@link Exception} wrapper for AzureLib.<br>
 * Mostly just serves as a marker for internal error handling.
 */
public class AzureLibException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AzureLibException(ResourceLocation fileLocation, String message) {
		super(fileLocation + ": " + message);
	}

	public AzureLibException(ResourceLocation fileLocation, String message, Throwable cause) {
		super(fileLocation + ": " + message, cause);
	}
}
