package mod.azure.azurelib.neoforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibMod;
import mod.azure.azurelib.common.internal.common.config.AzureLibConfig;
import mod.azure.azurelib.common.internal.common.config.format.ConfigFormats;
import mod.azure.azurelib.common.internal.common.config.io.ConfigIO;
import mod.azure.azurelib.common.internal.common.network.packet.*;
import mod.azure.azurelib.sblforked.SBLConstants;

@Mod(AzureLib.MOD_ID)
public final class NeoForgeAzureLibMod {

    public static DeferredRegister<BlockEntityType<?>> blockEntityTypeDeferredRegister = DeferredRegister.create(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        AzureLib.MOD_ID
    );

    public static DeferredRegister<Block> blockDeferredRegister = DeferredRegister.create(
        BuiltInRegistries.BLOCK,
        AzureLib.MOD_ID
    );

    public static final DeferredRegister.DataComponents DATA_COMPONENTS_REGISTER = DeferredRegister
        .createDataComponents(
            Registries.DATA_COMPONENT_TYPE,
            AzureLib.MOD_ID
        );

    public NeoForgeAzureLibMod(IEventBus modEventBus) {
        AzureLib.initialize();
        AzureLibMod.initRegistry();
        DATA_COMPONENTS_REGISTER.register(modEventBus);
        blockEntityTypeDeferredRegister.register(modEventBus);
        blockDeferredRegister.register(modEventBus);
        AzureLibMod.config = AzureLibMod.registerConfig(AzureLibConfig.class, ConfigFormats.json()).getConfigInstance();
        modEventBus.addListener(this::init);
        modEventBus.addListener(this::registerMessages);
        SBLConstants.SBL_LOADER.init(modEventBus);
    }

    private void init(final FMLCommonSetupEvent event) {
        ConfigIO.FILE_WATCH_MANAGER.startService();
    }

    public void registerMessages(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(AzureLib.MOD_ID);

        registrar.playBidirectional(
            BlockEntityAnimTriggerPacket.TYPE,
            BlockEntityAnimTriggerPacket.CODEC,
            (msg, ctx) -> msg.handle()
        );
        registrar.playBidirectional(
            BlockEntityAnimDataSyncPacket.TYPE,
            BlockEntityAnimDataSyncPacket.CODEC,
            (msg, ctx) -> msg.handle()
        );
        registrar.playBidirectional(
            EntityAnimTriggerPacket.TYPE,
            EntityAnimTriggerPacket.CODEC,
            (msg, ctx) -> msg.handle()
        );
        registrar.playBidirectional(
            AzEntityDispatchCommandPacket.TYPE,
            AzEntityDispatchCommandPacket.CODEC,
            (msg, ctx) -> msg.handle()
        );
        registrar.playBidirectional(
            AzItemStackDispatchCommandPacket.TYPE,
            AzItemStackDispatchCommandPacket.CODEC,
            (msg, ctx) -> msg.handle()
        );
        registrar.playBidirectional(
            AzBlockEntityDispatchCommandPacket.TYPE,
            AzBlockEntityDispatchCommandPacket.CODEC,
            (msg, ctx) -> msg.handle()
        );
        registrar.playBidirectional(
            EntityAnimDataSyncPacket.TYPE,
            EntityAnimDataSyncPacket.CODEC,
            (msg, ctx) -> msg.handle()
        );
        registrar.playBidirectional(AnimTriggerPacket.TYPE, AnimTriggerPacket.CODEC, (msg, ctx) -> msg.handle());
        registrar.playBidirectional(AnimDataSyncPacket.TYPE, AnimDataSyncPacket.CODEC, (msg, ctx) -> msg.handle());
        registrar.playBidirectional(SendConfigDataPacket.TYPE, SendConfigDataPacket.CODEC, (msg, ctx) -> msg.handle());
    }
}
