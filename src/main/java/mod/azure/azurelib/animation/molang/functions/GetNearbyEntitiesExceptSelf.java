package mod.azure.azurelib.animation.molang.functions;

import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangArray;
import mod.azure.azurelib.core.molang.MolangQueryContext;
import mod.azure.azurelib.core.molang.runtime.MolangObjectValue;
import mod.azure.azurelib.core.molang.runtime.MolangSlot;

/**
 * {@code query.get_nearby_entities_except_self(radius[, 'entity_id'])}: an array of the living entities within
 * {@code radius} blocks, nearest first, optionally only those of one type ({@code 'minecraft:pig'}); the entity itself
 * is never included. Meant for {@code for_each} and {@code query.count}. The radius is capped at
 * {@value AzMolangQueryContext#MAX_NEARBY_RADIUS}.
 * <p>
 * This scans the level each time it is evaluated. Store the result in a {@code temp.*} rather than calling it several
 * times in one expression.
 * </p>
 */
public class GetNearbyEntitiesExceptSelf extends AzQueryFunction implements MolangObjectValue {

    public GetNearbyEntitiesExceptSelf(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    protected double evaluate(AzMolangQueryContext context) {
        return 0;
    }

    @Override
    public void evaluate(MolangSlot out) {
        if (!(MolangQueryContext.current() instanceof AzMolangQueryContext)) {
            out.setObject(MolangArray.EMPTY);
            return;
        }

        out.setObject(
            ((AzMolangQueryContext) MolangQueryContext.current()).nearbyEntities(getArg(0), stringArg(1), false)
        );
    }
}
