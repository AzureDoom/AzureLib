/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.api.core.behaviour.custom.move;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

/**
 * A movement behaviour for automatically following the owner of a {@link TamableAnimal TameableAnimal}.<br>
 *
 * @param <E> The owner of the brain
 */
public class FollowOwner<E extends TamableAnimal> extends FollowEntity<E, LivingEntity> {

    protected LivingEntity owner = null;

    public FollowOwner() {
        following(this::getOwner);
        teleportToTargetAfter(12);
    }

    protected LivingEntity getOwner(E entity) {
        if (this.owner == null)
            this.owner = entity.getOwner();

        if (this.owner != null && this.owner.isRemoved())
            this.owner = null;

        return this.owner;
    }
}
