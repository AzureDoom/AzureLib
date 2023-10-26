package mod.azure.azurelib;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import mod.azure.azurelib.network.AzureLibNetwork;
import mod.azure.azurelib.cache.AzureLibCache;

/**
 * Base class for AzureLib!<br>
 * Hello World!<br>
 * There's not much to really see here, but feel free to stay a while and have a snack or something.
 * @see mod.azure.azurelib.util.AzureLibUtil
 */
public class AzureLib {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final Marker MAIN_MARKER = MarkerManager.getMarker("main");
	public static final String MOD_ID = "azurelib";
	public static volatile boolean hasInitialized;

	synchronized public static void initialize() {
		if (!hasInitialized) {
			DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AzureLibCache::registerReloadListener);
			AzureLibNetwork.init();
		}

		hasInitialized = true;
	}
}
