package mod.azure.azurelib.neoforge.platform;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;

import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import mod.azure.azurelib.neoforge.NeoForgeAzureLibMod;
import mod.azure.azurelib.platform.services.IPlatformHelper;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Neoforge";
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Path getGameDir() {
        return FMLLoader.getCurrent().getGameDir();
    }

    @Override
    public boolean isServerEnvironment() {
        return FMLEnvironment.getDist().isDedicatedServer();
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponent(
        String id,
        UnaryOperator<DataComponentType.Builder<T>> builder
    ) {
        return NeoForgeAzureLibMod.DATA_COMPONENTS_REGISTER.registerComponentType(id, builder);
    }

    @Override
    public boolean isEnvironmentClient() {
        return FMLEnvironment.getDist().isClient();
    }
}
