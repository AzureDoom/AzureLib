package mod.azure.azurelib.animation.molang;

import net.minecraft.entity.Entity;

/**
 * Supplies the attack target for {@code query.has_target}, {@code query.target_x_rotation} and
 * {@code query.target_y_rotation}.
 * <p>
 * Animations run on the client, where a hostile mob's attack target is normally {@code null}: vanilla does not
 * automatically synchronize its AI target.
 * <p>
 * Implement this on your entity and return a target you synchronize yourself, typically an entity ID stored in the
 * entity's data tracker:
 *
 * <pre>
 * {@code
 *
 * &#64;Override
 * public Entity getMolangTarget() {
 *     int id = getSyncedMolangTargetId();
 *     return id < 0 ? null : findMolangTargetById(id);
 * }
 * }
 * </pre>
 * <p>
 * The helper methods above represent reading a synchronized entity ID and resolving it in the client world. Their
 * implementation depends on the mapping set and synchronization API available in the project.
 * <p>
 * Entities that don't implement this fall back to the mob's vanilla attack target, which works wherever that target is
 * known client-side.
 */
public interface AzMolangTargetProvider {

    /**
     * @return the entity this one is attacking, or {@code null} if it has none
     */
    Entity getMolangTarget();
}
