package mod.azure.azurelib.neoforge;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.packet.*;

@Mod(AzureLib.MOD_ID)
public final class NeoForgeAzureLibMod {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS_REGISTER = DeferredRegister
        .createDataComponents(
            Registries.DATA_COMPONENT_TYPE,
            AzureLib.MOD_ID
        );

    public NeoForgeAzureLibMod(IEventBus modEventBus) {
        AzureLib.initialize();
        DATA_COMPONENTS_REGISTER.register(modEventBus);
        modEventBus.addListener(this::registerMessages);
    }

    public void registerMessages(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(AzureLib.MOD_ID);
        registrar.playBidirectional(
            AzEntityDispatchCommandPacket.TYPE,
            AzEntityDispatchCommandPacket.CODEC,
            (msg, _) -> msg.handle()
        );
        registrar.playBidirectional(
            AzItemStackDispatchCommandPacket.TYPE,
            AzItemStackDispatchCommandPacket.CODEC,
            (msg, _) -> msg.handle()
        );
        registrar.playBidirectional(
            AzBlockEntityDispatchCommandPacket.TYPE,
            AzBlockEntityDispatchCommandPacket.CODEC,
            (msg, _) -> msg.handle()
        );
        registrar.playBidirectional(
            SendConfigDataPacket.TYPE,
            SendConfigDataPacket.CODEC,
            (msg, ctx) -> msg.handle()
        );
        registrar.playToServer(
            AzHitboxPosePacket.TYPE,
            AzHitboxPosePacket.CODEC,
            (msg, ctx) -> msg.handleServer((ServerPlayer) ctx.player())
        );
        registrar.playToServer(
            AzHitboxAttackPacket.TYPE,
            AzHitboxAttackPacket.CODEC,
            (msg, ctx) -> msg.handleServer((ServerPlayer) ctx.player())
        );
        registrar.playToServer(
            AzHitboxInteractPacket.TYPE,
            AzHitboxInteractPacket.CODEC,
            (msg, ctx) -> msg.handleServer((ServerPlayer) ctx.player())
        );
    }
}
