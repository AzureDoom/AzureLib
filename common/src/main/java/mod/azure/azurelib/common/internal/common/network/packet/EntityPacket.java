package mod.azure.azurelib.common.internal.common.network.packet;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

import mod.azure.azurelib.common.platform.Services;

public class EntityPacket {

    public static Packet<?> createPacket(Entity entity) {
        return Services.NETWORK.createPacket(entity);
    }

    private EntityPacket() {
        throw new UnsupportedOperationException();
    }
}
