package mod.azure.azurelib.network;

import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.network.packet.*;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Network handling class for AzureLib.<br>
 * Handles packet registration and some networking functions
 */
public final class AzureLibNetwork {

	public static final Marker MARKER = MarkerManager.getMarker("Network");
	private static final String VER = "1";
	private static final SimpleChannel PACKET_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(AzureLib.MOD_ID, "main"), () -> VER, VER::equals, VER::equals);

	private static final Map<String, GeoAnimatable> SYNCED_ANIMATABLES = new Object2ObjectOpenHashMap<>();

	public static void init() {
		int id = 0;

		PACKET_CHANNEL.registerMessage(id++, AnimDataSyncPacket.class, AnimDataSyncPacket::encode, AnimDataSyncPacket::decode, AnimDataSyncPacket::receivePacket);
		PACKET_CHANNEL.registerMessage(id++, AnimTriggerPacket.class, AnimTriggerPacket::encode, AnimTriggerPacket::decode, AnimTriggerPacket::receivePacket);
		PACKET_CHANNEL.registerMessage(id++, EntityAnimDataSyncPacket.class, EntityAnimDataSyncPacket::encode, EntityAnimDataSyncPacket::decode, EntityAnimDataSyncPacket::receivePacket);
		PACKET_CHANNEL.registerMessage(id++, EntityAnimTriggerPacket.class, EntityAnimTriggerPacket::encode, EntityAnimTriggerPacket::decode, EntityAnimTriggerPacket::receivePacket);
		PACKET_CHANNEL.registerMessage(id++, BlockEntityAnimDataSyncPacket.class, BlockEntityAnimDataSyncPacket::encode, BlockEntityAnimDataSyncPacket::decode, BlockEntityAnimDataSyncPacket::receivePacket);
		PACKET_CHANNEL.registerMessage(id++, BlockEntityAnimTriggerPacket.class, BlockEntityAnimTriggerPacket::encode, BlockEntityAnimTriggerPacket::decode, BlockEntityAnimTriggerPacket::receivePacket);

		PACKET_CHANNEL.registerMessage(id++, AzBlockEntityDispatchCommandPacket.class, AzBlockEntityDispatchCommandPacket::encode, AzBlockEntityDispatchCommandPacket::receive, AzureLibNetwork::handlePacket);
		PACKET_CHANNEL.registerMessage(id++, AzItemStackDispatchCommandPacket.class, AzItemStackDispatchCommandPacket::encode, AzItemStackDispatchCommandPacket::receive, AzureLibNetwork::handlePacket);
		PACKET_CHANNEL.registerMessage(id++, AzEntityDispatchCommandPacket.class, AzEntityDispatchCommandPacket::encode, AzEntityDispatchCommandPacket::receive, AzureLibNetwork::handlePacket);
	}

	/**
	 * Registers a synced {@link GeoAnimatable} object for networking support.<br>
	 * It is recommended that you don't call this directly, instead implementing and calling {@link mod.azure.azurelib.animatable.SingletonGeoAnimatable#registerSyncedAnimatable}
	 */
	synchronized public static void registerSyncedAnimatable(GeoAnimatable animatable) {
		GeoAnimatable existing = SYNCED_ANIMATABLES.put(animatable.getClass().toString(), animatable);

		if (existing == null)
			AzureLib.LOGGER.debug("Registered SyncedAnimatable for " + animatable.getClass().toString());
	}

	/**
	 * Gets a registered synced {@link GeoAnimatable} object by name
	 * 
	 * @param className
	 */
	@Nullable
	public static GeoAnimatable getSyncedAnimatable(String className) {
		GeoAnimatable animatable = SYNCED_ANIMATABLES.get(className);

		if (animatable == null)
			AzureLib.LOGGER.error("Attempting to retrieve unregistered synced animatable! (" + className + ")");

		return animatable;
	}

	/**
	 * Send a packet using AzureLib's packet channel
	 */
	public static <M> void send(M packet, PacketDistributor.PacketTarget distributor) {
		PACKET_CHANNEL.send(distributor, packet);
	}

	public static void sendClientPacket(ServerPlayer target, IPacket<?> packet) {
		PACKET_CHANNEL.sendTo(packet, target.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}

	private static void handlePacket(AbstractPacket packet, Supplier<NetworkEvent.Context> context) {
		NetworkEvent.Context handler = context.get();
		handler.enqueueWork(packet::handle);
		handler.setPacketHandled(true);
	}

	public static final class PacketRegistry {

		private static int packetIndex;

		public static void register() {
			registerNetworkPacket(S2C_SendConfigData.class);
		}

		private static <P extends IPacket<P>> void registerNetworkPacket(Class<P> packetType) {
			P packet;
			try {
				packet = packetType.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new ReportedException(
					CrashReport.forThrowable(
						e,
						"Couldn't instantiate packet for registration. Make sure you have provided public constructor with no parameters."
					)
				);
			}
			PACKET_CHANNEL.registerMessage(packetIndex++, packetType, IPacket::encode, packet::decode, IPacket::handle);
		}
	}
}
