package mod.azure.azurelib.network;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.network.packet.AnimDataSyncPacket;
import mod.azure.azurelib.network.packet.AnimTriggerPacket;
import mod.azure.azurelib.network.packet.BlockEntityAnimDataSyncPacket;
import mod.azure.azurelib.network.packet.BlockEntityAnimTriggerPacket;
import mod.azure.azurelib.network.packet.EntityAnimDataSyncPacket;
import mod.azure.azurelib.network.packet.EntityAnimTriggerPacket;
import mod.azure.azurelib.network.packet.EntityPacketOnClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Network handling class for AzureLib.<br>
 * Handles packet registration and some networking functions
 */
public final class AzureLibNetwork {
	public static final ResourceLocation ANIM_DATA_SYNC_PACKET_ID = new ResourceLocation(AzureLib.MOD_ID, "anim_data_sync");
	public static final ResourceLocation ANIM_TRIGGER_SYNC_PACKET_ID = new ResourceLocation(AzureLib.MOD_ID, "anim_trigger_sync");

	public static final ResourceLocation ENTITY_ANIM_DATA_SYNC_PACKET_ID = new ResourceLocation(AzureLib.MOD_ID, "entity_anim_data_sync");
	public static final ResourceLocation ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID = new ResourceLocation(AzureLib.MOD_ID, "entity_anim_trigger_sync");

	public static final ResourceLocation BLOCK_ENTITY_ANIM_DATA_SYNC_PACKET_ID = new ResourceLocation(AzureLib.MOD_ID, "block_entity_anim_data_sync");
	public static final ResourceLocation BLOCK_ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID = new ResourceLocation(AzureLib.MOD_ID, "block_entity_anim_trigger_sync");

	public static final ResourceLocation CUSTOM_ENTITY_ID = new ResourceLocation(AzureLib.MOD_ID, "spawn_entity");

	public static final Map<String, GeoAnimatable> SYNCED_ANIMATABLES = new Object2ObjectOpenHashMap<>();

	/**
	 * Used to register packets that the server sends
	 **/
	public static void registerClientReceiverPackets() {
		ClientPlayNetworking.registerGlobalReceiver(ANIM_DATA_SYNC_PACKET_ID, AnimDataSyncPacket::receive);
		ClientPlayNetworking.registerGlobalReceiver(ANIM_TRIGGER_SYNC_PACKET_ID, AnimTriggerPacket::receive);

		ClientPlayNetworking.registerGlobalReceiver(ENTITY_ANIM_DATA_SYNC_PACKET_ID, EntityAnimDataSyncPacket::receive);
		ClientPlayNetworking.registerGlobalReceiver(ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID, EntityAnimTriggerPacket::receive);

		ClientPlayNetworking.registerGlobalReceiver(BLOCK_ENTITY_ANIM_DATA_SYNC_PACKET_ID, BlockEntityAnimDataSyncPacket::receive);
		ClientPlayNetworking.registerGlobalReceiver(BLOCK_ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID, BlockEntityAnimTriggerPacket::receive);

		ClientPlayNetworking.registerGlobalReceiver(CUSTOM_ENTITY_ID, (client, handler, buf, responseSender) -> {
			EntityPacketOnClient.onPacket(client, buf);
		});
	}

	/**
	 * Registers a synced {@link GeoAnimatable} object for networking support.<br>
	 * It is recommended that you don't call this directly, instead implementing and calling {@link mod.azure.azurelib.animatable.SingletonGeoAnimatable#registerSyncedAnimatable}
	 */
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
}
