/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.math;

/**
 * Group class Simply wraps given {@link IValue} into parenthesis in the {@link #toString()} method.
 */
public class Group implements IValue {

    private IValue value;

    public Group(IValue value) {
        this.value = value;
    }

    @Override
    public double get() {
        return this.value.get();
    }

    @Override
    public String toString() {
        return "(" + this.value.toString() + ")";
    }
}
