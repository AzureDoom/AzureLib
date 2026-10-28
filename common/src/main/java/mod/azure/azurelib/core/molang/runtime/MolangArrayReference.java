package mod.azure.azurelib.core.molang.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import mod.azure.azurelib.core.molang.MolangArray;

/**
 * A read-only {@code array.*} name. Bedrock defines these in render controllers; AzureLib has none, so code supplies
 * them through {@link mod.azure.azurelib.core.molang.MolangParser#registerArray}. The supplier is called each time the
 * array is read, so it can return a different array per animatable. An unregistered name reads as an empty array.
 * <p>
 * For per-animatable arrays without a global supplier, store a {@link MolangArray} in a {@code variable.*} instead
 * ({@code context.molangScope().setObject("v.skins", array)}).
 * </p>
 */
public final class MolangArrayReference implements MolangObjectValue {

    private static final Map<String, Supplier<MolangArray>> ARRAYS = new ConcurrentHashMap<>();

    private static final Map<String, MolangArrayReference> INTERNED = new ConcurrentHashMap<>();

    private final String name;

    private MolangArrayReference(String name) {
        this.name = name;
    }

    /**
     * @param normalizedName a full lower-case name such as {@code array.skins}
     */
    public static MolangArrayReference of(String normalizedName) {
        return INTERNED.computeIfAbsent(normalizedName, MolangArrayReference::new);
    }

    public static void register(String normalizedName, Supplier<MolangArray> supplier) {
        ARRAYS.put(normalizedName, supplier);
    }

    @Override
    public void evaluate(MolangSlot out) {
        var supplier = ARRAYS.get(name);
        var array = supplier == null ? null : supplier.get();
        out.setObject(array == null ? MolangArray.EMPTY : array);
    }

    @Override
    public double get() {
        return 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
