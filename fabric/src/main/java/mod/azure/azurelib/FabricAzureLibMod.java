package mod.azure.azurelib;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import mod.azure.azurelib.config.TestingConfig;
import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.platform.FabricAzureLibNetwork;

public final class FabricAzureLibMod implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigIO.FILE_WATCH_MANAGER.startService();
        AzureLibMod.config = AzureLibMod.registerConfig(TestingConfig.class, ConfigFormats.json()).getConfigInstance();
        AzureLib.initialize();
        new FabricAzureLibNetwork();

        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> ConfigIO.FILE_WATCH_MANAGER.stopService());
    }
}
