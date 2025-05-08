package mod.azure.azurelib.network;

import net.minecraft.network.PacketBuffer;

public abstract class AbstractPacket {

    public abstract void encode(PacketBuffer buf);

    public abstract void handle();
}
