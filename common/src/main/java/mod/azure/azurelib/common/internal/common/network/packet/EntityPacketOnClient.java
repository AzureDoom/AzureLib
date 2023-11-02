package mod.azure.azurelib.common.internal.common.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.UUID;

public class EntityPacketOnClient {
    public static void onPacket(Minecraft context, FriendlyByteBuf byteBuf) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.byId(byteBuf.readVarInt());
        UUID entityUUID = byteBuf.readUUID();
        int entityID = byteBuf.readVarInt();
        double x = byteBuf.readDouble();
        double y = byteBuf.readDouble();
        double z = byteBuf.readDouble();
        float pitch = (byteBuf.readByte() * 360) / 256.0F;
        float yaw = (byteBuf.readByte() * 360) / 256.0F;
        context.execute(() -> {
            ClientLevel world = Minecraft.getInstance().level;
            Entity entity = type.create(world);
            if (entity != null) {
                entity.absMoveTo(x, y, z);
                entity.syncPacketPositionCodec(x, y, z);
                entity.setXRot(pitch);
                entity.setYRot(yaw);
                entity.setId(entityID);
                entity.setUUID(entityUUID);
                world.addEntity(entity);
            }
        });
    }

    private EntityPacketOnClient() {
        throw new UnsupportedOperationException();
    }
}