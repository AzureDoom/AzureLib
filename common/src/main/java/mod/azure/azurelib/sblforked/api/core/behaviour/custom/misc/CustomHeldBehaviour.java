/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.api.core.behaviour.custom.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import mod.azure.azurelib.sblforked.api.core.behaviour.ExtendedBehaviour;
import mod.azure.azurelib.sblforked.api.core.behaviour.HeldBehaviour;

/**
 * A behaviour module that invokes a callback every tick until stopped.<br>
 * Useful for handling custom minor actions that are either too specific to warrant a new behaviour, or not worth
 * implementing into a full behaviour.<br>
 * Set the condition for running via {@link ExtendedBehaviour#startCondition(Predicate)}<br>
 * Set the condition for stopping via {@link ExtendedBehaviour#stopIf(Predicate)}
 */
public final class CustomHeldBehaviour<E extends LivingEntity> extends HeldBehaviour<E> {

    private Consumer<E> callback;

    public CustomHeldBehaviour(Consumer<E> callback) {
        this.callback = callback;
    }

    /**
     * Replace the callback function
     *
     * @return this
     */
    public CustomHeldBehaviour<E> callback(Consumer<E> callback) {
        this.callback = callback;

        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of();
    }

    @Override
    protected void tick(E entity) {
        this.callback.accept(entity);
    }
}
