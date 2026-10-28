package mod.azure.azurelib.animation.molang.functions;

import net.minecraft.item.ItemStack;

import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.core.math.IValue;

/**
 * {@code query.equipped_item_any_tag(slot, tag, ...)}: 1 if the item in the equipment slot has any of the given item
 * tags ({@code q.equipped_item_any_tag('slot.weapon.mainhand', 'minecraft:swords')}). An empty slot gives 0.
 */
public class EquippedItemAnyTag extends AzQueryFunction {

    public EquippedItemAnyTag(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    protected double evaluate(AzMolangQueryContext context) {
        ItemStack stack = itemIn(context, slotArg(0, null));

        if (stack.isEmpty()) {
            return 0;
        }

        for (int i = 1; i < this.args.length; i++) {
            boolean tagged = hasItemTag(stack, stringArg(i));

            if (tagged) {
                return 1;
            }
        }

        return 0;
    }
}
