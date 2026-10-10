package mod.azure.azurelib.client;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.util.client.AzRenderTick;

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
        // 1.7.10 fires tick events on FML's bus, not on MinecraftForge.EVENT_BUS.
        FMLCommonHandler.instance().bus().register(new AzRenderTick());
    }
}
