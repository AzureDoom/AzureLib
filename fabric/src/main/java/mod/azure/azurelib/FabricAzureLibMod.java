package mod.azure.azurelib;

import mod.azure.azurelib.config.TestingConfig;
import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.entities.TickingLightEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class FabricAzureLibMod implements ModInitializer {

	@Override
	public void onInitialize() {
		ConfigIO.FILE_WATCH_MANAGER.startService();
		AzureLib.initialize();
		Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(AzureLib.MOD_ID, "lightblock"), AzureLibMod.TICKING_LIGHT_BLOCK);
		AzureLibMod.TICKING_LIGHT_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, AzureLib.MOD_ID + ":lightblock", FabricBlockEntityTypeBuilder.create(TickingLightEntity::new, AzureLibMod.TICKING_LIGHT_BLOCK).build(null));
		AzureLibMod.config = AzureLibMod.registerConfig(TestingConfig.class, ConfigFormats.json()).getConfigInstance();
		ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
			ConfigIO.FILE_WATCH_MANAGER.stopService();
		});
	}
}
