package mod.azure.azurelib.common.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface AzServerboundPacket extends CustomPacketPayload {

    void handleServer(ServerPlayer sender);
}
