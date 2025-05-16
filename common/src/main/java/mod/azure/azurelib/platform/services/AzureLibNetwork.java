package mod.azure.azurelib.platform.services;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.network.AbstractPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

class LockHolder { // Package private class
    public static Object LOCK = new Object();
}

public interface AzureLibNetwork {
    @Deprecated(forRemoval = true)
    ResourceLocation ANIM_DATA_SYNC_PACKET_ID = AzureLib.modResource("anim_data_sync");
    @Deprecated(forRemoval = true)
    ResourceLocation ANIM_TRIGGER_SYNC_PACKET_ID = AzureLib.modResource("anim_trigger_sync");

    @Deprecated(forRemoval = true)
    ResourceLocation ENTITY_ANIM_DATA_SYNC_PACKET_ID = AzureLib.modResource("entity_anim_data_sync");
    @Deprecated(forRemoval = true)
    ResourceLocation ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID = AzureLib.modResource("entity_anim_trigger_sync");

    @Deprecated(forRemoval = true)
    ResourceLocation BLOCK_ENTITY_ANIM_DATA_SYNC_PACKET_ID = AzureLib.modResource("block_entity_anim_data_sync");
    @Deprecated(forRemoval = true)
    ResourceLocation BLOCK_ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID = AzureLib.modResource("block_entity_anim_trigger_sync");

    @Deprecated(forRemoval = true)
    ResourceLocation CUSTOM_ENTITY_ID = AzureLib.modResource("spawn_entity");

    @Deprecated(forRemoval = true)
    Map<String, GeoAnimatable> SYNCED_ANIMATABLES = new Object2ObjectOpenHashMap<>();

    ResourceLocation AZ_BLOCKENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID = AzureLib.modResource(
            "az_blockentity_dispatch_command_sync"
    );

    ResourceLocation AZ_ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID = AzureLib.modResource("az_entity_anim_trigger_sync");

    ResourceLocation AZ_ENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID = AzureLib.modResource(
            "az_entity_dispatch_command_sync"
    );

    ResourceLocation AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID = AzureLib.modResource(
            "az_item_stack_dispatch_command_sync"
    );

    /**
     * Registers a synced {@link GeoAnimatable} object for networking support.<br>
     * It is recommended that you don't call this directly, instead implementing and calling {@link mod.azure.azurelib.animatable.SingletonGeoAnimatable#registerSyncedAnimatable}
     */
    @Deprecated(forRemoval = true)
    default void registerSyncedAnimatable(GeoAnimatable animatable) {
        synchronized (this) {
            GeoAnimatable existing = SYNCED_ANIMATABLES.put(animatable.getClass().toString(), animatable);

            if (existing == null)
                AzureLib.LOGGER.debug("Registered SyncedAnimatable for " + animatable.getClass());
        }
    }

    @Deprecated(forRemoval = true)
    Packet<ClientGamePacketListener> createPacket(Entity entity);

    /**
     * Used to register packets that the server sends
     **/
    @Deprecated(forRemoval = true)
    void registerClientReceiverPackets();

    void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack);
    void sendToEntitiesTrackingChunk(AbstractPacket packet, ServerLevel level, BlockPos blockPos);

    void sendClientPacket(ServerPlayer player, String id);
    static void sendWithCallback(AbstractPacket packet, IPacketCallback callback) {
        callback.onReadyToSend(packet);
    }

    interface IPacketCallback {
        void onReadyToSend(AbstractPacket packetToSend);
    }

    /**
     * Gets a registered synced {@link GeoAnimatable} object by name
     *
     * @param className the className
     */
    @Nullable
    @Deprecated(forRemoval = true)
    static GeoAnimatable getSyncedAnimatable(String className) {
        GeoAnimatable animatable = SYNCED_ANIMATABLES.get(className);

        if (animatable == null)
            AzureLib.LOGGER.error("Attempting to retrieve unregistered synced animatable! (" + className + ")");

        return animatable;
    }
}
