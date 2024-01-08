package mod.azure.azurelib.neoforge.network;

import net.minecraft.network.FriendlyByteBuf;

public interface IPacket<P extends IPacket<P>> {

    P decode(FriendlyByteBuf buffer);
}
