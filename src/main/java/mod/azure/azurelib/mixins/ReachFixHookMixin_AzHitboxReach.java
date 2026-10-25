package mod.azure.azurelib.mixins.compat.reachfix;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import mod.azure.azurelib.hitbox.AzHitboxHolder;
import mod.azure.azurelib.hitbox.AzHitboxes;

@Pseudo
@Mixin(targets = "meldexun.reachfix.hook.NetHandlerPlayServerHook", remap = false)
public abstract class ReachFixHookMixin_AzHitboxReach {

    @Inject(
        method = "isEntityInRange",
        at = @At("RETURN"),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILSOFT,
        require = 0
    )
    private static void azurelib$hitboxReach(
        NetHandlerPlayServer serverHandler,
        Entity target,
        EnumHand hand,
        CallbackInfoReturnable<Boolean> cir,
        EntityPlayer player,
        AxisAlignedBB aabb,
        double distanceSq,
        double reach
    ) {
        if (cir.getReturnValueZ() || !(target instanceof AzHitboxHolder)) {
            return;
        }

        AxisAlignedBB box = AzHitboxes.getInteractionBox(target, aabb);
        if (box == aabb) {
            return;
        }

        Vec3d eye = player.getPositionEyes(1f);
        double dx = Math.max(Math.max(box.minX - eye.x, 0), eye.x - box.maxX);
        double dy = Math.max(Math.max(box.minY - eye.y, 0), eye.y - box.maxY);
        double dz = Math.max(Math.max(box.minZ - eye.z, 0), eye.z - box.maxZ);

        if (dx * dx + dy * dy + dz * dz < reach * reach) {
            cir.setReturnValue(true);
        }
    }
}
