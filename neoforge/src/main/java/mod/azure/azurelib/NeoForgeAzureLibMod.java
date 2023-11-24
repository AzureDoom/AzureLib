package mod.azure.azurelib;

import mod.azure.azurelib.config.TestingConfig;
import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.enchantments.IncendiaryEnchantment;
import mod.azure.azurelib.entities.TickingLightBlock;
import mod.azure.azurelib.entities.TickingLightEntity;
import mod.azure.azurelib.network.Networking;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(AzureLib.MOD_ID)
public final class NeoForgeAzureLibMod {
    public static NeoForgeAzureLibMod instance;

    public NeoForgeAzureLibMod(IEventBus modEventBus) {
        instance = this;
        AzureLib.initialize();
        AzureLibMod.config = AzureLibMod.registerConfig(TestingConfig.class, ConfigFormats.json()).getConfigInstance();
        modEventBus.addListener(this::init);
        AzureEnchantments.ENCHANTMENTS.register(modEventBus);
        AzureBlocks.BLOCKS.register(modEventBus);
        AzureEntities.TILE_TYPES.register(modEventBus);
    }

    private void init(FMLCommonSetupEvent event) {
        Networking.PacketRegistry.register();
        ConfigIO.FILE_WATCH_MANAGER.startService();
    }

    public class AzureEnchantments {
        public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(Registries.ENCHANTMENT, AzureLib.MOD_ID);
        public static final Supplier<Enchantment> INCENDIARYENCHANTMENT = ENCHANTMENTS.register("incendiaryenchantment", () -> new IncendiaryEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));
    }

    public class AzureBlocks {
        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, AzureLib.MOD_ID);

        public static final Supplier<TickingLightBlock> TICKING_LIGHT_BLOCK = BLOCKS.register("lightblock", () -> new TickingLightBlock());
    }

    public class AzureEntities {

        public static final DeferredRegister<BlockEntityType<?>> TILE_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AzureLib.MOD_ID);

        public static final Supplier<BlockEntityType<TickingLightEntity>> TICKING_LIGHT_ENTITY = TILE_TYPES.register("lightblock", () -> BlockEntityType.Builder.of(TickingLightEntity::new, AzureBlocks.TICKING_LIGHT_BLOCK.get()).build(null));
    }
}
