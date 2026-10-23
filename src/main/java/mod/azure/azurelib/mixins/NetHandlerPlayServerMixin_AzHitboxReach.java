package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mod.azure.azurelib.hitbox.AzHitboxHolder;
import mod.azure.azurelib.hitbox.AzHitboxMath;
import mod.azure.azurelib.hitbox.AzHitboxes;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin_AzHitboxReach {

    @Redirect(
        method = "processUseEntity", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayerMP;getDistanceSqToEntity(Lnet/minecraft/entity/Entity;)D"
        )
    )
    private double azurelib$hitboxReach(EntityPlayerMP player, Entity target) {
        double vanilla = player.getDistanceSqToEntity(target);

        if (!(target instanceof AzHitboxHolder)) {
            return vanilla;
        }

        AxisAlignedBB box = AzHitboxes.getInteractionBox(target, target.boundingBox);
        return Math.min(vanilla, AzHitboxMath.distanceToSqr(box, AzHitboxMath.eyePosition(player)));
    }
}
