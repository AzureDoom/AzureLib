package mod.azure.azurelib.common.internal.common.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public abstract class AbstractPacket implements CustomPacketPayload {

    public abstract void handle();
}
