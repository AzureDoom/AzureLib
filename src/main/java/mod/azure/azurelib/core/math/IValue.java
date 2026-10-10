/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.math;

/**
 * Math value interface This interface provides only one method which is used by all mathematical related classes. The
 * point of this interface is to provide generalized abstract method for computing/fetching some value from different
 * mathematical classes.
 */
public interface IValue {

    /**
     * Get computed or stored value
     */
    double get();

    /**
     * Returns an equivalent value with constant sub-expressions folded and no-op wrappers removed. Called once at parse
     * time, before the tree is shared; implementations may rewrite their children in place. Must never change what
     * {@link #get()} returns.
     */
    default IValue simplify() {
        return this;
    }
}
