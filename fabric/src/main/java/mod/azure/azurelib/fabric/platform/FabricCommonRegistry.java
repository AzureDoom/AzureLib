package mod.azure.azurelib.fabric.platform;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.function.Supplier;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.platform.services.CommonRegistry;

public class FabricCommonRegistry implements CommonRegistry {

    private static <T, R extends Registry<? super T>> Supplier<T> registerSupplier(
        R registry,
        String modID,
        String id,
        Supplier<T> object
    ) {
        final T registeredObject = Registry.register(
            (Registry<T>) registry,
            ResourceLocation.fromNamespaceAndPath(modID, id),
            object.get()
        );

        return () -> registeredObject;
    }

    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, String registryName, Supplier<? extends T> supplier) {
        return (Supplier<T>) registerSupplier(registry, AzureLib.MOD_ID, registryName, supplier);
    }

    @Override
    public <E extends Mob> Supplier<SpawnEggItem> makeSpawnEggFor(
        Supplier<EntityType<E>> entityType,
        int primaryEggColour,
        int secondaryEggColour,
        Item.Properties itemProperties
    ) {
        return () -> new SpawnEggItem(entityType.get(), primaryEggColour, secondaryEggColour, itemProperties);
    }

    @Override
    public CreativeModeTab.Builder newCreativeTabBuilder() {
        return FabricItemGroup.builder();
    }
}
