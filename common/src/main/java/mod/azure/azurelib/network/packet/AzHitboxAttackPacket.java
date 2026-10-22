package mod.azure.azurelib.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.hitbox.AzHitboxHolder;
import mod.azure.azurelib.network.AzServerboundPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;

public record AzHitboxAttackPacket(
    int entityId,
    int hitboxIndex
) implements AzServerboundPacket {

    public static final Type<AzHitboxAttackPacket> TYPE = new Type<>(AzureLibNetwork.AZ_HITBOX_ATTACK_PACKET_ID);

    public static final StreamCodec<FriendlyByteBuf, AzHitboxAttackPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        AzHitboxAttackPacket::entityId,
        ByteBufCodecs.VAR_INT,
        AzHitboxAttackPacket::hitboxIndex,
        AzHitboxAttackPacket::new
    );

    @Override
    public void handleServer(ServerPlayer sender) {
        var target = sender.level().getEntity(entityId);

        if (target instanceof AzHitboxHolder && target != sender) {
            AzHitboxDamageTracker.claimMelee(sender, target, hitboxIndex);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
