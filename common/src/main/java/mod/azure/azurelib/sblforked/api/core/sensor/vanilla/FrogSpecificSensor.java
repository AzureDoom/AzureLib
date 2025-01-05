/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.api.core.sensor.vanilla;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.frog.Frog;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;

import mod.azure.azurelib.sblforked.api.core.sensor.EntityFilteringSensor;
import mod.azure.azurelib.sblforked.api.core.sensor.ExtendedSensor;
import mod.azure.azurelib.sblforked.registry.SBLSensors;
import mod.azure.azurelib.sblforked.util.BrainUtils;

/**
 * A replication of vanilla's {@link net.minecraft.world.entity.ai.sensing.FrogAttackablesSensor}. Not really useful,
 * but included for completeness' sake and legibility. <br>
 * Handles the Frog's tongue target.
 *
 * @param <E> The entity
 */
public class FrogSpecificSensor<E extends LivingEntity> extends EntityFilteringSensor<LivingEntity, E> {

    @Override
    public MemoryModuleType<LivingEntity> getMemory() {
        return MemoryModuleType.NEAREST_ATTACKABLE;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return SBLSensors.FROG_SPECIFIC.get();
    }

    @Override
    protected BiPredicate<LivingEntity, E> predicate() {
        return (target, entity) -> {
            if (BrainUtils.hasMemory(entity, MemoryModuleType.HAS_HUNTING_COOLDOWN))
                return false;

            if (!Sensor.isEntityAttackable(entity, target))
                return false;

            if (!Frog.canEat(target))
                return false;

            if (!target.closerThan(entity, 10))
                return false;

            List<UUID> unreachableTargets = BrainUtils.getMemory(entity, MemoryModuleType.UNREACHABLE_TONGUE_TARGETS);

            return unreachableTargets == null || !unreachableTargets.contains(target.getUUID());
        };
    }

    @Nullable
    @Override
    protected LivingEntity findMatches(E entity, NearestVisibleLivingEntities matcher) {
        return matcher.findClosest(target -> predicate().test(target, entity)).orElse(null);
    }
}
