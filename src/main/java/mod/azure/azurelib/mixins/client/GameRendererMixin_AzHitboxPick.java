package mod.azure.azurelib.mixins.client;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
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
 * Crosshair picking against bone hitboxes.
 * <p>
 * MCP: {@code EntityRenderer#getMouseOver(float)}; Beta's client world is a plain {@code World}.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin_AzHitboxPick {

    @Inject(method = "pick", at = @At("HEAD"))
    private void azurelib$beginPick(float tickDelta, CallbackInfo ci) {
        AzHitboxCollision.begin(AzHitboxCollision.Query.PICK, null);
    }

    @ModifyArg(
        method = "pick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;"
        ), index = 1
    )
    private Box azurelib$widenCandidateSearch(Box area) {
        return AzHitboxCollision.widenSearch(area);
    }

    @Redirect(
        method = "pick", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;shape:Lnet/minecraft/util/math/Box;",
            opcode = Opcodes.GETFIELD
        )
    )
    private Box azurelib$captureCandidate(Entity candidate) {
        return AzHitboxCollision.captureCandidate(candidate);
    }

    @Redirect(
        method = "pick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/Box;clip(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/world/HitResult;"
        )
    )
    private HitResult azurelib$clipHitboxes(Box box, Vec3d from, Vec3d _to) {
        return AzHitboxCollision.clip(box, from, _to, () -> Mc.clipBox(box, from, _to));
    }

    @Redirect(
        method = "pick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/Box;contains(Lnet/minecraft/util/math/Vec3d;)Z"
        )
    )
    private boolean azurelib$containsHitboxes(Box box, Vec3d vec) {
        return AzHitboxCollision.contains(box, vec, () -> Mc.boxContains(box, vec));
    }

    @Inject(method = "pick", at = @At("RETURN"))
    private void azurelib$endPick(float tickDelta, CallbackInfo ci) {
        AzHitboxCollision.endQuery();
    }
}
