package mod.azure.azurelib.common.internal.common.network.packet;

import mod.azure.azurelib.common.platform.Services;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class EntityPacket {
    public static Packet<?> createPacket(Entity entity) {
        return Services.NETWORK.createPacket(entity);
    }

    private EntityPacket() {
        throw new UnsupportedOperationException();
    }
}
