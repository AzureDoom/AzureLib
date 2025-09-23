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
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import mod.azure.azurelib.common.platform.services.CommonRegistry;

public class NeoForgeCommonRegistry implements CommonRegistry {

    public static DeferredRegister<BlockEntityType<?>> blockEntityTypeDeferredRegister;

    public static DeferredRegister<Block> blockDeferredRegister;

    public static DeferredRegister<EntityType<?>> entityTypeDeferredRegister;

    public static DeferredRegister<Item> itemDeferredRegister;

    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, String registryName, Supplier<? extends T> supplier) {
        if (registry == BuiltInRegistries.BLOCK) {
            return (Supplier<T>) blockDeferredRegister.register(registryName, (Supplier<Block>) supplier);
        } else if (registry == BuiltInRegistries.ITEM) {
            return (Supplier<T>) itemDeferredRegister.register(registryName, (Supplier<Item>) supplier);
        } else if (registry == BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            return (Supplier<T>) blockEntityTypeDeferredRegister.register(
                registryName,
                (Supplier<BlockEntityType<?>>) supplier
            );
        } else if (registry == BuiltInRegistries.ENTITY_TYPE) {
            return (Supplier<T>) entityTypeDeferredRegister.register(
                registryName,
                (Supplier<EntityType<?>>) supplier
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
