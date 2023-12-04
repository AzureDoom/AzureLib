package mod.azure.azurelib.platform;

import mod.azure.azurelib.NeoForgeAzureLibMod;
import mod.azure.azurelib.entities.TickingLightBlock;
import mod.azure.azurelib.entities.TickingLightEntity;
import mod.azure.azurelib.platform.services.IPlatformHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public Path getGameDir() {
        return FMLLoader.getGamePath();
    }

    @Override
    public boolean isServerEnvironment() {
        return FMLEnvironment.dist.isDedicatedServer();
    }

    @Override
    public TickingLightBlock getTickingLightBlock() {
        return (TickingLightBlock) NeoForgeAzureLibMod.AzureBlocks.TICKING_LIGHT_BLOCK.get();
    }

    @Override
    public BlockEntityType<TickingLightEntity> getTickingLightEntity() {
        return NeoForgeAzureLibMod.AzureEntities.TICKING_LIGHT_ENTITY.get();
    }

    @Override
    public Enchantment getIncendairyenchament() {
        return NeoForgeAzureLibMod.AzureEnchantments.INCENDIARYENCHANTMENT.get();
    }

    @Override
    public Path modsDir() {
        return FMLPaths.MODSDIR.get();
    }
}