package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import mod.azure.azurelib.hitbox.AzHitboxCollision;

@Mixin(ArrowEntity.class)
public abstract class ArrowEntityMixin_AzHitboxes {

    @Inject(method = "tick", at = @At("HEAD"))
    private void azurelib$beginHitboxQuery(CallbackInfo ci) {
        AzHitboxCollision.begin(AzHitboxCollision.Query.PROJECTILE, (Entity) (Object) this);
    }

    @SuppressWarnings("rawtypes")
    @Redirect(
        method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;"
        )
    )
    private List azurelib$widenCandidateSearch(World world, Entity box, Box area) {
        return AzHitboxCollision.findCandidates(area, box1 -> world.getEntities(box, box1));
    }

    @Redirect(
        method = "tick", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;boundingBox:Lnet/minecraft/util/math/Box;",
            opcode = Opcodes.GETFIELD
        )
    )
    private Box azurelib$captureCandidate(Entity candidate) {
        return AzHitboxCollision.captureCandidate(candidate);
    }

    @Redirect(
        method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/Box;raycast(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/hit/HitResult;"
        )
    )
    private HitResult azurelib$clipHitboxes(Box box, Vec3d max, Vec3d to) {
        return AzHitboxCollision.clip(box, max, to, () -> box.raycast(max, to));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void azurelib$endHitboxQuery(CallbackInfo ci) {
        AzHitboxCollision.endQuery();
    }
}
