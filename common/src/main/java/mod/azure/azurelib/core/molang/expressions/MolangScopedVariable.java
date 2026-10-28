package mod.azure.azurelib.core.molang.expressions;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mod.azure.azurelib.core.math.Variable;
import mod.azure.azurelib.core.molang.LazyVariable;
import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.MolangScope;
import mod.azure.azurelib.core.molang.runtime.MolangSlot;

/**
 * A Molang {@code variable.*} reference whose value lives in the {@link MolangScope} that is current when it is
 * evaluated, i.e. on the animatable being animated, instead of in one value shared by every entity. One instance per
 * name is shared by every parsed expression.
 * <p>
 * If the current scope has never assigned it, reads fall back to a global variable of the same name that code has
 * registered or driven on {@link MolangParser} (so {@code MolangParser.INSTANCE.setValue("variable.x", ...)} keeps
 * working), and otherwise return 0. Assignments always write to the current scope. Like temp variables it can hold
 * strings, arrays and entity references; see {@link MolangScope#setObject(int, Object)} for how entities are kept.
 */
public final class MolangScopedVariable extends Variable implements MolangDefinable, MolangObjectVariable {

    private static final Map<String, MolangScopedVariable> INTERNED = new ConcurrentHashMap<>();

    private final int slot;

    private LazyVariable fallback;

    private int fallbackGeneration = -1;

    private MolangScopedVariable(String normalizedName) {
        super(normalizedName, 0);
        this.slot = MolangParser.scopeSlot(normalizedName);
    }

    /**
     * @param normalizedName a full name such as {@code variable.foo}
     */
    public static MolangScopedVariable of(String normalizedName) {
        return INTERNED.computeIfAbsent(normalizedName, MolangScopedVariable::new);
    }

    @Override
    public double get() {
        var scope = MolangScope.current();

        if (scope.isSet(slot)) {
            return scope.get(slot);
        }

        var global = fallback();
        return global == null ? 0 : global.get();
    }

    @Override
    public void evaluate(MolangSlot out) {
        var scope = MolangScope.current();

        if (scope.isSet(slot)) {
            var object = scope.getObject(slot);

            if (object != null) {
                out.setObject(object);
            } else {
                out.setNumber(scope.get(slot));
            }

            return;
        }

        var global = fallback();
        out.setNumber(global == null ? 0 : global.get());
    }

    @Override
    public void set(double value) {
        MolangScope.current().set(slot, value);
    }

    @Override
    public void setObject(@Nullable Object value) {
        MolangScope.current().setObject(slot, value);
    }

    /**
     * Set in the current scope, or driven globally by code. {@code v.x ?? 1} uses this. An entity reference whose
     * entity has since been garbage collected counts as unset, like Bedrock's stale actor references.
     */
    @Override
    public boolean isDefined() {
        return MolangScope.current().isSet(slot) || fallback() != null;
    }

    private LazyVariable fallback() {
        var generation = MolangParser.drivenGeneration();

        if (generation != fallbackGeneration) {
            this.fallback = MolangParser.drivenVariableOrNull(getName());
            this.fallbackGeneration = generation;
        }

        return fallback;
    }
}
