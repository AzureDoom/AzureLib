package mod.azure.azurelib.network;

import net.minecraft.entity.player.PlayerEntity;

import mod.azure.azurelib.util.ResourceLocation;

/**
 * A client-to-server AzureLib packet. The sender is typed as {@link PlayerEntity} because the server player class does
 * not exist in the client jar, and in single player these are handled directly on the client with the local player.
 */
public interface AzServerboundPacket {

    ResourceLocation id();

    void write(AzByteBuf buf);

    void handleServer(PlayerEntity sender);
}
