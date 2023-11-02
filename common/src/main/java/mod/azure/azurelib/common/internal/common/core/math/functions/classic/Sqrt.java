package mod.azure.azurelib.common.internal.common.core.math.functions.classic;

import mod.azure.azurelib.common.internal.common.core.math.IValue;
import mod.azure.azurelib.common.internal.common.core.math.functions.Function;

public class Sqrt extends Function {
	public Sqrt(IValue[] values, String name) throws Exception {
		super(values, name);
	}

	@Override
	public int getRequiredArguments() {
		return 1;
	}

	@Override
	public double get() {
		return Math.sqrt(this.getArg(0));
	}
}
