package mod.azure.azurelib.common.internal.common.core.molang.functions;

import mod.azure.azurelib.common.internal.common.core.math.IValue;
import mod.azure.azurelib.common.internal.common.core.math.functions.Function;
import mod.azure.azurelib.common.internal.common.core.math.functions.classic.Sin;

/**
 * Replacement function for {@link Sin}, operating in degrees rather than radians
 */
public class SinDegrees extends Function {
	public SinDegrees(IValue[] values, String name) throws Exception {
		super(values, name);
	}

	@Override
	public int getRequiredArguments() {
		return 1;
	}

	@Override
	public double get() {
		return Math.sin(getArg(0) / 180 * Math.PI);
	}
}
