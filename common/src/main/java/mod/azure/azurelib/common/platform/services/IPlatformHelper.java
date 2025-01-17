package mod.azure.azurelib.common.platform.services;

import net.minecraft.core.component.DataComponentType;

import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface IPlatformHelper {

    String getPlatformName();

    boolean isDevelopmentEnvironment();

    Path getGameDir();

    boolean isServerEnvironment();

    boolean isEnvironmentClient();

    <T> Supplier<DataComponentType<T>> registerDataComponent(
        String id,
        UnaryOperator<DataComponentType.Builder<T>> builder
    );
}
