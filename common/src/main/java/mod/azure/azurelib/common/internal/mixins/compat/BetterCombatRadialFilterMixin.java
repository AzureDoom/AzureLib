package mod.azure.azurelib.common.internal.mixins.compat;

import net.bettercombat.BetterCombatMod;
import net.bettercombat.client.collision.CollisionHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import mod.azure.azurelib.common.compat.bettercombat.AzBetterCombatHitboxHandler;

@Mixin(targets = "net.bettercombat.client.collision.TargetFinder$RadialFilter", remap = false)
public abstract class BetterCombatRadialFilterMixin {

    @Shadow
    @Final
    private Vec3 origin;

    @Shadow
    @Final
    private Vec3 orientation;

    @Shadow
    @Final
    private double attackRange;

    @Shadow
    @Final
    private double attackAngle;

    @Shadow
    private static boolean rayContainsNoObstacle(Vec3 start, Vec3 end) {
        throw new AssertionError();
    }

    @Inject(method = "filter(Ljava/util/List;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void azurelib$radialFilterAnimatedHitboxes(
        List<Entity> entities,
        CallbackInfoReturnable<List<Entity>> cir
    ) {
        var hits = AzBetterCombatHitboxHandler.collisionHits();

        if (hits.isEmpty()) {
            return;
        }

        var result = new ArrayList<Entity>(entities.size());

        for (var entity : entities) {
            var boxes = hits.get(entity);
            boolean passes;

            if (boxes == null) {
                var bb = entity.getBoundingBox();
                passes = azurelib$passes(
                    CollisionHelper.distanceVector(origin, bb),
                    entity.position().add(0, entity.getBbHeight() / 2.0, 0).subtract(origin)
                );
            } else {
                passes = false;

                for (var box : boxes) {
                    if (azurelib$passes(box.closestPoint(origin).subtract(origin), box.center().subtract(origin))) {
                        passes = true;
                        break;
                    }
                }
            }

            if (passes) {
                result.add(entity);
            }
        }

        hits.clear();
        cir.setReturnValue(result);
    }

    @Unique
    private boolean azurelib$passes(Vec3 distanceVector, Vec3 positionVector) {
        double maxAngleDif = attackAngle / 2.0;

        return distanceVector.length() <= attackRange
            && (attackAngle == 0
                || CollisionHelper.angleBetween(positionVector, orientation) <= maxAngleDif
                || CollisionHelper.angleBetween(distanceVector, orientation) <= maxAngleDif)
            && (BetterCombatMod.config.allow_attacking_thru_walls
                || rayContainsNoObstacle(origin, origin.add(distanceVector))
                || rayContainsNoObstacle(origin, origin.add(positionVector)));
    }
}
