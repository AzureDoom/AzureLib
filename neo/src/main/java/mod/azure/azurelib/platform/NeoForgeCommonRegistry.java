package mod.azure.azurelib.platform;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

import mod.azure.azurelib.NeoForgeAzureLibMod;
import mod.azure.azurelib.platform.services.CommonRegistry;

/**
 * The NeoForgeCommonRegistry class provides a concrete implementation of the CommonRegistry interface for use with
 * NeoForge. It facilitates centralized registration of blocks, items, entities, block entities, and creative mode tabs.
 * This implementation leverages the ForgeMod's deferred registers to manage the creation and registration of these
 * objects.
 */
public class NeoForgeCommonRegistry implements CommonRegistry {

    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, String registryName, Supplier<? extends T> supplier) {
        if (registry == BuiltInRegistries.BLOCK) {
            return (Supplier<T>) NeoForgeAzureLibMod.BLOCKS.register(registryName, (Supplier<Block>) supplier);
        } else if (registry == BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            return (Supplier<T>) NeoForgeAzureLibMod.TILE_TYPES.register(
                registryName,
                (Supplier<BlockEntityType<?>>) supplier
            );
        }

        throw new IllegalArgumentException(
            "Received registration attempt for an unhandled registry. Registry: " + registry
        );
    }
}
