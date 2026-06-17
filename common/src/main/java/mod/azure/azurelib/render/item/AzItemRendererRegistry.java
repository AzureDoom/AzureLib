package mod.azure.azurelib.render.item;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The AzItemRendererRegistry class manages the association between items and their renderers in the context of the
 * AzureLib framework. It provides functionality to register and retrieve item renderers dynamically, ensuring that
 * appropriate renderers can be applied to specific item types or instances.
 */
public class AzItemRendererRegistry {

    private static final Map<Item, AzItemRenderer> ITEM_TO_RENDERER = new HashMap<>();

    private static final Map<Item, Supplier<AzItemRenderer>> ITEM_TO_RENDERER_SUPPLIER = new HashMap<>();

    public static void register(Item item, Supplier<AzItemRenderer> itemRendererSupplier) {
        ITEM_TO_RENDERER_SUPPLIER.put(item, itemRendererSupplier);
    }

    public static void register(Supplier<AzItemRenderer> itemRendererSupplier, Item item, Item... items) {
        register(item, itemRendererSupplier);

        for (var otherItem : items) {
            register(otherItem, itemRendererSupplier);
        }
    }

    public static @Nullable AzItemRenderer getOrNull(Item item) {
        return ITEM_TO_RENDERER.computeIfAbsent(item, ($) -> {
            var rendererSupplier = ITEM_TO_RENDERER_SUPPLIER.get(item);
            return rendererSupplier == null ? null : rendererSupplier.get();
        });
    }
}
