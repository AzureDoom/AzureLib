package mod.azure.azurelib.animation.molang;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;

/**
 * Supplies the attack target for {@code query.has_target}, {@code query.target_x_rotation} and
 * {@code query.target_y_rotation}.
 * <p>
 * Animations run on the client, where {@link EntityMob#getAttackTarget()} is normally {@code null}: vanilla never syncs
 * a mob's target. Implement this on your entity and return a target you sync yourself, typically an entity ID kept in
 * {@link net.minecraft.entity.DataWatcher}:
 *
 * <pre>
 * {@code
 *
 * &#64;Override
 * public Entity getMolangTarget() {
 *     int id = dataWatcher.getWatchableObjectInt(TARGET_ID);
 *     return id < 0 ? null : worldObj.getEntityByID(id);
 * }
 * }
 * </pre>
 * <p>
 * Entities that don't implement this fall back to {@link EntityLiving#getAttackTarget()}, which works wherever the
 * target is known client-side.
 * </p>
 */
public interface AzMolangTargetProvider {

    /**
     * @return the entity this one is attacking, or {@code null} if it has none
     */
    Entity getMolangTarget();
}
