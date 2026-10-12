/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client.adapter;

import java.lang.reflect.Field;

import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.client.WidgetAdder;
import mod.azure.azurelib.config.value.ConfigValue;

/**
 * Places the GUI widgets for a single config value into its row.
 */
@FunctionalInterface
public interface DisplayAdapter {

    /**
     * @param holder    Config holder the value belongs to
     * @param value     The value to edit
     * @param field     Owning field, used to read GUI annotations ({@code @ColorValue}, {@code @NumberFormat}, ...)
     * @param container Row the widgets are added to, also receives validation results
     */
    void placeWidgets(ConfigHolder<?> holder, ConfigValue<?> value, Field field, WidgetAdder container);
}
