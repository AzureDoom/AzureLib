package mod.azure.azurelib.common.internal.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractPacket implements CustomPacketPayload {

    public abstract void write(FriendlyByteBuf buf);

    public abstract void handle();

    public abstract ResourceLocation id();
}
