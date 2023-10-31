package mod.azure.azurelib.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface IPacket<P extends IPacket<P>> {

    void encode(FriendlyByteBuf buffer);

    P decode(FriendlyByteBuf buffer);

    void handle(NetworkEvent.Context context);
}
