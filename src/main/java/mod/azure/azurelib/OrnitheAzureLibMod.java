package mod.azure.azurelib;

import net.ornithemc.osl.entrypoints.api.ModInitializer;

public final class OrnitheAzureLibMod implements ModInitializer {

    @Override
    public void init() {
        AzureLib.initialize();
    }
}
