package mod.azure.azurelib.client;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.hitbox.client.AzHitboxClientHooks;

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
        MinecraftForge.EVENT_BUS.register(AzHitboxClientHooks.class);
    }
}
