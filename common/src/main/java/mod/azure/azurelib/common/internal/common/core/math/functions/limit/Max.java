package mod.azure.azurelib.common.internal.common.core.math.functions.limit;

import mod.azure.azurelib.common.internal.common.core.math.functions.Function;
import mod.azure.azurelib.common.internal.common.core.math.IValue;

public class Max extends Function {
	public Max(IValue[] values, String name) throws Exception {
		super(values, name);
	}

	@Override
	public int getRequiredArguments() {
		return 2;
	}

	@Override
	public double get() {
		return Math.max(this.getArg(0), this.getArg(1));
	}
}
