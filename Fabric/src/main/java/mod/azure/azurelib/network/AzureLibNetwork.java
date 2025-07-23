package mod.azure.azurelib.network;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.BiConsumer;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.network.api.IClientPacket;
import mod.azure.azurelib.network.api.IPacket;
import mod.azure.azurelib.network.api.IPacketDecoder;
import mod.azure.azurelib.network.api.IPacketEncoder;
import mod.azure.azurelib.network.packet.*;

/**
 * Network handling class for AzureLib.<br>
 * Handles packet registration and some networking functions
 */
public final class AzureLibNetwork {

    public static final Marker MARKER = MarkerManager.getMarker("Network");

    public static final ResourceLocation ANIM_DATA_SYNC_PACKET_ID = AzureLib.modResource("anim_data_sync");

    public static final ResourceLocation ANIM_TRIGGER_SYNC_PACKET_ID = AzureLib.modResource("anim_trigger_sync");

    public static final ResourceLocation ENTITY_ANIM_DATA_SYNC_PACKET_ID = AzureLib.modResource(
        "entity_anim_data_sync"
    );

    public static final ResourceLocation ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID = AzureLib.modResource(
        "entity_anim_trigger_sync"
    );

    public static final ResourceLocation BLOCK_ENTITY_ANIM_DATA_SYNC_PACKET_ID = AzureLib.modResource(
        "block_entity_anim_data_sync"
    );

    public static final ResourceLocation BLOCK_ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID = AzureLib.modResource(
        "block_entity_anim_trigger_sync"
    );

    public static final ResourceLocation CUSTOM_ENTITY_ID = AzureLib.modResource("spawn_entity");

    public static final ResourceLocation AZ_BLOCKENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID = new ResourceLocation(
        AzureLib.MOD_ID,
        "az_blockentity_dispatch_command_sync"
    );

    public static final ResourceLocation AZ_ENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID = new ResourceLocation(
        AzureLib.MOD_ID,
        "az_entity_dispatch_command_sync"
    );

    public static final ResourceLocation AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID = new ResourceLocation(
        AzureLib.MOD_ID,
        "az_item_stack_dispatch_command_sync"
    );

    public static final Map<String, GeoAnimatable> SYNCED_ANIMATABLES = new Object2ObjectOpenHashMap<>();

    /**
     * Used to register packets that the server sends
     **/
    public static void registerClientReceiverPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ANIM_DATA_SYNC_PACKET_ID, AnimDataSyncPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(ANIM_TRIGGER_SYNC_PACKET_ID, AnimTriggerPacket::receive);

        ClientPlayNetworking.registerGlobalReceiver(ENTITY_ANIM_DATA_SYNC_PACKET_ID, EntityAnimDataSyncPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(
            ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID,
            EntityAnimTriggerPacket::receive
        );

        ClientPlayNetworking.registerGlobalReceiver(
            BLOCK_ENTITY_ANIM_DATA_SYNC_PACKET_ID,
            BlockEntityAnimDataSyncPacket::receive
        );
        ClientPlayNetworking.registerGlobalReceiver(
            BLOCK_ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID,
            BlockEntityAnimTriggerPacket::receive
        );

        ClientPlayNetworking.registerGlobalReceiver(
            CUSTOM_ENTITY_ID,
            (client, handler, buf, responseSender) -> EntityPacketOnClient.onPacket(client, buf)
        );

        ClientPlayNetworking.registerGlobalReceiver(
            AZ_BLOCKENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID,
            AzBlockEntityDispatchCommandPacket::receive
        );

        ClientPlayNetworking.registerGlobalReceiver(
            AZ_ENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID,
            AzEntityDispatchCommandPacket::receive
        );

        ClientPlayNetworking.registerGlobalReceiver(
            AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID,
            AzItemStackDispatchCommandPacket::receive
        );
    }

    /**
     * Registers a synced {@link GeoAnimatable} object for networking support.<br>
     * It is recommended that you don't call this directly, instead implementing and calling
     * {@link mod.azure.azurelib.animatable.SingletonGeoAnimatable#registerSyncedAnimatable}
     */
    @Deprecated()
    synchronized public static void registerSyncedAnimatable(GeoAnimatable animatable) {
        GeoAnimatable existing = SYNCED_ANIMATABLES.put(animatable.getClass().toString(), animatable);

        if (existing == null)
            AzureLib.LOGGER.debug("Registered SyncedAnimatable for " + animatable.getClass());
    }

    /**
     * Gets a registered synced {@link GeoAnimatable} object by name
     *
     * @param className the className
     */
    @Deprecated()
    @Nullable
    public static GeoAnimatable getSyncedAnimatable(String className) {
        GeoAnimatable animatable = SYNCED_ANIMATABLES.get(className);

        if (animatable == null)
            AzureLib.LOGGER.error("Attempting to retrieve unregistered synced animatable! (" + className + ")");

        return animatable;
    }

    public static void sendWithCallback(AbstractPacket packet, IPacketCallback callback) {
        callback.onReadyToSend(packet);
    }

    public static void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack) {
        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(entityToTrack)) {
            ServerPlayNetworking.send(trackingPlayer, packet.getPacketID(), packet.encode());
        }

        if (entityToTrack instanceof ServerPlayer)
            ServerPlayNetworking.send(((ServerPlayer) entityToTrack), packet.getPacketID(), packet.encode());
    }

    public static void sendToEntitiesTrackingChunk(AbstractPacket packet, ServerLevel level, BlockPos blockPos) {
        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(level, blockPos)) {
            ServerPlayNetworking.send(trackingPlayer, packet.getPacketID(), packet.encode());
        }
    }

    public interface IPacketCallback {

        void onReadyToSend(AbstractPacket packetToSend);
    }

    public static void sendClientPacket(ServerPlayer target, IClientPacket<?> packet) {
        dispatch(packet, (packetId, buffer) -> ServerPlayNetworking.send(target, packetId, buffer));
    }

    private static <T> void dispatch(IPacket<T> packet, BiConsumer<ResourceLocation, FriendlyByteBuf> dispatcher) {
        ResourceLocation packetId = packet.getPacketId();
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        IPacketEncoder<T> encoder = packet.getEncoder();
        T data = packet.getPacketData();
        encoder.encode(data, buffer);
        dispatcher.accept(packetId, buffer);
    }

    public static final class PacketRegistry {

        public static void registerClient() {
            registerServer2ClientReceiver(S2C_SendConfigData.class);
        }

        @Environment(EnvType.CLIENT)
        private static <T> void registerServer2ClientReceiver(Class<? extends IClientPacket<T>> clientPacketClass) {
            try {
                IClientPacket<T> packet = clientPacketClass.getDeclaredConstructor().newInstance();
                ResourceLocation packetId = packet.getPacketId();
                ClientPlayNetworking.registerGlobalReceiver(packetId, (client, handler, buffer, responseDispatcher) -> {
                    IPacketDecoder<T> decoder = packet.getDecoder();
                    T packetData = decoder.decode(buffer);
                    client.execute(
                        () -> packet.handleClientsidePacket(client, handler, packetData, responseDispatcher)
                    );
                });
            } catch (
                NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException exc
            ) {
                AzureLib.LOGGER.fatal(
                    MARKER,
                    "Couldn't instantiate new client packet from class {}, make sure it declares public default constructor",
                    clientPacketClass.getSimpleName()
                );
                throw new RuntimeException(exc);
            }
        }
    }
}
