package mod.azure.azurelib.client;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mod.azure.azurelib.cache.AzureLibCache;

/**
 * Client-side bootstrap, invoked from {@link mod.azure.azurelib.ForgeAzureLibMod} during pre-init so the resource
 * reload listener is in place before the first resource reload loads models and animations.
 */
@SideOnly(Side.CLIENT)
public final class AzureLibClientInit {

    private static boolean initialized;

    private AzureLibClientInit() {}

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        AzureLibCache.registerReloadListener();
    }
}
