package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.server.entity.mob.player.ServerPlayerEntity;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mod.azure.azurelib.hitbox.AzHitboxHolder;
import mod.azure.azurelib.hitbox.AzHitboxMath;
import mod.azure.azurelib.hitbox.AzHitboxes;
import mod.azure.azurelib.platform.mc.Mc;

/**
 * Dedicated server only. Beta rejects an attack or interaction when the target's position is 6 or more blocks from the
 * player; for hitbox holders the distance is measured to the nearest pickable hitbox instead, so long tails and necks
 * can be hit from where the client says they are.
 * <p>
 * MCP: {@code NetServerHandler#handleUseEntity(Packet7UseEntity)} calling {@code getDistanceSqToEntity}.
 * {@code require = 0}: without it hitboxes still work, large entities are just limited to vanilla's 6 block check.
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_AzHitboxReach {

    @Redirect(
        method = "handleInteractEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/entity/mob/player/ServerPlayerEntity;squaredDistanceTo(Lnet/minecraft/entity/Entity;)D"
        ),
        require = 0
    )
    private double azurelib$hitboxReach(ServerPlayerEntity player, Entity target) {
        double vanilla = Mc.distanceSq(player, target);

        if (!(target instanceof AzHitboxHolder)) {
            return vanilla;
        }

        Box box = AzHitboxes.getInteractionBox(target, Mc.boundingBox(target));
        return Math.min(vanilla, AzHitboxMath.distanceToSqr(box, AzHitboxMath.eyePosition(player)));
    }
}
