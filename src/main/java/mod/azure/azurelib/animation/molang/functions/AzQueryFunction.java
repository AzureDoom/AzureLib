package mod.azure.azurelib.animation.molang.functions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangQueryContext;
import mod.azure.azurelib.core.molang.functions.query.ContextQueryFunction;
import mod.azure.azurelib.core.molang.runtime.MolangSlot;

/**
 * Base for AzureLib's query functions that need Minecraft types: string arguments naming slots, items and tags, and
 * entity results. They read the entity bound in {@link AzMolangQueryContext}, so they follow the arrow operator.
 */
public abstract class AzQueryFunction extends ContextQueryFunction {

    private final MolangSlot scratch = new MolangSlot();

    /**
     * Ore dictionary entries by tag name; an empty list for names that aren't registered. Looked up once per name and
     * call site, since resolving a name allocates.
     */
    private final Map<String, NonNullList<ItemStack>> oreCache = new HashMap<>();

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
     * hand name ({@code 'main_hand'} / {@code 'off_hand'}), or 0 / 1 for the hands. 1.12.2 has no body armor slot.
     *
     * @return the slot, {@code fallback} if the argument is missing, or {@code null} for an unknown name
     */
    protected EntityEquipmentSlot slotArg(int index, EntityEquipmentSlot fallback) {
        if (index >= this.args.length) {
            return fallback;
        }

        argInto(index, scratch);
        String name = scratch.string();
        double number = scratch.number();
        scratch.clear();

        if (name == null) {
            return (int) number == OFF_HAND ? EntityEquipmentSlot.OFFHAND : EntityEquipmentSlot.MAINHAND;
        }

        switch (name.toLowerCase(Locale.ROOT)) {
            case "main_hand":
            case "mainhand":
            case "slot.weapon.mainhand":
                return EntityEquipmentSlot.MAINHAND;
            case "off_hand":
            case "offhand":
            case "slot.weapon.offhand":
                return EntityEquipmentSlot.OFFHAND;
            case "slot.armor.head":
            case "head":
                return EntityEquipmentSlot.HEAD;
            case "slot.armor.chest":
            case "chest":
                return EntityEquipmentSlot.CHEST;
            case "slot.armor.legs":
            case "legs":
                return EntityEquipmentSlot.LEGS;
            case "slot.armor.feet":
            case "feet":
                return EntityEquipmentSlot.FEET;
            default:
                return null;
        }
    }

    /**
     * @return the item in {@code slot} of the context's living entity, or {@link ItemStack#EMPTY}
     */
    protected static ItemStack itemIn(AzMolangQueryContext context, EntityEquipmentSlot slot) {
        EntityLivingBase living = context.livingEntity();
        return living == null || slot == null ? ItemStack.EMPTY : living.getItemStackFromSlot(slot);
    }

    /**
     * @return the stack's item registry id, or {@code null} for an empty stack or an unregistered item
     */
    protected static ResourceLocation itemId(ItemStack stack) {
        return stack.isEmpty() ? null : stack.getItem().getRegistryName();
    }

    /**
     * Whether {@code stack}'s item id equals {@code name}. A name without a namespace means {@code minecraft:}.
     */
    protected static boolean isItemNamed(ItemStack stack, String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        ResourceLocation id = itemId(stack);
        return id != null && id.equals(new ResourceLocation(name));
    }

    /**
     * Whether {@code stack} is in the item tag {@code name}. 1.12.2 has no item tags, so this checks the Forge ore
     * dictionary instead: {@code 'ingotIron'}, {@code 'logWood'}. A leading {@code #} and a {@code forge:} or
     * {@code ore:} namespace are ignored. Ore dictionary names are case-sensitive.
     */
    protected boolean hasItemTag(ItemStack stack, String name) {
        if (stack.isEmpty() || name == null) {
            return false;
        }

        NonNullList<ItemStack> ores = oreCache.get(name);

        if (ores == null) {
            String oreName = oreName(name);
            ores = OreDictionary.doesOreNameExist(oreName)
                ? OreDictionary.getOres(oreName, false)
                : NonNullList.create();
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
