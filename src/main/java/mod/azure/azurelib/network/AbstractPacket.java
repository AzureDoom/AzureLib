package mod.azure.azurelib.network;

import net.minecraft.util.ResourceLocation;

public abstract class AbstractPacket {

    public abstract void encode(AzByteBuf buf);

    /** Runs on the client thread. */
    public abstract void handle();

    public abstract ResourceLocation getPacketID();
}
