package mod.azure.azurelib.core.molang.expressions;

/**
 * A variable that can be unset, for Molang's null-coalescing operator: {@code v.speed ?? 1} is {@code v.speed} if it
 * has been given a value, otherwise 1. Variables that don't implement this always count as set.
 */
public interface MolangDefinable {

    /**
     * @return whether the variable currently holds a value
     */
    boolean isDefined();
}
