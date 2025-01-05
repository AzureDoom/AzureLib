package mod.azure.azurelib.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibMod;
import mod.azure.azurelib.common.internal.common.config.AzureLibConfig;
import mod.azure.azurelib.common.internal.common.config.format.ConfigFormats;
import mod.azure.azurelib.common.internal.common.config.io.ConfigIO;
import mod.azure.azurelib.common.internal.common.network.packet.*;
import mod.azure.azurelib.core2.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.fabric.core2.example.ExampleEntityTypes;
import mod.azure.azurelib.fabric.core2.example.armors.AzDoomArmor;
import mod.azure.azurelib.fabric.core2.example.blocks.Stargate;
import mod.azure.azurelib.fabric.core2.example.items.AzPistol;
import mod.azure.azurelib.fabric.platform.FabricAzureLibNetwork;
import mod.azure.azurelib.sblforked.SBLConstants;

public final class FabricAzureLibMod implements ModInitializer {

    public static final Block STARGATE = new Stargate(
        BlockBehaviour.Properties.of().sound(SoundType.DRIPSTONE_BLOCK).strength(5.0f, 8.0f).noOcclusion()
    );

    public static final Item AZ_PISTOL = new AzPistol();

    public static final Item AZ_DOOM_HELMET = new AzDoomArmor(ArmorItem.Type.HELMET);

    public static final Item AZ_DOOM_CHESTPLATE = new AzDoomArmor(ArmorItem.Type.CHESTPLATE);

    public static final Item AZ_DOOM_LEGGINGS = new AzDoomArmor(ArmorItem.Type.LEGGINGS);

    public static final Item AZ_DOOM_BOOTS = new AzDoomArmor(ArmorItem.Type.BOOTS);

    @Override
    public void onInitialize() {
        SBLConstants.SBL_LOADER.init(null);
        ConfigIO.FILE_WATCH_MANAGER.startService();
        AzureLib.initialize();
        AzureLibMod.initRegistry();
        new FabricAzureLibNetwork();
        AzureLibMod.config = AzureLibMod.registerConfig(AzureLibConfig.class, ConfigFormats.json()).getConfigInstance();
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> ConfigIO.FILE_WATCH_MANAGER.stopService());
        PayloadTypeRegistry.playS2C().register(BlockEntityAnimTriggerPacket.TYPE, BlockEntityAnimTriggerPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(BlockEntityAnimDataSyncPacket.TYPE, BlockEntityAnimDataSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(EntityAnimTriggerPacket.TYPE, EntityAnimTriggerPacket.CODEC);
        PayloadTypeRegistry.playS2C()
            .register(AzBlockEntityDispatchCommandPacket.TYPE, AzBlockEntityDispatchCommandPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(AzEntityDispatchCommandPacket.TYPE, AzEntityDispatchCommandPacket.CODEC);
        PayloadTypeRegistry.playS2C()
            .register(AzItemStackDispatchCommandPacket.TYPE, AzItemStackDispatchCommandPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(EntityAnimDataSyncPacket.TYPE, EntityAnimDataSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(AnimTriggerPacket.TYPE, AnimTriggerPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(AnimDataSyncPacket.TYPE, AnimDataSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SendConfigDataPacket.TYPE, SendConfigDataPacket.CODEC);
        Registry.register(BuiltInRegistries.BLOCK, AzureLib.modResource("stargate"), STARGATE);
        Registry.register(
            BuiltInRegistries.ITEM,
            AzureLib.modResource("stargate"),
            new BlockItem(STARGATE, new Item.Properties())
        );
        Registry.register(
            BuiltInRegistries.ITEM,
            AzureLib.modResource("az_pistol"),
            AZ_PISTOL
        );
        Registry.register(
            BuiltInRegistries.ITEM,
            AzureLib.modResource("az_doomicorn_helmet"),
            AZ_DOOM_HELMET
        );
        Registry.register(
            BuiltInRegistries.ITEM,
            AzureLib.modResource("az_doomicorn_chestplate"),
            AZ_DOOM_CHESTPLATE
        );
        Registry.register(
            BuiltInRegistries.ITEM,
            AzureLib.modResource("az_doomicorn_leggings"),
            AZ_DOOM_LEGGINGS
        );
        Registry.register(
            BuiltInRegistries.ITEM,
            AzureLib.modResource("az_doomicorn_boots"),
            AZ_DOOM_BOOTS
        );
        ExampleEntityTypes.initialize();
        AzIdentityRegistry.register(AZ_DOOM_HELMET, AZ_DOOM_CHESTPLATE, AZ_DOOM_LEGGINGS, AZ_DOOM_BOOTS, AZ_PISTOL);
    }
}
