package mod.azure.azurelib;

import mod.azure.azurelib.entities.TickingLightBlock;
import mod.azure.azurelib.entities.TickingLightEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber
@Mod(AzureLib.MOD_ID)
public final class AzureLibMod {

	public static AzureLibMod instance;

	public AzureLibMod() {
		instance = this;
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		AzureLib.initialize();
		AzureBlocks.BLOCKS.register(modEventBus);
		AzureEntities.TILE_TYPES.register(modEventBus);
	}

	public class AzureBlocks {
		public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AzureLib.MOD_ID);

		public static final RegistryObject<Block> TICKING_LIGHT_BLOCK = BLOCKS.register("lightblock", () -> new TickingLightBlock());
	}

	public class AzureEntities {

		public static final DeferredRegister<BlockEntityType<?>> TILE_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AzureLib.MOD_ID);

		public static final RegistryObject<BlockEntityType<TickingLightEntity>> TICKING_LIGHT_ENTITY = TILE_TYPES.register("lightblock", () -> BlockEntityType.Builder.of(TickingLightEntity::new, AzureBlocks.TICKING_LIGHT_BLOCK.get()).build(null));
	}
}
