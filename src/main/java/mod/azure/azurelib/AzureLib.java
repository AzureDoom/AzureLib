package mod.azure.azurelib;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import mod.azure.azurelib.platform.Services;

/**
 * Base class for AzureLib!<br>
 * Hello World!<br>
 * There's little to really see here, but feel free to stay a while and have a snack or something.
 *
 * @see mod.azure.azurelib.util.AzureLibUtil
 */
public class AzureLib {

    public static final Logger LOGGER = LogManager.getLogger("azurelib");

    public static final Marker MAIN_MARKER = MarkerManager.getMarker("main");

    public static final String MOD_ID = "azurelib";

    public static final String MOD_NAME = "AzureLib";

    public static final String ITEM_UUID_TAG = "az_id";

    public static boolean hasInitialized;

    private AzureLib() {}

    /**
     * Sets up AzureLib's common systems (networking). Called by {@link ForgeAzureLibMod} during pre-init; safe to call
     * more than once.
     */
    public static void initialize() {
        if (!hasInitialized) {
            Services.INITIALIZER.initialize();
        }
        hasInitialized = true;
    }

    public static ResourceLocation modResource(String name) {
        return new ResourceLocation(MOD_ID, name);
    }
}
