/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;

import java.util.List;

import mod.azure.azurelib.sblforked.api.core.sensor.ExtendedSensor;
import mod.azure.azurelib.sblforked.registry.SBLSensors;
import mod.azure.azurelib.sblforked.util.BrainUtils;

/**
 * A replication of vanilla's {@link net.minecraft.world.entity.ai.sensing.PiglinBruteSpecificSensor}. Not really
 * useful, but included for completeness' sake and legibility. <br>
 * Keeps track of nearby {@link Piglin piglins} and {@link MemoryModuleType#NEAREST_VISIBLE_NEMESIS nemesis}
 *
 * @param <E> The entity
 */
public class PiglinBruteSpecificSensor<E extends LivingEntity> extends ExtendedSensor<E> {

    private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEARBY_ADULT_PIGLINS);

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return SBLSensors.PIGLIN_BRUTE_SPECIFIC.get();
    }

    @Override
    protected void doTick(ServerLevel level, E entity) {
        Brain<?> brain = entity.getBrain();
        List<AbstractPiglin> nearbyPiglins = new ObjectArrayList<>();

        BrainUtils.withMemory(
            brain,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            entities -> BrainUtils.setMemory(
                brain,
                MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
                (Mob) entities.findClosest(target -> target instanceof WitherSkeleton || target instanceof WitherBoss)
                    .orElse(null)
            )
        );
        BrainUtils.withMemory(brain, MemoryModuleType.NEAREST_LIVING_ENTITIES, entities -> {
            for (LivingEntity target : entities) {
                if (target instanceof AbstractPiglin piglin && piglin.isAdult())
                    nearbyPiglins.add(piglin);
            }
        });
        BrainUtils.setMemory(brain, MemoryModuleType.NEARBY_ADULT_PIGLINS, nearbyPiglins);
    }
}
