package mod.azure.azurelib.mixins.compat;

import net.bettercombat.client.collision.OrientedBoundingBox;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import mod.azure.azurelib.compat.bettercombat.AzBetterCombatHitboxHandler;

@Mixin(targets = "net.bettercombat.client.collision.TargetFinder$CollisionFilter", remap = false)
public abstract class BetterCombatCollisionFilterMixin {

    @Shadow
    private OrientedBoundingBox obb;

    @Inject(method = "filter(Ljava/util/List;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void azurelib$filterAnimatedHitboxes(List<Entity> entities, CallbackInfoReturnable<List<Entity>> cir) {
        var hits = AzBetterCombatHitboxHandler.collisionHits();
        hits.clear();

        var result = new ArrayList<Entity>(entities.size());

        for (var entity : entities) {
            var hitboxes = AzBetterCombatHitboxHandler.combatHitboxes(entity);

            if (hitboxes.isEmpty()) {
                if (azurelib$vanillaHit(entity)) {
                    result.add(entity);
                }
                continue;
            }

            var touched = AzBetterCombatHitboxHandler.intersecting(hitboxes, obb);

            if (!touched.isEmpty()) {
                hits.put(entity, touched);
                result.add(entity);
            }
        }

        cir.setReturnValue(result);
    }

    @Unique
    private boolean azurelib$vanillaHit(Entity entity) {
        return obb.intersects(entity.getBoundingBox().inflate(entity.getPickRadius()))
            || obb.contains(entity.position().add(0, entity.getBbHeight() / 2.0, 0));
    }
}
