package mod.azure.azurelib.common.internal.common.core.math.functions.classic;

import mod.azure.azurelib.common.internal.common.core.math.functions.Function;
import mod.azure.azurelib.common.internal.common.core.math.IValue;

/**
 * Absolute value function
 */
public class ATan2 extends Function {
	public ATan2(IValue[] values, String name) throws Exception {
		super(values, name);
	}

	@Override
	public int getRequiredArguments() {
		return 2;
	}

	@Override
	public double get() {
		return Math.atan2(this.getArg(0), this.getArg(1));
	}
}
