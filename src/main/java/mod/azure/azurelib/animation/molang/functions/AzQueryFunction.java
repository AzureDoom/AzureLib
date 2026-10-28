package mod.azure.azurelib.animation.molang.functions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangQueryContext;
import mod.azure.azurelib.core.molang.functions.query.ContextQueryFunction;
import mod.azure.azurelib.core.molang.runtime.MolangSlot;
import mod.azure.azurelib.util.AzEquipmentSlot;

/**
 * Base for AzureLib's query functions that need Minecraft types: string arguments naming slots, items and tags, and
 * entity results. They read the entity bound in {@link AzMolangQueryContext}, so they follow the arrow operator.
 * <p>
 * In 1.7.10 an empty slot is a {@code null} stack, so the helpers here return {@code null} for empty and callers check
 * with {@link #isEmpty(ItemStack)}.
 * </p>
 */
public abstract class AzQueryFunction extends ContextQueryFunction {

    private final MolangSlot scratch = new MolangSlot();

    /**
     * Ore dictionary entries by tag name; an empty list for names that aren't registered. Looked up once per name and
     * call site.
     */
    private final Map<String, List<ItemStack>> oreCache = new HashMap<>();

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
     * empty in 1.7.10.
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
        return stack == null || stack.getItem() == null || stack.stackSize <= 0;
    }

    /**
     * @return the item in {@code slot} of the context's living entity, or {@code null} if the slot is empty
     */
    protected static ItemStack itemIn(AzMolangQueryContext context, AzEquipmentSlot slot) {
        EntityLivingBase living = context.livingEntity();

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

        Object name = Item.itemRegistry.getNameForObject(stack.getItem());
        return name == null ? null : normalizeItemId(name.toString());
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
     * Whether {@code stack} is in the item tag {@code name}. 1.7.10 has no item tags, so this checks the Forge ore
     * dictionary instead: {@code 'ingotIron'}, {@code 'logWood'}. A leading {@code #} and a {@code forge:} or
     * {@code ore:} namespace are ignored. Ore dictionary names are case-sensitive.
     */
    protected boolean hasItemTag(ItemStack stack, String name) {
        if (isEmpty(stack) || name == null) {
            return false;
        }

        List<ItemStack> ores = oreCache.get(name);

        if (ores == null) {
            String oreName = oreName(name);
            // Checked first because getOres(String) registers any name it is asked about.
            ores = OreDictionary.doesOreNameExist(oreName)
                ? OreDictionary.getOres(oreName)
                : Collections.<ItemStack>emptyList();
            oreCache.put(name, ores);
        }

        for (int i = 0; i < ores.size(); i++) {
            if (OreDictionary.itemMatches(ores.get(i), stack, false)) {
                return true;
            }
        }

        return false;
    }

    private static String oreName(String tag) {
        String name = tag.startsWith("#") ? tag.substring(1) : tag;

        if (name.startsWith("forge:")) {
            return name.substring("forge:".length());
        }

        if (name.startsWith("ore:")) {
            return name.substring("ore:".length());
        }

        return name;
    }
}
