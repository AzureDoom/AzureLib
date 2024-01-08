package mod.azure.azurelib.common.platform.services;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.common.internal.common.core.animatable.GeoAnimatable;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
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
    ResourceLocation ANIM_DATA_SYNC_PACKET_ID = AzureLib.modResource("anim_data_sync");
    ResourceLocation ANIM_TRIGGER_SYNC_PACKET_ID = AzureLib.modResource("anim_trigger_sync");

    ResourceLocation ENTITY_ANIM_DATA_SYNC_PACKET_ID = AzureLib.modResource("entity_anim_data_sync");
    ResourceLocation ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID = AzureLib.modResource("entity_anim_trigger_sync");

    ResourceLocation BLOCK_ENTITY_ANIM_DATA_SYNC_PACKET_ID = AzureLib.modResource("block_entity_anim_data_sync");
    ResourceLocation BLOCK_ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID = AzureLib.modResource("block_entity_anim_trigger_sync");
    ResourceLocation CONFIG_PACKET_ID = AzureLib.modResource("config_packet");

    ResourceLocation CUSTOM_ENTITY_ID = AzureLib.modResource("spawn_entity");

    ResourceLocation RELOAD = AzureLib.modResource("reload");

    Map<String, GeoAnimatable> SYNCED_ANIMATABLES = new Object2ObjectOpenHashMap<>();

    /**
     * Registers a synced {@link GeoAnimatable} object for networking support.<br>
     * It is recommended that you don't call this directly, instead implementing and calling {@link SingletonGeoAnimatable#registerSyncedAnimatable}
     */
    default void registerSyncedAnimatable(GeoAnimatable animatable) {
        synchronized (this) {
            GeoAnimatable existing = SYNCED_ANIMATABLES.put(animatable.getClass().toString(), animatable);

            if (existing == null)
                AzureLib.LOGGER.debug("Registered SyncedAnimatable for " + animatable.getClass());
        }
    }

    Packet<?> createPacket(Entity entity);

    /**
     * Used to register packets that the server sends
     **/
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
    static GeoAnimatable getSyncedAnimatable(String className) {
        GeoAnimatable animatable = SYNCED_ANIMATABLES.get(className);

        if (animatable == null)
            AzureLib.LOGGER.error("Attempting to retrieve unregistered synced animatable! (" + className + ")");

        return animatable;
    }
}
