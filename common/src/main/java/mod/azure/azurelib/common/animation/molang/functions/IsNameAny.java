package mod.azure.azurelib.common.animation.molang.functions;

import mod.azure.azurelib.common.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.core.math.IValue;

/**
 * {@code query.is_name_any(name, ...)}: 1 if the entity's name is any of the given strings. The name is the custom name
 * (name tag) if it has one, the player name for players, and otherwise the entity type's display name. Case-sensitive.
 */
public class IsNameAny extends AzQueryFunction {

    public IsNameAny(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    protected double evaluate(AzMolangQueryContext context) {
        var entity = context.entity();

        if (entity == null) {
            return 0;
        }

        var name = entity.getName().getString();

        for (int i = 0; i < this.args.length; i++) {
            if (name.equals(stringArg(i))) {
                return 1;
            }
        }

        return 0;
    }
}
