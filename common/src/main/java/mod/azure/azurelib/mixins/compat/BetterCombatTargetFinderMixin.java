
package mod.azure.azurelib.mixins.compat;

import net.bettercombat.client.collision.OrientedBoundingBox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

import mod.azure.azurelib.compat.bettercombat.AzBetterCombatHitboxHandler;
import mod.azure.azurelib.hitbox.AzHitboxes;

@Mixin(
    targets = "net.bettercombat.client.collision.TargetFinder$CollisionFilter",
    remap = false
)
public abstract class BetterCombatTargetFinderMixin {

    @Shadow(remap = false)
    private OrientedBoundingBox obb;

    @Inject(
        method = "filter(Ljava/util/List;)Ljava/util/List;",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void azurelib$filterAnimatedHitboxes(
        List<Entity> entities,
        CallbackInfoReturnable<List<Entity>> cir
    ) {
        List<Entity> result = entities.stream()
            .filter(entity -> {
                if (!AzHitboxes.hasLivePose(entity)) {
                    return obb.intersects(
                        entity.getBoundingBox()
                            .inflate(
                                entity.getPickRadius()
                            )
                    ) || obb.contains(
                        new Vec3(
                            entity.getX(),
                            entity.getY() + entity.getBbHeight() / 2.0,
                            entity.getZ()
                        )
                    );
                }

                return AzBetterCombatHitboxHandler
                    .findHit(entity, obb) != null;
            })
            .collect(Collectors.toList());

        cir.setReturnValue(result);
    }
}
