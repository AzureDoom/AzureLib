/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import mod.azure.azurelib.sblforked.api.core.sensor.ExtendedSensor;
import mod.azure.azurelib.sblforked.registry.SBLSensors;
import mod.azure.azurelib.sblforked.util.BrainUtils;

/**
 * A replication of vanilla's {@link net.minecraft.world.entity.ai.sensing.WardenEntitySensor}. Not really useful, but
 * included for completeness' sake and legibility. <br>
 * Handle's the Warden's nearest attackable target, prioritising players.
 *
 * @param <E> The entity
 */
public class WardenSpecificSensor<E extends Warden> extends NearbyLivingEntitySensor<E> {

    private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_ATTACKABLE);

    public WardenSpecificSensor() {
        setRadius(24);
        setPredicate((target, entity) -> entity.canTargetEntity(target));
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return SBLSensors.WARDEN_SPECIFIC.get();
    }

    @Override
    protected void doTick(ServerLevel level, E entity) {
        super.doTick(level, entity);

        BrainUtils.withMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, entities -> {
            LivingEntity fallbackTarget = null;

            for (LivingEntity target : entities) {
                if (target instanceof Player) {
                    BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_ATTACKABLE, target);

                    return;
                } else if (fallbackTarget == null) {
                    fallbackTarget = target;
                }
            }

            if (fallbackTarget != null) {
                BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_ATTACKABLE, fallbackTarget);
            } else {
                BrainUtils.clearMemory(entity, MemoryModuleType.NEAREST_ATTACKABLE);
            }
        });
    }
}
