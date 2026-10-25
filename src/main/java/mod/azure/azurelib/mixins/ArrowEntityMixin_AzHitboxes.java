package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HitResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.hitbox.AzHitboxCollision;
import mod.azure.azurelib.platform.mc.Mc;

/**
 * Arrows hit bone hitboxes. MCP: {@code EntityArrow}.
 * <p>
 * Widen the entity search by the largest hitbox overhang, remember which candidate's box is being read, and swap the
 * candidate's box clip for a hitbox raycast. The hurt itself happens inside this tick, which is how
 * {@link mod.azure.azurelib.hitbox.AzHitboxDamageTracker} ties it to this projectile.
 * <p>
 * MCP names: {@code onUpdate}, {@code World#getEntitiesWithinAABBExcludingEntity}, {@code Entity#boundingBox},
 * {@code AxisAlignedBB#func_1169_a} (calculateIntercept).
 */
@Mixin(ArrowEntity.class)
public abstract class ArrowEntityMixin_AzHitboxes {

    @Inject(method = "tick", at = @At("HEAD"))
    private void azurelib$beginHitboxQuery(CallbackInfo ci) {
        AzHitboxCollision.begin(AzHitboxCollision.Query.PROJECTILE, (Entity) (Object) this);
    }

    @ModifyArg(
        method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;"
        ), index = 1
    )
    private Box azurelib$widenCandidateSearch(Box area) {
        return AzHitboxCollision.widenSearch(area);
    }

    @Redirect(
        method = "tick", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;shape:Lnet/minecraft/util/math/Box;",
            opcode = Opcodes.GETFIELD
        )
    )
    private Box azurelib$captureCandidate(Entity candidate) {
        return AzHitboxCollision.captureCandidate(candidate);
    }

    @Redirect(
        method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/Box;clip(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/world/HitResult;"
        )
    )
    private HitResult azurelib$clipHitboxes(Box box, Vec3d from, Vec3d _to) {
        return AzHitboxCollision.clip(box, from, _to, () -> Mc.clipBox(box, from, _to));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void azurelib$endHitboxQuery(CallbackInfo ci) {
        AzHitboxCollision.endQuery();
    }
}
