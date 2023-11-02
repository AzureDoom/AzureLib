package mod.azure.azurelib.common.internal.common.core.math.functions.utility;

import mod.azure.azurelib.common.internal.common.core.math.IValue;
import mod.azure.azurelib.common.internal.common.core.math.functions.Function;
import mod.azure.azurelib.common.internal.common.core.utils.Interpolations;

public class LerpRotate extends Function {
	public LerpRotate(IValue[] values, String name) throws Exception {
		super(values, name);
	}

	@Override
	public int getRequiredArguments() {
		return 3;
	}

	@Override
	public double get() {
		return Interpolations.lerpYaw(this.getArg(0), this.getArg(1), this.getArg(2));
	}
}
