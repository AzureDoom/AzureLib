package mod.azure.azurelib.network.packet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.player.PlayerEntity;

import mod.azure.azurelib.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.hitbox.AzHitboxHolder;
import mod.azure.azurelib.network.AzByteBuf;
import mod.azure.azurelib.network.AzServerboundPacket;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.util.ResourceLocation;

public final class AzHitboxAttackPacket implements AzServerboundPacket {

    public static final ResourceLocation ID = AzureLibNetwork.AZ_HITBOX_ATTACK_PACKET_ID;

    private final int entityId;

    private final int hitboxIndex;

    public AzHitboxAttackPacket(int entityId, int hitboxIndex) {
        this.entityId = entityId;
        this.hitboxIndex = hitboxIndex;
    }

    public static AzHitboxAttackPacket read(AzByteBuf buf) {
        return new AzHitboxAttackPacket(buf.readVarInt(), buf.readVarInt());
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
    public void handleServer(PlayerEntity sender) {
        Entity target = Mc.entityById(Mc.world(sender), entityId);

        if (target instanceof AzHitboxHolder && target != sender) {
            AzHitboxDamageTracker.claimMelee(sender, target, hitboxIndex);
        }
    }
}
