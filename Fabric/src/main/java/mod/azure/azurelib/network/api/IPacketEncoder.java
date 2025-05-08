package mod.azure.azurelib.network.api;

import net.minecraft.network.FriendlyByteBuf;

@FunctionalInterface
public interface IPacketEncoder<T> {

    void encode(T data, FriendlyByteBuf buffer);
}
