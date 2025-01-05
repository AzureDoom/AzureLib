/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.neoforge.platform;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.sblforked.SBLLoader;
import mod.azure.azurelib.sblforked.api.core.sensor.ExtendedSensor;
import mod.azure.azurelib.sblforked.registry.SBLMemoryTypes;
import mod.azure.azurelib.sblforked.registry.SBLSensors;

public class NeoForgeSBLForked implements SBLLoader {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(
        Registries.MEMORY_MODULE_TYPE,
        AzureLib.MOD_ID
    );

    public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(
        Registries.SENSOR_TYPE,
        AzureLib.MOD_ID
    );

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
        Registries.ENTITY_TYPE,
        AzureLib.MOD_ID
    );

    public void init(Object eventBus) {
        final IEventBus modEventBus = (IEventBus) eventBus;

        MEMORY_TYPES.register(modEventBus);
        SENSORS.register(modEventBus);

        SBLMemoryTypes.init();
        SBLSensors.init();
    }

    @Override
    public boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }

    @Override
    public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id) {
        return registerMemoryType(id, Optional.empty());
    }

    @Override
    public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id, Optional<Codec<T>> codec) {
        return MEMORY_TYPES.register(id, () -> new MemoryModuleType<>(codec));
    }

    @Override
    public <T extends ExtendedSensor<?>> Supplier<SensorType<T>> registerSensorType(String id, Supplier<T> sensor) {
        return SENSORS.register(id, () -> new SensorType<>(sensor));
    }
}
