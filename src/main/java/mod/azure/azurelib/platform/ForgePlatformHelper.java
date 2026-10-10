package mod.azure.azurelib.platform;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.nio.file.Path;

import mod.azure.azurelib.platform.services.IPlatformHelper;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return Loader.isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        Object deobf = Launch.blackboard.get("fml.deobfuscatedEnvironment");
        return deobf instanceof Boolean && (Boolean) deobf;
    }

    @Override
    public Path getGameDir() {
        File home = Launch.minecraftHome;
        return (home != null ? home : new File(".")).toPath();
    }

    @Override
    public boolean isServerEnvironment() {
        return FMLCommonHandler.instance().getSide().isServer();
    }

    @Override
    public Path modsDir() {
        return getGameDir().resolve("mods");
    }
}
