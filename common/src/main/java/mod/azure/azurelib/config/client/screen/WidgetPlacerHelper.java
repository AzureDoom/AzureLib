/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client.screen;

public final class WidgetPlacerHelper {

    private WidgetPlacerHelper() {
        throw new UnsupportedOperationException();
    }

    public static int getLeft(int x, int totalWidth) {
        return x + totalWidth - getWidth(totalWidth) - 42;
    }

    public static int getWidth(int totalWidth) {
        return (int) (totalWidth / 3.5);
    }
}
