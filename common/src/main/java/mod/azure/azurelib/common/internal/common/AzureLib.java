package mod.azure.azurelib.common.internal.common;

import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import mod.azure.azurelib.common.platform.Services;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Base class for AzureLib!<br>
 * Hello World!<br>
 * There's not much to really see here, but feel free to stay a while and have a snack or something.
 * @see AzureLibUtil
 */
public class AzureLib {
	public static final Logger LOGGER = LogManager.getLogger("azurelib");
	public static final Marker MAIN_MARKER = MarkerManager.getMarker("main");
	public static final String MOD_ID = "azurelib";
	public static boolean hasInitialized;

	public static void initialize() {
		if (!hasInitialized) {
			Services.INITIALIZER.initialize();
		}
		hasInitialized = true;
	}
}
