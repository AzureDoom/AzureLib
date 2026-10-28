package mod.azure.azurelib.core.molang.runtime;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangQueryContext;

/**
 * {@code entity->expression}: evaluates {@code expression} as if animating the entity on the left, so its queries (and,
 * where the context supports it, its {@code variable.*} values) read that entity: {@code t.pig->q.health}.
 * <p>
 * The left side must evaluate to an entity reference the current {@link MolangQueryContext} accepts (see
 * {@link MolangQueryContext#pushEntity}). Anything else, including a stale or removed entity, gives 0 without
 * evaluating the right side, as in Bedrock.
 * </p>
 */
public final class MolangArrow implements MolangObjectValue {

    private IValue entity;

    private IValue expression;

    private final MolangSlot entitySlot = new MolangSlot();

    private final MolangSlot result = new MolangSlot();

    public MolangArrow(IValue entity, IValue expression) {
        this.entity = entity;
        this.expression = expression;
    }

    @Override
    public void evaluate(MolangSlot out) {
        MolangSlot.evaluate(entity, entitySlot);
        Object target = entitySlot.object();
        entitySlot.clear();

        MolangQueryContext context = MolangQueryContext.current();

        if (target == null || !context.pushEntity(target)) {
            out.setNumber(0);
            return;
        }

        try {
            MolangSlot.evaluate(expression, out);
        } finally {
            context.popEntity();
        }
    }

    @Override
    public double get() {
        evaluate(result);
        double value = result.number();
        result.clear();
        return value;
    }

    @Override
    public IValue simplify() {
        this.entity = entity.simplify();
        this.expression = expression.simplify();
        return this;
    }

    @Override
    public String toString() {
        return entity + "->" + expression;
    }
}
