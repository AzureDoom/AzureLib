/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.List;

import mod.azure.azurelib.sblforked.api.core.sensor.ExtendedSensor;
import mod.azure.azurelib.sblforked.api.core.sensor.PredicateSensor;
import mod.azure.azurelib.sblforked.registry.SBLSensors;
import mod.azure.azurelib.sblforked.util.BrainUtils;

/**
 * A sensor that sets or clears the {@link MemoryModuleType#IS_IN_WATER} memory depending on certain criteria. <br>
 * Defaults:
 * <ul>
 * <li>{@link LivingEntity#isInWater()}</li>
 * </ul>
 *
 * @param <E> The entity
 */
public class InWaterSensor<E extends LivingEntity> extends PredicateSensor<E, E> {

    private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.IS_IN_WATER);

    public InWaterSensor() {
        super((entity2, entity) -> entity.isInWater());
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return SBLSensors.IN_WATER.get();
    }

    @Override
    protected void doTick(ServerLevel level, E entity) {
        if (predicate().test(entity, entity)) {
            BrainUtils.setMemory(entity, MemoryModuleType.IS_IN_WATER, Unit.INSTANCE);
        } else {
            BrainUtils.clearMemory(entity, MemoryModuleType.IS_IN_WATER);
        }
    }
}
