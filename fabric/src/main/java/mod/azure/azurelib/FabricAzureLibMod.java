package mod.azure.azurelib;

import mod.azure.azurelib.config.AzureLibConfig;
import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.enchantments.IncendiaryEnchantment;
import mod.azure.azurelib.entities.TickingLightBlock;
import mod.azure.azurelib.entities.TickingLightEntity;
import mod.azure.azurelib.platform.FabricAzureLibNetwork;
import mod.azure.azurelib.rewrite.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.testing.armor.DoomicornArmor;
import mod.azure.azurelib.testing.block.StargateBlock;
import mod.azure.azurelib.testing.block.be.StargateBlockEntity;
import mod.azure.azurelib.testing.entity.MarauderEntity;
import mod.azure.azurelib.testing.item.PistolItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class FabricAzureLibMod implements ModInitializer {
    public static BlockEntityType<TickingLightEntity> TICKING_LIGHT_ENTITY;
    public static BlockEntityType<StargateBlockEntity> STARGATE_BLOCK_ENTITY;
    public static final TickingLightBlock TICKING_LIGHT_BLOCK = new TickingLightBlock();
    public static final StargateBlock STARGATE_BLOCK = new StargateBlock();
    public static final Enchantment INCENDIARYENCHANTMENT = new IncendiaryEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND);
    public static final EntityType<MarauderEntity> MARAUDER = mob("marauder", MarauderEntity::new, 1.5F, 2.5F);

    public static final Item PISTOL_ITEM = new PistolItem();
    public static final Item DOOMICORN_HELMET = new DoomicornArmor(ArmorItem.Type.HELMET);
    public static final Item DOOMICORN_CHESTPLATE = new DoomicornArmor(ArmorItem.Type.CHESTPLATE);
    public static final Item DOOMICORN_LEGGINGS = new DoomicornArmor(ArmorItem.Type.LEGGINGS);
    public static final Item DOOMICORN_BOOTS = new DoomicornArmor(ArmorItem.Type.BOOTS);

    @Override
    public void onInitialize() {
        ConfigIO.FILE_WATCH_MANAGER.startService();
        AzureLibMod.config = AzureLibMod.registerConfig(AzureLibConfig.class, ConfigFormats.json()).getConfigInstance();
        AzureLib.initialize();
        new FabricAzureLibNetwork();

        Registry.register(BuiltInRegistries.BLOCK, AzureLib.modResource("lightblock"), FabricAzureLibMod.TICKING_LIGHT_BLOCK);
        FabricAzureLibMod.TICKING_LIGHT_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, AzureLib.MOD_ID + ":lightblock", FabricBlockEntityTypeBuilder.create(TickingLightEntity::new, FabricAzureLibMod.TICKING_LIGHT_BLOCK).build(null));
        Registry.register(BuiltInRegistries.BLOCK, AzureLib.modResource("stargate"), FabricAzureLibMod.STARGATE_BLOCK);
        FabricAzureLibMod.STARGATE_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, AzureLib.MOD_ID + ":stargate", FabricBlockEntityTypeBuilder.create(StargateBlockEntity::new, FabricAzureLibMod.STARGATE_BLOCK).build(null));
        FabricDefaultAttributeRegistry.register(
                MARAUDER,
                Monster.createMonsterAttributes()
        );

        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> ConfigIO.FILE_WATCH_MANAGER.stopService());
        if (AzureLibMod.config.useIncendiaryEnchantment)
            Registry.register(BuiltInRegistries.ENCHANTMENT, AzureLib.modResource("incendiaryenchantment"), INCENDIARYENCHANTMENT);

        Registry.register(BuiltInRegistries.ITEM, AzureLib.modResource("pistol"), PISTOL_ITEM);
        Registry.register(BuiltInRegistries.ITEM, AzureLib.modResource("doomicorn_helmet"), DOOMICORN_HELMET);
        Registry.register(BuiltInRegistries.ITEM, AzureLib.modResource("doomicorn_chestplate"), DOOMICORN_CHESTPLATE);
        Registry.register(BuiltInRegistries.ITEM, AzureLib.modResource("doomicorn_leggings"), DOOMICORN_LEGGINGS);
        Registry.register(BuiltInRegistries.ITEM, AzureLib.modResource("doomicorn_boots"), DOOMICORN_BOOTS);

        AzIdentityRegistry.register(
                PISTOL_ITEM,
                DOOMICORN_HELMET,
                DOOMICORN_CHESTPLATE,
                DOOMICORN_LEGGINGS,
                DOOMICORN_BOOTS
        );
    }

    private static <T extends Entity> EntityType<T> mob(String id, EntityType.EntityFactory<T> factory, float height, float width) {
        final var type = FabricEntityTypeBuilder.create(MobCategory.MONSTER, factory).dimensions(
                EntityDimensions.scalable(height, width)).fireImmune().trackedUpdateRate(1).trackRangeBlocks(
                90).build();
        Registry.register(BuiltInRegistries.ENTITY_TYPE, AzureLib.modResource(id), type);

        return type;
    }
}
