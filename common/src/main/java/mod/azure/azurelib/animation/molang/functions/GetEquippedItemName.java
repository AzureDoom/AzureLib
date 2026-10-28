package mod.azure.azurelib.animation.molang.functions;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangQueryContext;
import mod.azure.azurelib.core.molang.runtime.MolangObjectValue;
import mod.azure.azurelib.core.molang.runtime.MolangSlot;

/**
 * {@code query.get_equipped_item_name([hand])}: the name of the item in the hand ({@code 'main_hand'} or 0, the
 * default; {@code 'off_hand'} or 1), as a string. As in Bedrock the name is the item id without its namespace, so
 * {@code q.get_equipped_item_name == 'bow'}. An empty hand gives {@code ''}.
 */
public class GetEquippedItemName extends AzQueryFunction implements MolangObjectValue {

    public GetEquippedItemName(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double evaluate(AzMolangQueryContext context) {
        return 0;
    }

    @Override
    public void evaluate(MolangSlot out) {
        if (!(MolangQueryContext.current() instanceof AzMolangQueryContext context)) {
            out.setObject("");
            return;
        }

        int hand = handArg(0, MAIN_HAND);
        EquipmentSlot slot = hand == OFF_HAND
            ? EquipmentSlot.OFFHAND
            : hand == MAIN_HAND ? EquipmentSlot.MAINHAND : null;
        ItemStack stack = itemIn(context, slot);

        out.setObject(stack.isEmpty() ? "" : Registry.ITEM.getKey(stack.getItem()).getPath());
    }
}
