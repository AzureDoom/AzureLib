package mod.azure.azurelib;

import net.fabricmc.api.ModInitializer;

public final class FabricAzureLibMod implements ModInitializer {

    @Override
    public void onInitialize() {
        AzureLib.initialize();
    }
}
