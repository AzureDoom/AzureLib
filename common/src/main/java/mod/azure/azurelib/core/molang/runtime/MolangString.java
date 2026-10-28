package mod.azure.azurelib.core.molang.runtime;

/**
 * A string literal, {@code 'minecraft:zombie'}. Case is preserved (unlike the rest of Molang, strings are
 * case-sensitive), and strings only support {@code ==} and {@code !=}. Used as a number, a string is 0.
 */
public final class MolangString implements MolangObjectValue {

    private final String value;

    public MolangString(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public double get() {
        return 0;
    }

    @Override
    public void evaluate(MolangSlot out) {
        out.setObject(value);
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }
}
