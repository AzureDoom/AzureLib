package mod.azure.azurelib.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CreativeModeTabs;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibMod;
import mod.azure.azurelib.common.internal.common.config.AzureLibConfig;
import mod.azure.azurelib.common.internal.common.config.format.ConfigFormats;
import mod.azure.azurelib.common.internal.common.config.io.ConfigIO;
import mod.azure.azurelib.common.internal.common.network.packet.*;
import mod.azure.azurelib.fabric.platform.FabricAzureLibNetwork;
import mod.azure.azurelib.rewrite.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.rewrite.testing.Registry;
import mod.azure.azurelib.sblforked.SBLConstants;

public final class FabricAzureLibMod implements ModInitializer {

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
        FabricDefaultAttributeRegistry.register(
            Registry.MUTANT_ZOMBIE.get(),
            Monster.createMonsterAttributes()
        );
        AzIdentityRegistry.register(
            Registry.WOLF_HELMET.get(),
            Registry.WOLF_CHESTPLATE.get(),
            Registry.WOLF_LEGGINGS.get(),
            Registry.WOLF_BOOTS.get()
        );
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS)
            .register(event -> event.prepend(Registry.MUTANT_ZOMBIE_SPAWN_EGG.get()));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(event -> {
            event.prepend(Registry.WOLF_HELMET.get());
            event.prepend(Registry.WOLF_CHESTPLATE.get());
            event.prepend(Registry.WOLF_LEGGINGS.get());
            event.prepend(Registry.WOLF_BOOTS.get());
        });
    }
}
