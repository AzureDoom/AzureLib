/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.math;

/**
 * Negate operator class This class is responsible for negating given value
 */
public class Negate implements IValue {

    public IValue value;

    public Negate(IValue value) {
        this.value = value;
    }

    @Override
    public double get() {
        return this.value.get() == 0 ? 1 : 0;
    }

    @Override
    public String toString() {
        return "!" + this.value.toString();
    }
}
