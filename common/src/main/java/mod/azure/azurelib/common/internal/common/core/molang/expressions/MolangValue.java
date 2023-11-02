package mod.azure.azurelib.common.internal.common.core.molang.expressions;

import mod.azure.azurelib.common.internal.common.core.math.Constant;
import mod.azure.azurelib.common.internal.common.core.math.IValue;
import mod.azure.azurelib.common.internal.common.core.molang.MolangParser;

/**
 * Molang extension for the {@link IValue} system.
 * Used to handle values and expressions specific to Molang deserialization
 */
public class MolangValue implements IValue {
	private final IValue value;
	private final boolean returns;

	public MolangValue(IValue value) {
		this(value, false);
	}

	public MolangValue(IValue value, boolean isReturn) {
		this.value = value;
		this.returns = isReturn;
	}

	@Override
	public double get() {
		return this.value.get();
	}

	public IValue getValueHolder() {
		return this.value;
	}

	public boolean isReturnValue() {
		return this.returns;
	}

	public boolean isConstant() {
		return getClass() == MolangValue.class && value instanceof Constant;
	}

	@Override
	public String toString() {
		return (this.returns ? MolangParser.RETURN : "") + this.value.toString();
	}
}
