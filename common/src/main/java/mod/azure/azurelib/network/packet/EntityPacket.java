package mod.azure.azurelib.network.packet;

import mod.azure.azurelib.platform.Services;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;

public class EntityPacket {
    public static Packet<ClientGamePacketListener> createPacket(Entity entity) {
        return Services.NETWORK.createPacket(entity);
    }
}
