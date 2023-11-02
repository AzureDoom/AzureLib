package mod.azure.azurelib.common.internal.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractPacket {

    public abstract void encode(FriendlyByteBuf buf);

    public abstract void handle();

    public abstract ResourceLocation getPacketID();
}
