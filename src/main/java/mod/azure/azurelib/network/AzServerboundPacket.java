package mod.azure.azurelib.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public interface AzServerboundPacket {

    ResourceLocation id();

    void write(PacketBuffer buf);

    void handleServer(EntityPlayerMP sender);
}
