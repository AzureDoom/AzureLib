package mod.azure.azurelib.animation.molang.functions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.modificationstation.stationapi.api.tag.TagKey;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangQueryContext;
import mod.azure.azurelib.core.molang.functions.query.ContextQueryFunction;
import mod.azure.azurelib.core.molang.runtime.MolangSlot;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.util.AzEquipmentSlot;

/**
 * Base for AzureLib's query functions that need Minecraft types: string arguments naming slots, items and tags, and
 * entity results. They read the entity bound in {@link AzMolangQueryContext}, so they follow the arrow operator.
 * <p>
 * In Beta an empty slot is a {@code null} stack, so the helpers here return {@code null} for empty and callers check
 * with {@link #isEmpty(ItemStack)}. Item ids and tags come from StationAPI's item registry, through {@link Mc}.
 * </p>
 */
public abstract class AzQueryFunction extends ContextQueryFunction {

    private final MolangSlot scratch = new MolangSlot();

    /** Tag keys by the name written in the expression, so each name is parsed once per call site. */
    private final Map<String, TagKey<Item>> tagCache = new HashMap<>();

    protected AzQueryFunction(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected final double evaluate(MolangQueryContext context) {
        return context instanceof AzMolangQueryContext ? evaluate((AzMolangQueryContext) context) : 0;
    }

    protected abstract double evaluate(AzMolangQueryContext context);

    /**
     * @return the current query context, or {@code null} if it isn't AzureLib's
     */
    protected static AzMolangQueryContext currentContext() {
        MolangQueryContext context = MolangQueryContext.current();
        return context instanceof AzMolangQueryContext ? (AzMolangQueryContext) context : null;
    }

    /**
     * An equipment slot argument: a Bedrock slot name ({@code 'slot.weapon.mainhand'}, {@code 'slot.weapon.offhand'},
     * {@code 'slot.armor.head'}, {@code 'slot.armor.chest'}, {@code 'slot.armor.legs'}, {@code 'slot.armor.feet'}), a
     * hand name ({@code 'main_hand'} / {@code 'off_hand'}), or 0 / 1 for the hands. The offhand is accepted but always
     * empty in Beta.
     *
     * @return the slot, {@code fallback} if the argument is missing, or {@code null} for an unknown name
     */
    protected AzEquipmentSlot slotArg(int index, AzEquipmentSlot fallback) {
        if (index >= this.args.length) {
            return fallback;
        }

        argInto(index, scratch);
        String name = scratch.string();
        double number = scratch.number();
        scratch.clear();

        if (name == null) {
            return (int) number == OFF_HAND ? AzEquipmentSlot.OFFHAND : AzEquipmentSlot.MAINHAND;
        }

        switch (name.toLowerCase(Locale.ROOT)) {
            case "main_hand":
            case "mainhand":
            case "slot.weapon.mainhand":
                return AzEquipmentSlot.MAINHAND;
            case "off_hand":
            case "offhand":
            case "slot.weapon.offhand":
                return AzEquipmentSlot.OFFHAND;
            case "slot.armor.head":
            case "head":
                return AzEquipmentSlot.HEAD;
            case "slot.armor.chest":
            case "chest":
                return AzEquipmentSlot.CHEST;
            case "slot.armor.legs":
            case "legs":
                return AzEquipmentSlot.LEGS;
            case "slot.armor.feet":
            case "feet":
                return AzEquipmentSlot.FEET;
            default:
                return null;
        }
    }

    /**
     * @return whether {@code stack} is an empty slot
     */
    protected static boolean isEmpty(ItemStack stack) {
        return stack == null || Mc.item(stack) == null || Mc.count(stack) <= 0;
    }

    /**
     * @return the item in {@code slot} of the context's living entity, or {@code null} if the slot is empty
     */
    protected static ItemStack itemIn(AzMolangQueryContext context, AzEquipmentSlot slot) {
        LivingEntity living = context.livingEntity();

        if (living == null || slot == null) {
            return null;
        }

        ItemStack stack = slot.getStack(living);
        return isEmpty(stack) ? null : stack;
    }

    /**
     * @return the stack's item registry id with its namespace ({@code minecraft:bow}), or {@code null} for an empty
     *         stack or an unregistered item
     */
    protected static String itemId(ItemStack stack) {
        if (isEmpty(stack)) {
            return null;
        }

        String id = Mc.itemIdentifier(Mc.item(stack));
        return id == null ? null : normalizeItemId(id);
    }

    /**
     * Whether {@code stack}'s item id equals {@code name}. A name without a namespace means {@code minecraft:}.
     */
    protected static boolean isItemNamed(ItemStack stack, String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        String id = itemId(stack);
        return id != null && id.equals(normalizeItemId(name));
    }

    /**
     * Lower-cases {@code id} and adds the {@code minecraft:} namespace if it has none.
     */
    protected static String normalizeItemId(String id) {
        String lower = id.toLowerCase(Locale.ROOT);
        return lower.indexOf(':') < 0 ? "minecraft:" + lower : lower;
    }

    /**
     * Whether {@code stack} is in the StationAPI item tag {@code name} ({@code 'c:swords'}, with or without a leading
     * {@code #}; no namespace means {@code minecraft:}). A name that isn't a valid identifier matches nothing.
     */
    protected boolean hasItemTag(ItemStack stack, String name) {
        if (isEmpty(stack) || name == null || name.isEmpty()) {
            return false;
        }

        TagKey<Item> tag = tagCache.get(name);

        if (tag == null && !tagCache.containsKey(name)) {
            tag = parseTag(name);
            tagCache.put(name, tag);
        }

        return tag != null && Mc.isIn(stack, tag);
    }

    private static TagKey<Item> parseTag(String name) {
        try {
            return Mc.itemTag(normalizeItemId(name.startsWith("#") ? name.substring(1) : name));
        } catch (RuntimeException invalid) {
            // Characters an identifier can't hold; treat as a tag no item has.
            return null;
        }
    }
}
