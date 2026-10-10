/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.math.functions.utility;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;
import mod.azure.azurelib.core.utils.Interpolations;

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
