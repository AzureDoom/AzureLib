package mod.azure.azurelib.common.internal.common.core.math.functions.classic;

import mod.azure.azurelib.common.internal.common.core.math.functions.Function;
import mod.azure.azurelib.common.internal.common.core.math.IValue;

/**
 * Absolute value function
 */
public class ASin extends Function {
	public ASin(IValue[] values, String name) throws Exception {
		super(values, name);
	}

	@Override
	public int getRequiredArguments() {
		return 1;
	}

	@Override
	public double get() {
		return Math.asin(this.getArg(0));
	}
}
