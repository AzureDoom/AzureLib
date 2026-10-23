package mod.azure.azurelib.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface AzServerboundPacket {

    ResourceLocation id();

    void write(FriendlyByteBuf buf);

    void handleServer(ServerPlayer sender);
}
