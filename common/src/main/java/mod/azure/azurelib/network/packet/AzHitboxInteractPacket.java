package mod.azure.azurelib.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import mod.azure.azurelib.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.hitbox.AzHitboxHolder;
import mod.azure.azurelib.network.AzServerboundPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;

public record AzHitboxInteractPacket(
    int entityId,
    int hitboxIndex
) implements AzServerboundPacket {

    public static final ResourceLocation ID = AzureLibNetwork.AZ_HITBOX_INTERACT_PACKET_ID;

    public static AzHitboxInteractPacket read(FriendlyByteBuf buf) {
        return new AzHitboxInteractPacket(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(hitboxIndex);
    }

    @Override
    public void handleServer(ServerPlayer sender) {
        var target = sender.level.getEntity(entityId);

        if (target instanceof AzHitboxHolder && target != sender) {
            AzHitboxDamageTracker.claimInteract(sender, target, hitboxIndex);
        }
    }
}
