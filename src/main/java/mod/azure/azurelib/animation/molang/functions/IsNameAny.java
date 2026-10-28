package mod.azure.azurelib.animation.molang.functions;

import net.minecraft.entity.Entity;

import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.platform.mc.Mc;

/**
 * {@code query.is_name_any(name, ...)}: 1 if the entity's name is any of the given strings. Beta has no name tags, so
 * the name is the player name for players and the entity's registry name ({@code Pig}) otherwise. Case-sensitive.
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
        Entity entity = context.entity();

        if (entity == null) {
            return 0;
        }

        String name = Mc.displayName(entity);

        if (name == null) {
            return 0;
        }

        for (int i = 0; i < this.args.length; i++) {
            if (name.equals(stringArg(i))) {
                return 1;
            }
        }

        return 0;
    }
}
