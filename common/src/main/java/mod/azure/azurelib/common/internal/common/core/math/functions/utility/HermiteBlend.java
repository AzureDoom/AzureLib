package mod.azure.azurelib.common.internal.common.core.math.functions.utility;

import mod.azure.azurelib.common.internal.common.core.math.IValue;
import mod.azure.azurelib.common.internal.common.core.math.functions.Function;

public class HermiteBlend extends Function {
	public java.util.Random random;

	public HermiteBlend(IValue[] values, String name) throws Exception {
		super(values, name);

		this.random = new java.util.Random();
	}

	@Override
	public int getRequiredArguments() {
		return 1;
	}

	@Override
	public double get() {
		double min = Math.ceil(this.getArg(0));
		return Math.floor(3 * Math.pow(min, 2) - 2 * Math.pow(min, 3));
	}
}
