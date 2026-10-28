package mod.azure.azurelib.animation.molang;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Supplies the attack target for {@code query.has_target}, {@code query.target_x_rotation} and
 * {@code query.target_y_rotation}.
 * <p>
 * Animations run on the client, where {@link net.minecraft.world.entity.Mob#getTarget()} is normally {@code null}:
 * vanilla never syncs a mob's target. Implement this on your entity and return a target you sync yourself, typically an
 * entity id kept in {@link net.minecraft.network.syncher.SynchedEntityData}:
 *
 * <pre>{@code
 *
 * @Override
 * public @Nullable Entity getMolangTarget() {
 *     int id = entityData.get(TARGET_ID);
 *     return id < 0 ? null : level().getEntity(id);
 * }
 * }</pre>
 * <p>
 * Entities that don't implement this fall back to {@code Mob#getTarget()}, which works wherever the target is known
 * client-side.
 * </p>
 */
public interface AzMolangTargetProvider {

    /**
     * @return the entity this one is attacking, or {@code null} if it has none
     */
    @Nullable
    Entity getMolangTarget();
}
