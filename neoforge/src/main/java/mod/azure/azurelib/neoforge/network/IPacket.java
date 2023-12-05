package mod.azure.azurelib.neoforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public interface IPacket<P extends IPacket<P>> {

    void encode(FriendlyByteBuf buffer);

    P decode(FriendlyByteBuf buffer);

    void handle(NetworkEvent.Context context);
}
