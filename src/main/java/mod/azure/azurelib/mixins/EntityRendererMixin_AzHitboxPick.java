package mod.azure.azurelib.mixins;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import mod.azure.azurelib.hitbox.AzHitboxCollision;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin_AzHitboxPick {

    @Inject(method = "getMouseOver", at = @At("HEAD"))
    private void azurelib$beginPick(float partialTicks, CallbackInfo ci) {
        AzHitboxCollision.begin(AzHitboxCollision.Query.PICK, null);
    }

    @SuppressWarnings("rawtypes")
    @Redirect(
        method = "getMouseOver", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;"
        )
    )
    private List azurelib$widenCandidateSearch(WorldClient world, Entity except, AxisAlignedBB area) {
        return AzHitboxCollision.findCandidates(area, box -> world.getEntitiesWithinAABBExcludingEntity(except, box));
    }

    @Redirect(
        method = "getMouseOver", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;boundingBox:Lnet/minecraft/util/AxisAlignedBB;",
            opcode = Opcodes.GETFIELD
        )
    )
    private AxisAlignedBB azurelib$captureCandidate(Entity candidate) {
        return AzHitboxCollision.captureCandidate(candidate);
    }

    @Redirect(
        method = "getMouseOver", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/AxisAlignedBB;calculateIntercept(Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;)Lnet/minecraft/util/MovingObjectPosition;"
        )
    )
    private MovingObjectPosition azurelib$clipHitboxes(AxisAlignedBB box, Vec3 from, Vec3 to) {
        return AzHitboxCollision.clip(box, from, to, () -> box.calculateIntercept(from, to));
    }

    @Redirect(
        method = "getMouseOver", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/AxisAlignedBB;isVecInside(Lnet/minecraft/util/Vec3;)Z"
        )
    )
    private boolean azurelib$containsHitboxes(AxisAlignedBB box, Vec3 point) {
        return AzHitboxCollision.contains(box, point, () -> box.isVecInside(point));
    }

    @Inject(method = "getMouseOver", at = @At("RETURN"))
    private void azurelib$endPick(float partialTicks, CallbackInfo ci) {
        AzHitboxCollision.endQuery();
    }
}
