package mod.azure.azurelib.common.animation.molang.functions;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import mod.azure.azurelib.common.animation.molang.AzMolangQueryContext;
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

    private final Map<String, TagKey<Item>> tagCache = new HashMap<>();

    protected AzQueryFunction(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected final double evaluate(MolangQueryContext context) {
        return context instanceof AzMolangQueryContext az ? evaluate(az) : 0;
    }

    protected abstract double evaluate(AzMolangQueryContext context);

    /**
     * An equipment slot argument: a Bedrock slot name ({@code 'slot.weapon.mainhand'}, {@code 'slot.weapon.offhand'},
     * {@code 'slot.armor.head'}, {@code 'slot.armor.chest'}, {@code 'slot.armor.legs'}, {@code 'slot.armor.feet'},
     * {@code 'slot.armor.body'}), a hand name ({@code 'main_hand'} / {@code 'off_hand'}), or 0 / 1 for the hands.
     *
     * @return the slot, {@code fallback} if the argument is missing, or {@code null} for an unknown name
     */
    protected @Nullable EquipmentSlot slotArg(int index, @Nullable EquipmentSlot fallback) {
        if (index >= this.args.length) {
            return fallback;
        }

        argInto(index, scratch);
        var name = scratch.string();
        var number = scratch.number();
        scratch.clear();

        if (name == null) {
            return (int) number == OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
        }

        return switch (name.toLowerCase(Locale.ROOT)) {
            case "main_hand", "mainhand", "slot.weapon.mainhand" -> EquipmentSlot.MAINHAND;
            case "off_hand", "offhand", "slot.weapon.offhand" -> EquipmentSlot.OFFHAND;
            case "slot.armor.head", "head" -> EquipmentSlot.HEAD;
            case "slot.armor.chest", "chest" -> EquipmentSlot.CHEST;
            case "slot.armor.legs", "legs" -> EquipmentSlot.LEGS;
            case "slot.armor.feet", "feet" -> EquipmentSlot.FEET;
            case "slot.armor.body", "body" -> EquipmentSlot.BODY;
            default -> null;
        };
    }

    /**
     * @return the item in {@code slot} of the context's living entity, or {@link ItemStack#EMPTY}
     */
    protected static ItemStack itemIn(AzMolangQueryContext context, @Nullable EquipmentSlot slot) {
        LivingEntity living = context.livingEntity();
        return living == null || slot == null ? ItemStack.EMPTY : living.getItemBySlot(slot);
    }

    /**
     * Whether {@code stack}'s item id equals {@code name}. A name without a namespace means {@code minecraft:}.
     */
    protected static boolean isItemNamed(ItemStack stack, @Nullable String name) {
        if (stack.isEmpty() || name == null) {
            return false;
        }

        var id = ResourceLocation.tryParse(name);
        return id != null && id.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    /**
     * Whether {@code stack} is in the item tag {@code name} (with or without a leading {@code #}).
     */
    protected boolean hasItemTag(ItemStack stack, @Nullable String name) {
        if (stack.isEmpty() || name == null) {
            return false;
        }

        var tag = tagCache.computeIfAbsent(name, key -> {
            var id = ResourceLocation.tryParse(key.startsWith("#") ? key.substring(1) : key);
            return id == null ? null : TagKey.create(Registries.ITEM, id);
        });

        return tag != null && stack.is(tag);
    }
}
