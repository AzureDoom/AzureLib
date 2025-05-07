package mod.azure.azurelib;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import mod.azure.azurelib.config.AzureLibConfig;
import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.enchantments.IncendiaryEnchantment;
import mod.azure.azurelib.entities.TickingLightBlock;
import mod.azure.azurelib.entities.TickingLightEntity;
import mod.azure.azurelib.network.Networking;
import mod.azure.azurelib.rewrite.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.testing.armor.DoomicornArmor;
import mod.azure.azurelib.testing.block.StargateBlock;
import mod.azure.azurelib.testing.block.be.StargateBlockEntity;
import mod.azure.azurelib.testing.entity.MarauderEntity;
import mod.azure.azurelib.testing.item.PistolItem;

@Mod.EventBusSubscriber
@Mod(AzureLib.MOD_ID)
public final class NeoForgeAzureLibMod {

    public static NeoForgeAzureLibMod instance;

    public NeoForgeAzureLibMod() {
        instance = this;
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        AzureLib.initialize();
        AzureLibMod.config = AzureLibMod.registerConfig(AzureLibConfig.class, ConfigFormats.json()).getConfigInstance();
        modEventBus.addListener(this::init);
        if (AzureLibMod.config.useIncendiaryEnchantment)
            AzureEnchantments.ENCHANTMENTS.register(modEventBus);
        AzureBlocks.BLOCKS.register(modEventBus);
        AzureEntities.TILE_TYPES.register(modEventBus);
        AzureItems.ITEMS.register(modEventBus);
        modEventBus.addListener(this::createEntityAttributes);
        modEventBus.addListener(this::commonSetup);
    }

    private void init(FMLCommonSetupEvent event) {
        Networking.PacketRegistry.register();
        ConfigIO.FILE_WATCH_MANAGER.startService();
    }

    public class AzureEnchantments {

        public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(
            ForgeRegistries.ENCHANTMENTS,
            AzureLib.MOD_ID
        );

        public static final RegistryObject<Enchantment> INCENDIARYENCHANTMENT = ENCHANTMENTS.register(
            "incendiaryenchantment",
            () -> new IncendiaryEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND)
        );
    }

    public class AzureBlocks {

        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            ForgeRegistries.BLOCKS,
            AzureLib.MOD_ID
        );

        public static final RegistryObject<Block> TICKING_LIGHT_BLOCK = BLOCKS.register(
            "lightblock",
            TickingLightBlock::new
        );

        public static final RegistryObject<Block> STARGATE_BLOCK = BLOCKS.register("stargate", StargateBlock::new);
    }

    public class AzureEntities {

        public static final DeferredRegister<BlockEntityType<?>> TILE_TYPES = DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITY_TYPES,
            AzureLib.MOD_ID
        );

        public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
            ForgeRegistries.ENTITY_TYPES,
            AzureLib.MOD_ID
        );

        public static final RegistryObject<BlockEntityType<TickingLightEntity>> TICKING_LIGHT_ENTITY = TILE_TYPES
            .register(
                "lightblock",
                () -> BlockEntityType.Builder.of(TickingLightEntity::new, AzureBlocks.TICKING_LIGHT_BLOCK.get())
                    .build(null)
            );

        public static final RegistryObject<BlockEntityType<StargateBlockEntity>> STARGATE_BLOCK_ENTITY = TILE_TYPES
            .register(
                "stargate",
                () -> BlockEntityType.Builder.of(StargateBlockEntity::new, AzureBlocks.STARGATE_BLOCK.get()).build(null)
            );

        public static final RegistryObject<EntityType<MarauderEntity>> MARAUDER = ENTITY_TYPES.register(
            "maruader",
            () -> EntityType.Builder.of(MarauderEntity::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(8)
                .build(AzureLib.MOD_ID + ":marauder")
        );
    }

    public class AzureItems {

        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            ForgeRegistries.ITEMS,
            AzureLib.MOD_ID
        );

        public static final RegistryObject<Item> PISTOL_ITEM = ITEMS.register("pistol", () -> new PistolItem());

        public static final RegistryObject<Item> DOOMICORN_HELMET = ITEMS.register(
            "doomicorn_helmet",
            () -> new DoomicornArmor(
                ArmorItem.Type.HELMET
            )
        );

        public static final RegistryObject<Item> DOOMICORN_CHESTPLATE = ITEMS.register(
            "doomicorn_chestplate",
            () -> new DoomicornArmor(
                ArmorItem.Type.CHESTPLATE
            )
        );

        public static final RegistryObject<Item> DOOMICORN_LEGGINGS = ITEMS.register(
            "doomicorn_leggings",
            () -> new DoomicornArmor(
                ArmorItem.Type.LEGGINGS
            )
        );

        public static final RegistryObject<Item> DOOMICORN_BOOTS = ITEMS.register(
            "doomicorn_boots",
            () -> new DoomicornArmor(
                ArmorItem.Type.BOOTS
            )
        );
    }

    public void createEntityAttributes(final EntityAttributeCreationEvent event) {
        event.put(AzureEntities.MARAUDER.get(), Monster.createMonsterAttributes().build());
    }

    public void commonSetup(final FMLCommonSetupEvent event) {
        AzIdentityRegistry.register(
            AzureItems.PISTOL_ITEM.get(),
            AzureItems.DOOMICORN_HELMET.get(),
            AzureItems.DOOMICORN_CHESTPLATE.get(),
            AzureItems.DOOMICORN_LEGGINGS.get(),
            AzureItems.DOOMICORN_BOOTS.get()
        );
    }
}
