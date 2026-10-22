package mod.azure.azurelib.network;

import mod.azure.azurelib.network.packet.AzHitboxAttackPacket;
import mod.azure.azurelib.network.packet.AzHitboxInteractPacket;
import mod.azure.azurelib.network.packet.AzHitboxPosePacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;

public final class AzServerboundPackets {

    private AzServerboundPackets() {
        throw new UnsupportedOperationException();
    }

    public static void register(AzureLibNetwork network) {
        network.registerServerbound(AzHitboxPosePacket.ID, AzHitboxPosePacket.class, AzHitboxPosePacket::read);
        network.registerServerbound(AzHitboxAttackPacket.ID, AzHitboxAttackPacket.class, AzHitboxAttackPacket::read);
        network.registerServerbound(
            AzHitboxInteractPacket.ID,
            AzHitboxInteractPacket.class,
            AzHitboxInteractPacket::read
        );
    }
}
