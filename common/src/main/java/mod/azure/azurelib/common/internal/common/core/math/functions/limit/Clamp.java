package mod.azure.azurelib.common.internal.common.core.math.functions.limit;

import mod.azure.azurelib.common.internal.common.core.math.functions.Function;
import mod.azure.azurelib.common.internal.common.core.math.IValue;
import mod.azure.azurelib.common.internal.common.core.utils.MathUtils;

public class Clamp extends Function {
	public Clamp(IValue[] values, String name) throws Exception {
		super(values, name);
	}

	@Override
	public int getRequiredArguments() {
		return 3;
	}

	@Override
	public double get() {
		return MathUtils.clamp(this.getArg(0), this.getArg(1), this.getArg(2));
	}
}
