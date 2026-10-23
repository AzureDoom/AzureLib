package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mod.azure.azurelib.hitbox.AzHitboxHolder;
import mod.azure.azurelib.hitbox.AzHitboxes;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin_AzHitboxReach {

    @Redirect(
        method = "processUseEntity", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayerMP;getDistanceSq(Lnet/minecraft/entity/Entity;)D"
        )
    )
    private double azurelib$hitboxReach(EntityPlayerMP player, Entity target) {
        double vanilla = player.getDistanceSq(target);

        if (!(target instanceof AzHitboxHolder)) {
            return vanilla;
        }

        AxisAlignedBB box = AzHitboxes.getInteractionBox(target, target.getEntityBoundingBox());
        Vec3d eye = player.getPositionEyes(1f);
        double dx = Math.max(Math.max(box.minX - eye.x, 0), eye.x - box.maxX);
        double dy = Math.max(Math.max(box.minY - eye.y, 0), eye.y - box.maxY);
        double dz = Math.max(Math.max(box.minZ - eye.z, 0), eye.z - box.maxZ);

        return Math.min(vanilla, dx * dx + dy * dy + dz * dz);
    }
}
