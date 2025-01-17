/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.fabric.platform;

import com.mojang.serialization.Codec;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.Optional;
import java.util.function.Supplier;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.sblforked.SBLLoader;
import mod.azure.azurelib.sblforked.api.core.sensor.ExtendedSensor;
import mod.azure.azurelib.sblforked.registry.SBLMemoryTypes;
import mod.azure.azurelib.sblforked.registry.SBLSensors;

public class FabricSBLForked implements SBLLoader {

    public void init(Object eventBus) {
        SBLMemoryTypes.init();
        SBLSensors.init();
    }

    @Override
    public boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id) {
        return registerMemoryType(id, Optional.empty());
    }

    @Override
    public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id, Optional<Codec<T>> codec) {
        MemoryModuleType<T> memoryType = Registry.register(
            BuiltInRegistries.MEMORY_MODULE_TYPE,
            ResourceLocation.fromNamespaceAndPath(AzureLib.MOD_ID, id),
            new MemoryModuleType<>(codec)
        );

        return () -> memoryType;
    }

    @Override
    public <T extends ExtendedSensor<?>> Supplier<SensorType<T>> registerSensorType(String id, Supplier<T> sensor) {
        SensorType<T> sensorType = Registry.register(
            BuiltInRegistries.SENSOR_TYPE,
            ResourceLocation.fromNamespaceAndPath(AzureLib.MOD_ID, id),
            new SensorType<>(sensor)
        );

        return () -> sensorType;
    }
}
