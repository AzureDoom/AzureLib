package mod.azure.azurelib;

import mod.azure.azurelib.entities.TickingLightBlock;
import mod.azure.azurelib.entities.TickingLightEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class AzureLibMod implements ModInitializer {

	public static BlockEntityType<TickingLightEntity> TICKING_LIGHT_ENTITY;
	public static final TickingLightBlock TICKING_LIGHT_BLOCK = new TickingLightBlock();

	@Override
	public void onInitialize() {
		AzureLib.initialize();
		Registry.register(Registry.BLOCK, new ResourceLocation(AzureLib.MOD_ID, "lightblock"), TICKING_LIGHT_BLOCK);
		TICKING_LIGHT_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, AzureLib.MOD_ID + ":lightblock",
				FabricBlockEntityTypeBuilder.create(TickingLightEntity::new, TICKING_LIGHT_BLOCK).build(null));
	}
}
