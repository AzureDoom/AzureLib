package mod.azure.azurelib.common.internal.common.network.api;

import net.minecraft.network.FriendlyByteBuf;

@FunctionalInterface
public interface IPacketEncoder<T> {

    void encode(T data, FriendlyByteBuf buffer);
}
