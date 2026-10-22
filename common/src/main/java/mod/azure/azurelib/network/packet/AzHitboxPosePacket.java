package mod.azure.azurelib.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import mod.azure.azurelib.hitbox.AzHitboxSet;
import mod.azure.azurelib.hitbox.AzHitboxSync;
import mod.azure.azurelib.network.AzServerboundPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;

public record AzHitboxPosePacket(
    int entityId,
    float referenceYaw,
    boolean[] active,
    float[] data
) implements AzServerboundPacket {

    public static final ResourceLocation ID = AzureLibNetwork.AZ_HITBOX_POSE_PACKET_ID;

    private static final int FLOATS_PER_BOX = 10;

    public static AzHitboxPosePacket read(FriendlyByteBuf buf) {
        var entityId = buf.readVarInt();
        var yaw = buf.readFloat();
        var count = buf.readVarInt();

        if (count < 0 || count > AzHitboxSet.MAX_HITBOXES) {
            throw new IllegalArgumentException("Too many hitboxes in pose packet: " + count);
        }

        var active = new boolean[count];
        var activeCount = 0;

        for (int i = 0; i < count; i++) {
            active[i] = buf.readBoolean();

            if (active[i]) {
                activeCount++;
            }
        }

        var data = new float[activeCount * FLOATS_PER_BOX];

        for (int i = 0; i < data.length; i++) {
            data[i] = buf.readFloat();
        }

        return new AzHitboxPosePacket(entityId, yaw, active, data);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeFloat(referenceYaw);
        buf.writeVarInt(active.length);

        for (var a : active) {
            buf.writeBoolean(a);
        }

        for (var f : data) {
            buf.writeFloat(f);
        }
    }

    @Override
    public void handleServer(ServerPlayer sender) {
        var entity = sender.serverLevel().getEntity(entityId);

        if (entity != null) {
            AzHitboxSync.acceptReport(sender, entity, referenceYaw, active, data);
        }
    }
}
