package mod.azure.azurelib.mixins.compat;

import net.bettercombat.BetterCombat;
import net.bettercombat.logic.TargetHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

import mod.azure.azurelib.hitbox.AzHitboxes;

@Mixin(targets = "net.bettercombat.client.collision.TargetFinder", remap = false)
public abstract class BetterCombatInitialTargetsMixin {

    @Inject(method = "getInitialTargets", at = @At("RETURN"))
    private static void azurelib$includeHitboxOverhang(
        Player player,
        Entity cursorTarget,
        double attackRange,
        CallbackInfoReturnable<List<Entity>> cir
    ) {
        var overhang = AzHitboxes.maxOverhang();

        if (overhang <= 0) {
            return;
        }

        var entities = cir.getReturnValue();
        var searchBox = player.getBoundingBox()
            .inflate(attackRange * BetterCombat.config.target_search_range_multiplier + 1.0);

        var known = Collections.newSetFromMap(new IdentityHashMap<Entity, Boolean>());
        known.addAll(entities);

        var wider = player.level
            .getEntities(player, searchBox.inflate(overhang), e -> !e.isSpectator() && e.isPickable());

        for (var entity : wider) {
            if (known.contains(entity) || entity == cursorTarget) {
                continue;
            }

            if (entity.getBoundingBox().intersects(searchBox)) {
                continue;
            }

            if (
                !AzHitboxes.hasLivePose(entity)
                    || !AzHitboxes.getCullingBox(entity, entity.getBoundingBox()).intersects(searchBox)
            ) {
                continue;
            }

            if (
                !entity.isAttackable()
                    || (entity.equals(player.getVehicle()) && !TargetHelper.isAttackableMount(entity))
                    || TargetHelper.getRelation(player, entity) != TargetHelper.Relation.HOSTILE
            ) {
                continue;
            }

            entities.add(entity);
        }
    }
}
