package mod.azure.azurelib.animation.molang.functions;

import net.minecraft.item.ItemStack;

import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.core.math.IValue;

/**
 * {@code query.is_item_name_any(slot, index, name, ...)}: 1 if the item in the equipment slot is any of the named items
 * ({@code q.is_item_name_any('slot.weapon.mainhand', 0, 'minecraft:bow', 'minecraft:crossbow')}). The index is accepted
 * for Bedrock compatibility; equipment slots only have index 0.
 */
public class IsItemNameAny extends AzQueryFunction {

    public IsItemNameAny(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 3;
    }

    @Override
    protected double evaluate(AzMolangQueryContext context) {
        ItemStack stack = itemIn(context, slotArg(0, null));

        if (isEmpty(stack)) {
            return 0;
        }

        for (int i = 2; i < this.args.length; i++) {
            if (isItemNamed(stack, stringArg(i))) {
                return 1;
            }
        }

        return 0;
    }
}
