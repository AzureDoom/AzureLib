package mod.azure.azurelib.network.packet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import mod.azure.azurelib.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.hitbox.AzHitboxHolder;
import mod.azure.azurelib.network.AzByteBuf;
import mod.azure.azurelib.network.AzServerboundPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;

public final class AzHitboxInteractPacket implements AzServerboundPacket {

    public static final ResourceLocation ID = AzureLibNetwork.AZ_HITBOX_INTERACT_PACKET_ID;

    private final int entityId;

    private final int hitboxIndex;

    public AzHitboxInteractPacket(int entityId, int hitboxIndex) {
        this.entityId = entityId;
        this.hitboxIndex = hitboxIndex;
    }

    public static AzHitboxInteractPacket read(AzByteBuf buf) {
        return new AzHitboxInteractPacket(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(AzByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(hitboxIndex);
    }

    @Override
    public void handleServer(EntityPlayerMP sender) {
        Entity target = sender.getServerForPlayer().getEntityByID(entityId);

        if (target instanceof AzHitboxHolder && target != sender) {
            AzHitboxDamageTracker.claimInteract(sender, target, hitboxIndex);
        }
    }
}
