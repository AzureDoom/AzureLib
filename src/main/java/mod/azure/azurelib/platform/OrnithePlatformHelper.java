package mod.azure.azurelib.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

import mod.azure.azurelib.platform.services.IPlatformHelper;

public class OrnithePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Ornithe";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
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
    public Path modsDir() {
        return getGameDir().resolve("mods");
    }
}
