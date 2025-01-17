package mod.azure.azurelib.fabric.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.platform.services.IPlatformHelper;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public boolean isServerEnvironment() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponent(
        String id,
        UnaryOperator<DataComponentType.Builder<T>> builder
    ) {
        final DataComponentType<T> componentType = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            AzureLib.modResource(id).toString(),
            builder.apply(DataComponentType.builder()).build()
        );

        return () -> componentType;
    }

    @Override
    public boolean isEnvironmentClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }
}
