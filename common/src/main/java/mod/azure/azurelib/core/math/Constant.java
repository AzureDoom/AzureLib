/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.math;

/**
 * Constant class This class simply returns supplied in the constructor value
 */
public class Constant implements IValue {

    private double value;

    public Constant(double value) {
        this.value = value;
    }

    @Override
    public double get() {
        return this.value;
    }

    public void set(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
