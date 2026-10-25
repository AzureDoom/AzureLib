package mod.azure.azurelib.network.packet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import mod.azure.azurelib.hitbox.AzHitboxSet;
import mod.azure.azurelib.hitbox.AzHitboxSync;
import mod.azure.azurelib.network.AzByteBuf;
import mod.azure.azurelib.network.AzServerboundPacket;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.util.ResourceLocation;

public final class AzHitboxPosePacket implements AzServerboundPacket {

    public static final ResourceLocation ID = AzureLibNetwork.AZ_HITBOX_POSE_PACKET_ID;

    private static final int FLOATS_PER_BOX = 10;

    private final int entityId;

    private final float referenceYaw;

    private final boolean[] active;

    private final float[] data;

    public AzHitboxPosePacket(int entityId, float referenceYaw, boolean[] active, float[] data) {
        this.entityId = entityId;
        this.referenceYaw = referenceYaw;
        this.active = active;
        this.data = data;
    }

    public static AzHitboxPosePacket read(AzByteBuf buf) {
        int entityId = buf.readVarInt();
        float yaw = buf.readFloat();
        int count = buf.readVarInt();

        if (count < 0 || count > AzHitboxSet.MAX_HITBOXES) {
            throw new IllegalArgumentException("Too many hitboxes in pose packet: " + count);
        }

        boolean[] active = new boolean[count];
        int activeCount = 0;

        for (int i = 0; i < count; i++) {
            active[i] = buf.readBoolean();

            if (active[i]) {
                activeCount++;
            }
        }

        float[] data = new float[activeCount * FLOATS_PER_BOX];

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
    public void write(AzByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeFloat(referenceYaw);
        buf.writeVarInt(active.length);

        for (boolean a : active) {
            buf.writeBoolean(a);
        }

        for (float f : data) {
            buf.writeFloat(f);
        }
    }

    @Override
    public void handleServer(PlayerEntity sender) {
        Entity entity = Mc.entityById(sender.world, entityId);

        if (entity != null) {
            AzHitboxSync.acceptReport(sender, entity, referenceYaw, active, data);
        }
    }
}
