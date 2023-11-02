package mod.azure.azurelib.common.internal.common.core.math.functions.classic;

import mod.azure.azurelib.common.internal.common.core.math.functions.Function;
import mod.azure.azurelib.common.internal.common.core.math.IValue;

public class Pi extends Function {
	public Pi(IValue[] values, String name) throws Exception {
		super(values, name);
	}

	@Override
	public double get() {
		return Math.PI;
	}
}
