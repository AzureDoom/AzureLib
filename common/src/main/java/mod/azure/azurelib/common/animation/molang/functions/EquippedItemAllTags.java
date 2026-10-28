package mod.azure.azurelib.common.animation.molang.functions;

import mod.azure.azurelib.common.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.core.math.IValue;

/**
 * {@code query.equipped_item_all_tags(slot, tag, ...)}: 1 if the item in the equipment slot has every one of the given
 * item tags ({@code q.equipped_item_all_tags('slot.weapon.mainhand', 'minecraft:swords')}). An empty slot gives 0.
 */
public class EquippedItemAllTags extends AzQueryFunction {

    public EquippedItemAllTags(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    protected double evaluate(AzMolangQueryContext context) {
        var stack = itemIn(context, slotArg(0, null));

        if (stack.isEmpty()) {
            return 0;
        }

        for (int i = 1; i < this.args.length; i++) {
            var tagged = hasItemTag(stack, stringArg(i));

            if (!tagged) {
                return 0;
            }
        }

        return 1;
    }
}
