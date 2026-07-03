/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.util;

/**
 * Helper class for various AzureLib-specific functions.
 */
public record AzureLibUtil() {

    @SuppressWarnings("unchecked")
    public static <T> T self(Object object) {
        return (T) object;
    }
}
