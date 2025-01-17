/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.api.core.behaviour.custom.move;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import mod.azure.azurelib.sblforked.util.BrainUtils;

/**
 * Extension of MoveToWalkTarget, but auto-marking the sprinting flag depending on the movespeed. This can be useful for
 * using sprint animations on the client.
 *
 * @param <E>
 */
public class WalkOrRunToWalkTarget<E extends PathfinderMob> extends MoveToWalkTarget<E> {

    @Override
    protected void startOnNewPath(E entity) {
        BrainUtils.setMemory(entity, MemoryModuleType.PATH, this.path);

        if (entity.getNavigation().moveTo(this.path, this.speedModifier))
            entity.setSharedFlag(3, this.speedModifier > 1);
    }

    @Override
    protected void stop(E entity) {
        super.stop(entity);

        entity.setSharedFlag(3, false);
    }
}
