/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.api.core.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.sblforked.object.SBLShufflingList;

/**
 * Group behaviour that attempts to run all sub-behaviours in order, until the first successful one.
 *
 * @param <E> The entity
 */
public final class FirstApplicableBehaviour<E extends LivingEntity> extends GroupBehaviour<E> {

    public FirstApplicableBehaviour(Pair<ExtendedBehaviour<? super E>, Integer>... behaviours) {
        super(behaviours);
    }

    public FirstApplicableBehaviour(ExtendedBehaviour<? super E>... behaviours) {
        super(behaviours);
    }

    @Nullable
    @Override
    protected ExtendedBehaviour<? super E> pickBehaviour(
        ServerLevel level,
        E entity,
        long gameTime,
        SBLShufflingList<ExtendedBehaviour<? super E>> extendedBehaviours
    ) {
        for (ExtendedBehaviour<? super E> behaviour : extendedBehaviours) {
            if (behaviour.tryStart(level, entity, gameTime))
                return behaviour;
        }

        return null;
    }
}
