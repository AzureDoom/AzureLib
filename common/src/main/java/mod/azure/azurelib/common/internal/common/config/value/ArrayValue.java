/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.common.internal.common.config.value;

public interface ArrayValue {

    boolean isFixedSize();

    default String elementToString(Object element) {
        return element.toString();
    }
}
