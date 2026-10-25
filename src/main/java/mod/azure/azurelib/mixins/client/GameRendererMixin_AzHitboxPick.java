package mod.azure.azurelib.mixins.client;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
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

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin_AzHitboxPick {

    @Inject(method = "updateTargetedEntity", at = @At("HEAD"), require = 1)
    private void azurelib$beginPick(float partialTicks, CallbackInfo ci) {
        AzHitboxCollision.begin(AzHitboxCollision.Query.PICK, null);
    }

    @SuppressWarnings("rawtypes")
    @Redirect(
        method = "updateTargetedEntity", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;"
        ), require = 1
    )
    private List azurelib$widenCandidateSearch(World world, Entity box, Box area) {
        return AzHitboxCollision.findCandidates(area, box1 -> world.getEntities(box, box1));
    }

    @Redirect(
        method = "updateTargetedEntity", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;boundingBox:Lnet/minecraft/util/math/Box;",
            opcode = Opcodes.GETFIELD
        ), require = 1
    )
    private Box azurelib$captureCandidate(Entity candidate) {
        return AzHitboxCollision.captureCandidate(candidate);
    }

    @Redirect(
        method = "updateTargetedEntity", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/Box;raycast(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/hit/HitResult;"
        ), require = 1
    )
    private HitResult azurelib$clipHitboxes(Box box, Vec3d max, Vec3d to) {
        return AzHitboxCollision.clip(box, max, to, () -> box.raycast(max, to));
    }

    @Redirect(
        method = "updateTargetedEntity", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/Box;contains(Lnet/minecraft/util/math/Vec3d;)Z"
        ), require = 1
    )
    private boolean azurelib$containsHitboxes(Box box, Vec3d point) {
        return AzHitboxCollision.contains(box, point, () -> box.contains(point));
    }

    @Inject(method = "updateTargetedEntity", at = @At("RETURN"), require = 1)
    private void azurelib$endPick(float partialTicks, CallbackInfo ci) {
        AzHitboxCollision.endQuery();
    }
}
