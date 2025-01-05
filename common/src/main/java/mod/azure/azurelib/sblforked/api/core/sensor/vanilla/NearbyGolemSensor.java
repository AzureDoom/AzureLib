/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.List;

import mod.azure.azurelib.sblforked.api.core.sensor.ExtendedSensor;
import mod.azure.azurelib.sblforked.api.core.sensor.PredicateSensor;
import mod.azure.azurelib.sblforked.registry.SBLSensors;
import mod.azure.azurelib.sblforked.util.BrainUtils;

/**
 * A sensor that sets the {@link MemoryModuleType#GOLEM_DETECTED_RECENTLY} memory by checking if any of the detected
 * nearby entities are {@link net.minecraft.world.entity.animal.IronGolem Iron Golems}. <br>
 * Defaults:
 * <ul>
 * <li>200-tick scan rate</li>
 * <li>Only detects vanilla Iron Golems</li>
 * <li>Remembers the nearby golem for 600 ticks</li>
 * </ul>
 *
 * @see net.minecraft.world.entity.ai.sensing.GolemSensor
 * @param <E> The entity
 */
public class NearbyGolemSensor<E extends LivingEntity> extends PredicateSensor<LivingEntity, E> {

    private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(
        MemoryModuleType.GOLEM_DETECTED_RECENTLY
    );

    private int timeToRemember = 600;

    public NearbyGolemSensor() {
        setScanRate(entity -> 200);
        setPredicate((target, entity) -> target.getType() == EntityType.IRON_GOLEM && target.isAlive());
    }

    /**
     * Set the amount of ticks the entity should remember that the golem is there.
     *
     * @param ticks The number of ticks to remember for
     * @return this
     */
    public NearbyGolemSensor<E> setMemoryTime(int ticks) {
        this.timeToRemember = ticks;

        return this;
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return SBLSensors.NEARBY_GOLEM.get();
    }

    @Override
    protected void doTick(ServerLevel level, E entity) {
        BrainUtils.withMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, entityList -> {
            if (entityList.isEmpty())
                return;

            for (LivingEntity target : entityList) {
                if (predicate().test(target, entity)) {
                    BrainUtils.setForgettableMemory(
                        entity,
                        MemoryModuleType.GOLEM_DETECTED_RECENTLY,
                        true,
                        this.timeToRemember
                    );

                    return;
                }
            }
        });
    }
}
