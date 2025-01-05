/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.api.core.sensor.vanilla;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.SensorType;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;

import mod.azure.azurelib.sblforked.api.core.sensor.EntityFilteringSensor;
import mod.azure.azurelib.sblforked.api.core.sensor.ExtendedSensor;
import mod.azure.azurelib.sblforked.registry.SBLSensors;

/**
 * A sensor that sets the {@link MemoryModuleType#NEAREST_VISIBLE_ADULT} memory by checking the existing visible
 * entities for nearby adults of the same entity type. <br>
 *
 * @see net.minecraft.world.entity.ai.sensing.AdultSensor
 * @param <E> The entity
 */
public class NearbyAdultSensor<E extends AgeableMob> extends EntityFilteringSensor<AgeableMob, E> {

    @Override
    public MemoryModuleType<AgeableMob> getMemory() {
        return MemoryModuleType.NEAREST_VISIBLE_ADULT;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return SBLSensors.NEARBY_ADULT.get();
    }

    @Override
    protected BiPredicate<LivingEntity, E> predicate() {
        return (target, entity) -> target.getType() == entity.getType() && !target.isBaby();
    }

    @Nullable
    @Override
    protected AgeableMob findMatches(E entity, NearestVisibleLivingEntities matcher) {
        return (AgeableMob) matcher.findClosest(target -> predicate().test(target, entity)).orElse(null);
    }
}
