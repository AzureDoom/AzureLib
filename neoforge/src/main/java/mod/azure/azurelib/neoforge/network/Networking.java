package mod.azure.azurelib.neoforge.network;

import mod.azure.azurelib.common.internal.common.AzureLib;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.server.level.ServerPlayer;

public final class Networking {

    public static final Marker MARKER = MarkerManager.getMarker("Network");
    private static final String NETWORK_VERSION = "2.0.0";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(AzureLib.modResource("network_channel"))
            .networkProtocolVersion(() -> NETWORK_VERSION)
            .clientAcceptedVersions(NETWORK_VERSION::equals)
            .serverAcceptedVersions(NETWORK_VERSION::equals)
            .simpleChannel();

    public static void sendClientPacket(ServerPlayer target, IPacket<?> packet) {
        CHANNEL.sendTo(packet, target.connection.connection, PlayNetworkDirection.PLAY_TO_CLIENT);
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
                throw new ReportedException(CrashReport.forThrowable(e, "Couldn't instantiate packet for registration. Make sure you have provided public constructor with no parameters."));
            }
            CHANNEL.registerMessage(packetIndex++, packetType, IPacket::encode, packet::decode, IPacket::handle);
        }
    }
}
