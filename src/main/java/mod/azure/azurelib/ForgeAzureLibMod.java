package mod.azure.azurelib;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Forge entry point for AzureLib on Minecraft 1.12.2.
 */
@Mod(
    modid = AzureLib.MOD_ID,
    name = AzureLib.MOD_NAME,
    useMetadata = true,
    acceptedMinecraftVersions = "[1.12.2]"
)
public final class ForgeAzureLibMod {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        AzureLib.initialize();
        if (event.getSide().isClient()) {
            // Kept in a separate class so that no client-only class is touched on a dedicated server.
            mod.azure.azurelib.client.AzureLibClientInit.init();
        }
    }
}
