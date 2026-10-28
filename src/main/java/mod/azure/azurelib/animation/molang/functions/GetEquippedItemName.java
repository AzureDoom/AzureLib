package mod.azure.azurelib.animation.molang.functions;

import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.runtime.MolangObjectValue;
import mod.azure.azurelib.core.molang.runtime.MolangSlot;
import mod.azure.azurelib.util.AzEquipmentSlot;

/**
 * {@code query.get_equipped_item_name([hand])}: the name of the item in the hand ({@code 'main_hand'} or 0, the
 * default; {@code 'off_hand'} or 1), as a string. As in Bedrock the name is the item id without its namespace, so
 * {@code q.get_equipped_item_name == 'bow'}. An empty hand gives {@code ''}, and in Beta the offhand is always empty.
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
        AzMolangQueryContext context = currentContext();

        if (context == null) {
            out.setObject("");
            return;
        }

        int hand = handArg(0, MAIN_HAND);
        AzEquipmentSlot slot = hand == OFF_HAND
            ? AzEquipmentSlot.OFFHAND
            : hand == MAIN_HAND ? AzEquipmentSlot.MAINHAND : null;
        String id = itemId(itemIn(context, slot));

        out.setObject(id == null ? "" : id.substring(id.indexOf(':') + 1));
    }
}
