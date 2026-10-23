package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import mod.azure.azurelib.hitbox.AzHitboxCollision;

@Mixin(EntityArrow.class)
public abstract class EntityArrowMixin_AzHitboxes {

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void azurelib$beginHitboxQuery(CallbackInfo ci) {
        AzHitboxCollision.begin(AzHitboxCollision.Query.PROJECTILE, (Entity) (Object) this);
    }

    @SuppressWarnings("rawtypes")
    @Redirect(
        method = "onUpdate", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;"
        )
    )
    private List azurelib$widenCandidateSearch(World world, Entity except, AxisAlignedBB area) {
        return AzHitboxCollision.findCandidates(area, box -> world.getEntitiesWithinAABBExcludingEntity(except, box));
    }

    @Redirect(
        method = "onUpdate", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;boundingBox:Lnet/minecraft/util/AxisAlignedBB;",
            opcode = Opcodes.GETFIELD
        )
    )
    private AxisAlignedBB azurelib$captureCandidate(Entity candidate) {
        return AzHitboxCollision.captureCandidate(candidate);
    }

    @Redirect(
        method = "onUpdate", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/AxisAlignedBB;calculateIntercept(Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;)Lnet/minecraft/util/MovingObjectPosition;"
        )
    )
    private MovingObjectPosition azurelib$clipHitboxes(AxisAlignedBB box, Vec3 from, Vec3 to) {
        return AzHitboxCollision.clip(box, from, to, () -> box.calculateIntercept(from, to));
    }

    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void azurelib$endHitboxQuery(CallbackInfo ci) {
        AzHitboxCollision.endQuery();
    }
}
