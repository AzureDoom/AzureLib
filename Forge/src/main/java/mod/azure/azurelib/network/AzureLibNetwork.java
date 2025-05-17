package mod.azure.azurelib.network;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.network.packet.*;

/**
 * Network handling class for AzureLib.<br>
 * Handles packet registration and some networking functions
 */
public final class AzureLibNetwork {

    private static final String VER = "1";

    private static final SimpleChannel PACKET_CHANNEL = NetworkRegistry.newSimpleChannel(
        AzureLib.modResource("main"),
        () -> VER,
        VER::equals,
        VER::equals
    );

    private static final Map<String, GeoAnimatable> SYNCED_ANIMATABLES = new Object2ObjectOpenHashMap<>();

    public static void init() {
        int id = 0;

        PACKET_CHANNEL.registerMessage(
            id++,
            AnimDataSyncPacket.class,
            AnimDataSyncPacket::encode,
            AnimDataSyncPacket::decode,
            AnimDataSyncPacket::receivePacket
        );
        PACKET_CHANNEL.registerMessage(
            id++,
            AnimTriggerPacket.class,
            AnimTriggerPacket::encode,
            AnimTriggerPacket::decode,
            AnimTriggerPacket::receivePacket
        );
        PACKET_CHANNEL.registerMessage(
            id++,
            EntityAnimDataSyncPacket.class,
            EntityAnimDataSyncPacket::encode,
            EntityAnimDataSyncPacket::decode,
            EntityAnimDataSyncPacket::receivePacket
        );
        PACKET_CHANNEL.registerMessage(
            id++,
            EntityAnimTriggerPacket.class,
            EntityAnimTriggerPacket::encode,
            EntityAnimTriggerPacket::decode,
            EntityAnimTriggerPacket::receivePacket
        );
        PACKET_CHANNEL.registerMessage(
            id++,
            BlockEntityAnimDataSyncPacket.class,
            BlockEntityAnimDataSyncPacket::encode,
            BlockEntityAnimDataSyncPacket::decode,
            BlockEntityAnimDataSyncPacket::receivePacket
        );
        PACKET_CHANNEL.registerMessage(
            id++,
            BlockEntityAnimTriggerPacket.class,
            BlockEntityAnimTriggerPacket::encode,
            BlockEntityAnimTriggerPacket::decode,
            BlockEntityAnimTriggerPacket::receivePacket
        );

        PACKET_CHANNEL.registerMessage(
            id++,
            AzBlockEntityDispatchCommandPacket.class,
            AzBlockEntityDispatchCommandPacket::encode,
            AzBlockEntityDispatchCommandPacket::receive,
            AzureLibNetwork::handlePacket
        );
        PACKET_CHANNEL.registerMessage(
            id++,
            AzItemStackDispatchCommandPacket.class,
            AzItemStackDispatchCommandPacket::encode,
            AzItemStackDispatchCommandPacket::receive,
            AzureLibNetwork::handlePacket
        );
        PACKET_CHANNEL.registerMessage(
            id++,
            AzEntityDispatchCommandPacket.class,
            AzEntityDispatchCommandPacket::encode,
            AzEntityDispatchCommandPacket::receive,
            AzureLibNetwork::handlePacket
        );
    }

    /**
     * Registers a synced {@link GeoAnimatable} object for networking support.<br>
     * It is recommended that you don't call this directly, instead implementing and calling
     * {@link mod.azure.azurelib.animatable.SingletonGeoAnimatable#registerSyncedAnimatable}
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

    private static void handlePacket(AbstractPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context handler = context.get();
        handler.enqueueWork(packet::handle);
        handler.setPacketHandled(true);
    }
}
