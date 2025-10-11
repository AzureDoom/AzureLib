package mod.azure.azurelib.platform;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

import mod.azure.azurelib.ForgeAzureLibMod;
import mod.azure.azurelib.platform.services.CommonRegistry;

public class ForgeCommonRegistry implements CommonRegistry {

    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, String registryName, Supplier<? extends T> supplier) {
        if (registry == Registry.BLOCK) {
            return (Supplier<T>) ForgeAzureLibMod.BLOCKS.register(registryName, (Supplier<Block>) supplier);
        } else if (registry == Registry.BLOCK_ENTITY_TYPE) {
            return (Supplier<T>) ForgeAzureLibMod.TILE_TYPES.register(
                registryName,
                (Supplier<BlockEntityType<?>>) supplier
            );
        }

        throw new IllegalArgumentException(
            "Received registration attempt for an unhandled registry. Registry: " + registry
        );
    }
}
