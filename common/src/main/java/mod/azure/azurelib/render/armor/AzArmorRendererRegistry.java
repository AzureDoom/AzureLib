package mod.azure.azurelib.render.armor;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registry for associating custom armor renderers with specific items and optionally their CustomModelData values.
 * Allows flexible registration of {@link AzArmorRenderer} instances to render armor items differently based on their
 * CustomModelData, falling back to vanilla rendering if no custom renderer is registered.
 */
public class AzArmorRendererRegistry {

    /**
     * Internal record representing a unique combination of an item and its CustomModelData value. Used as a key in the
     * renderer maps.
     */
    private record ArmorKey(
        Item item,
        int customModelData
    ) {}

    /** Map storing instantiated renderers for a quick lookup. */
    private static final Map<ArmorKey, AzArmorRenderer> ITEM_TO_RENDERER = new HashMap<>();

    /** Map storing renderer suppliers for lazy initialization. */
    private static final Map<ArmorKey, Supplier<AzArmorRenderer>> ITEM_TO_RENDERER_SUPPLIER = new HashMap<>();

    /**
     * Registers a renderer supplier for an item without a specific CustomModelData constraint. The renderer will apply
     * to the item regardless of its CustomModelData value.
     *
     * @param item                  The item to associate with the renderer.
     * @param armorRendererSupplier A supplier providing the {@link AzArmorRenderer} instance.
     */
    public static void register(Item item, Supplier<AzArmorRenderer> armorRendererSupplier) {
        register(item, -1, armorRendererSupplier); // -1 indicates no specific CustomModelData
    }

    /**
     * Registers a renderer supplier for an item with a specific CustomModelData value. The renderer will only apply
     * when the item's CustomModelData matches the specified value.
     *
     * @param item                  The item to associate with the renderer.
     * @param customModelData       The specific CustomModelData value to match, or -1 for any value.
     * @param armorRendererSupplier A supplier providing the {@link AzArmorRenderer} instance.
     */
    public static void register(Item item, int customModelData, Supplier<AzArmorRenderer> armorRendererSupplier) {
        ITEM_TO_RENDERER_SUPPLIER.put(new ArmorKey(item, customModelData), armorRendererSupplier);
    }

    /**
     * Registers a renderer supplier for multiple items without a specific CustomModelData constraint. The renderer will
     * apply to all specified items regardless of their CustomModelData values.
     *
     * @param armorRendererSupplier A supplier providing the {@link AzArmorRenderer} instance.
     * @param item                  The first item to associate with the renderer.
     * @param items                 Additional items to associate with the same renderer.
     */
    public static void register(Supplier<AzArmorRenderer> armorRendererSupplier, Item item, Item... items) {
        register(item, armorRendererSupplier);
        for (var otherItem : items) {
            register(otherItem, armorRendererSupplier);
        }
    }

    /**
     * Registers a renderer supplier for multiple items with a specific CustomModelData value. The renderer will only
     * apply when the items' CustomModelData matches the specified value.
     *
     * @param customModelData       The specific CustomModelData value to match, or -1 for any value.
     * @param armorRendererSupplier A supplier providing the {@link AzArmorRenderer} instance.
     * @param item                  The first item to associate with the renderer.
     * @param items                 Additional items to associate with the same renderer.
     */
    public static void register(
        int customModelData,
        Supplier<AzArmorRenderer> armorRendererSupplier,
        Item item,
        Item... items
    ) {
        register(item, customModelData, armorRendererSupplier);
        for (var otherItem : items) {
            register(otherItem, customModelData, armorRendererSupplier);
        }
    }

    /**
     * Retrieves the renderer for an item with a specific CustomModelData value, or null if none is registered. If no
     * renderer is found for the exact CustomModelData, it falls back to a renderer registered for the item without a
     * specific CustomModelData (-1).
     *
     * @param item            The item to look up.
     * @param customModelData The CustomModelData value to match.
     * @return The associated {@link AzArmorRenderer}, or null if none is found.
     */
    public static @Nullable AzArmorRenderer getOrNull(Item item, int customModelData) {
        ArmorKey specificKey = new ArmorKey(item, customModelData);
        return ITEM_TO_RENDERER.computeIfAbsent(specificKey, (key) -> {
            var rendererSupplier = ITEM_TO_RENDERER_SUPPLIER.get(specificKey);
            if (rendererSupplier != null) {
                return rendererSupplier.get(); // Instantiate renderer if supplier exists for specific key
            }
            // Fallback to generic renderer for the item (no specific CustomModelData)
            ArmorKey genericKey = new ArmorKey(item, -1);
            rendererSupplier = ITEM_TO_RENDERER_SUPPLIER.get(genericKey);
            return rendererSupplier != null ? rendererSupplier.get() : null;
        });
    }

    /**
     * Retrieves the renderer for an ItemStack, considering its CustomModelData value. Delegates to
     * {@link #getOrNull(Item, int)} after extracting the CustomModelData.
     *
     * @param stack The ItemStack to look up.
     * @return The associated {@link AzArmorRenderer}, or null if none is found.
     */
    public static @Nullable AzArmorRenderer getOrNull(ItemStack stack) {
        int customModelData = getCustomModelDataId(stack);
        return getOrNull(stack.getItem(), customModelData);
    }

    /**
     * Extracts the CustomModelData value from an ItemStack. Returns 0 if no CustomModelData is present.
     *
     * @param itemStack The ItemStack to inspect.
     * @return The CustomModelData value, or 0 if not set.
     */
    public static int getCustomModelDataId(ItemStack itemStack) {
        int customModelDataId = 0;
        if (itemStack.getComponents().get(DataComponents.CUSTOM_MODEL_DATA) != null) {
            customModelDataId = itemStack.getComponents().get(DataComponents.CUSTOM_MODEL_DATA).value();
        }
        return customModelDataId;
    }
}
