package mod.azure.azurelib.neoforge.platform;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import java.util.function.Supplier;

import mod.azure.azurelib.common.platform.services.CommonRegistry;
import mod.azure.azurelib.neoforge.NeoForgeAzureLibMod;

public class NeoForgeCommonRegistry implements CommonRegistry {

    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, String registryName, Supplier<? extends T> supplier) {
        if (registry == BuiltInRegistries.BLOCK) {
            return (Supplier<T>) NeoForgeAzureLibMod.blockDeferredRegister.register(
                registryName,
                (Supplier<Block>) supplier
            );
        } else if (registry == BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            return (Supplier<T>) NeoForgeAzureLibMod.blockEntityTypeDeferredRegister.register(
                registryName,
                (Supplier<BlockEntityType<?>>) supplier
            );
        }

        throw new IllegalArgumentException(
            "Received registration attempt for an unhandled registry. Registry: " + registry
        );
    }

    @Override
    public <E extends Mob> Supplier<SpawnEggItem> makeSpawnEggFor(
        Supplier<EntityType<E>> entityType,
        int primaryEggColour,
        int secondaryEggColour,
        Item.Properties itemProperties
    ) {
        return () -> new DeferredSpawnEggItem(entityType, primaryEggColour, secondaryEggColour, itemProperties);
    }

    @Override
    public CreativeModeTab.Builder newCreativeTabBuilder() {
        return CreativeModeTab.builder();
    }
}
