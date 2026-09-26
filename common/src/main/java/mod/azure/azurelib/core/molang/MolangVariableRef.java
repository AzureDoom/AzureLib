package mod.azure.azurelib.core.molang;

import java.util.function.DoubleSupplier;

/**
 * A cached handle to a named Molang variable, for code that binds the same query every frame. Resolving a variable by
 * name costs a hash-map lookup; this resolves it once and reuses the {@link LazyVariable}.
 * <p>
 * {@link MolangParser#register} replaces the variable object for a name, so the handle re-resolves whenever a
 * registration has happened since it last resolved. Intended for the render thread, like the rest of query binding.
 */
public final class MolangVariableRef {

    private final String name;

    private LazyVariable variable;

    private int generation = -1;

    public MolangVariableRef(String name) {
        this.name = name;
    }

    public LazyVariable variable() {
        int current = MolangParser.registrationGeneration();

        if (this.generation != current) {
            this.variable = MolangParser.INSTANCE.getVariable(this.name);
            this.generation = current;
        }

        return this.variable;
    }

    /** Same as {@link MolangParser#setValue(String, DoubleSupplier)} without the lookup. */
    public void set(DoubleSupplier valueSupplier) {
        variable().set(valueSupplier);
    }

    /** Same as {@link MolangParser#setMemoizedValue(String, DoubleSupplier)} without the lookup. */
    public void setMemoized(DoubleSupplier valueSupplier) {
        variable().setMemoized(valueSupplier);
    }

    public String name() {
        return this.name;
    }
}
