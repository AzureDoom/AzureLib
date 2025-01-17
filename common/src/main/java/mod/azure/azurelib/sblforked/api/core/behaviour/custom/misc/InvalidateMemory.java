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
import java.util.function.BiPredicate;

import mod.azure.azurelib.sblforked.api.core.behaviour.ExtendedBehaviour;
import mod.azure.azurelib.sblforked.util.BrainUtils;

/**
 * Custom behaviour for conditionally invalidating/resetting existing memories.<br>
 * This allows for custom handling of stored memories, and clearing them at will.<br>
 * <br>
 * Invalidates the memory unconditionally once the behaviour runs. Use {@link InvalidateMemory#invalidateIf} and
 * {@link ExtendedBehaviour#startCondition} to quantify its operating conditions
 *
 * @param <E> The brain owner
 * @param <M> The data type of the memory
 */
public class InvalidateMemory<E extends LivingEntity, M> extends ExtendedBehaviour<E> {

    private List<Pair<MemoryModuleType<?>, MemoryStatus>> memoryRequirements;

    protected BiPredicate<E, M> customPredicate = (entity, target) -> true;

    protected MemoryModuleType<M> memory;

    public InvalidateMemory(MemoryModuleType<M> memory) {
        super();

        this.memory = memory;
        this.memoryRequirements = List.of(Pair.of(this.memory, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return this.memoryRequirements == null ? List.of() : this.memoryRequirements;
    }

    /**
     * Sets the {@link MemoryModuleType memory} to check and invalidate.
     */
    public InvalidateMemory<E, M> forMemory(MemoryModuleType<M> memory) {
        this.memory = memory;
        this.memoryRequirements = List.of(Pair.of(this.memory, MemoryStatus.VALUE_PRESENT));

        return this;
    }

    /**
     * Sets a custom predicate to invalidate the memory if none of the previous checks invalidate it first.
     */
    public InvalidateMemory<E, M> invalidateIf(BiPredicate<E, M> predicate) {
        this.customPredicate = predicate;

        return this;
    }

    @Override
    protected void start(E entity) {
        M memory = BrainUtils.getMemory(entity, this.memory);

        if (memory != null && this.customPredicate.test(entity, memory))
            BrainUtils.clearMemory(entity, this.memory);
    }
}
