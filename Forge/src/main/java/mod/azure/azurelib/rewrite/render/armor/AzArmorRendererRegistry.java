package mod.azure.azurelib.rewrite.render.armor;

import net.minecraft.world.item.Item;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AzArmorRendererRegistry {

    private static final Map<Item, AzArmorRenderer> ITEM_TO_RENDERER = new HashMap<>();

    private static final Map<Item, Supplier<AzArmorRenderer>> ITEM_TO_RENDERER_SUPPLIER = new HashMap<>();

    public static void register(Item item, Supplier<AzArmorRenderer> armorRendererSupplier) {
        ITEM_TO_RENDERER_SUPPLIER.put(item, armorRendererSupplier);
    }

    public static void register(Supplier<AzArmorRenderer> armorRendererSupplier, Item item, Item... items) {
        register(item, armorRendererSupplier);

        for (var otherItem : items) {
            register(otherItem, armorRendererSupplier);
        }
    }

    public static AzArmorRenderer getOrNull(Item item) {
        return ITEM_TO_RENDERER.computeIfAbsent(item, ($) -> {
            var rendererSupplier = ITEM_TO_RENDERER_SUPPLIER.get(item);
            return rendererSupplier == null ? null : rendererSupplier.get();
        });
    }
}
