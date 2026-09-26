/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.math.functions.utility;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;

public class HermiteBlend extends Function {

    public HermiteBlend(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    /**
     * Hermite basis 3t^2 - 2t^3. Smooth from 0 to 1 over t in [0, 1]; any input is valid, but values outside that range
     * extrapolate the curve.
     */
    @Override
    public double get() {
        double t = this.getArg(0);

        return t * t * (3 - 2 * t);
    }
}
