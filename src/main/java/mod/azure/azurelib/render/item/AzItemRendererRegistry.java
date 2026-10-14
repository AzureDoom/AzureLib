package mod.azure.azurelib.render.item;

import net.minecraft.item.Item;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.util.ResourceLocation;

/**
 * The AzItemRendererRegistry class manages the association between items and their renderers in the context of the
 * AzureLib framework. It provides functionality to register and retrieve item renderers dynamically, ensuring that
 * appropriate renderers can be applied to specific item types or instances.
 */
public class AzItemRendererRegistry {

    private static final Map<Item, AzItemRenderer> ITEM_TO_RENDERER = new ConcurrentHashMap<>();

    private static final Map<Item, Supplier<AzItemRenderer>> ITEM_TO_RENDERER_SUPPLIER = new ConcurrentHashMap<>();

    private static final Map<Item, AzItemRendererAdapter> ADAPTERS = new ConcurrentHashMap<>();

    /**
     * Registers an AzureLib renderer for the item. On 1.7.10 this also registers AzureLib's {@link IItemRenderer}
     * adapter with Forge, so call it on the client during init.
     */
    public static void register(Item item, Supplier<AzItemRenderer> itemRendererSupplier) {
        ITEM_TO_RENDERER_SUPPLIER.put(item, itemRendererSupplier);
        AzIdentityRegistry.register(item);
        ADAPTERS.put(item, new AzItemRendererAdapter(item));
    }

    /**
     * Registers the item, and also tells AzureLib where its {@code models/item/<name>.json} (for {@code display}
     * transforms) lives. Beta items have no registry name to derive it from.
     */
    public static void register(
        Item item,
        ResourceLocation itemModelJson,
        Supplier<AzItemRenderer> itemRendererSupplier
    ) {
        AzItemDisplayTransforms.setModelLocation(item, itemModelJson);
        register(item, itemRendererSupplier);
    }

    /** The adapter AzureLib's item mixins draw through, or null if the item has no AzureLib renderer. */
    public static @Nullable AzItemRendererAdapter adapterOrNull(@Nullable Item item) {
        return item == null ? null : ADAPTERS.get(item);
    }

    public static void register(Supplier<AzItemRenderer> itemRendererSupplier, Item item, Item... items) {
        register(item, itemRendererSupplier);

        for (Item otherItem : items) {
            register(otherItem, itemRendererSupplier);
        }
    }

    public static @Nullable AzItemRenderer getOrNull(Item item) {
        return ITEM_TO_RENDERER.computeIfAbsent(item, ($) -> {
            Supplier<AzItemRenderer> rendererSupplier = ITEM_TO_RENDERER_SUPPLIER.get(item);
            return rendererSupplier == null ? null : rendererSupplier.get();
        });
    }
}
