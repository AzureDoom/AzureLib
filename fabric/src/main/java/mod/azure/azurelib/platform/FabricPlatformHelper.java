package mod.azure.azurelib.platform;

import mod.azure.azurelib.FabricAzureLibMod;
import mod.azure.azurelib.entities.TickingLightBlock;
import mod.azure.azurelib.entities.TickingLightEntity;
import mod.azure.azurelib.platform.services.IPlatformHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
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
    public TickingLightBlock getTickingLightBlock() {
        return FabricAzureLibMod.TICKING_LIGHT_BLOCK;
    }
    @Override
    public BlockEntityType<TickingLightEntity> getTickingLightEntity() {
        return FabricAzureLibMod.TICKING_LIGHT_ENTITY;
    }

    @Override
    public Enchantment getIncendairyenchament() {
        return FabricAzureLibMod.INCENDIARYENCHANTMENT;
    }
}