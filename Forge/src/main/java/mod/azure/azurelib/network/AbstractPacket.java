package mod.azure.azurelib.network;

import net.minecraft.network.FriendlyByteBuf;

public abstract class AbstractPacket {

    public abstract void encode(FriendlyByteBuf buf);

    public abstract void handle();
}
