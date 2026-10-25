package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mod.azure.azurelib.hitbox.AzHitboxHolder;
import mod.azure.azurelib.hitbox.AzHitboxMath;
import mod.azure.azurelib.hitbox.AzHitboxes;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_AzHitboxReach {

    @Redirect(
        method = "handleInteractEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/ServerPlayerEntity;getSquaredDistance(Lnet/minecraft/entity/Entity;)D"
        ),
        require = 1
    )
    private double azurelib$hitboxReach(ServerPlayerEntity player, Entity target) {
        double vanilla = player.getSquaredDistance(target);

        if (!(target instanceof AzHitboxHolder)) {
            return vanilla;
        }

        Box box = AzHitboxes.getInteractionBox(target, target.boundingBox);
        return Math.min(vanilla, AzHitboxMath.distanceToSqr(box, AzHitboxMath.eyePosition(player)));
    }
}
