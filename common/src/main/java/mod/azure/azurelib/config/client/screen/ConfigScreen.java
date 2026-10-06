/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.client.adapter.DisplayAdapters;
import mod.azure.azurelib.config.client.widget.ConfigEntryWidget;
import mod.azure.azurelib.config.value.ConfigValue;

public class ConfigScreen extends AbstractConfigScreen {

    private final Map<String, ConfigValue<?>> valueMap;

    public ConfigScreen(
        ConfigHolder<?> configHolder,
        Component screenTitle,
        Map<String, ConfigValue<?>> valueMap,
        Screen previous
    ) {
        super(screenTitle, previous, configHolder);
        this.valueMap = valueMap;
    }

    @Override
    protected int getEntryCount() {
        return this.valueMap.size();
    }

    @Override
    protected void init() {
        final var viewportMin = HEADER_HEIGHT;
        final var viewportHeight = this.height - viewportMin - FOOTER_HEIGHT;
        this.pageSize = Math.max((viewportHeight - 20) / ROW_SPACING, 1);
        this.correctScrollingIndex(this.valueMap.size());
        List<ConfigValue<?>> values = new ArrayList<>(this.valueMap.values());
        var errorOffset = (viewportHeight - 20) - (this.pageSize * ROW_SPACING - 5);
        var offset = 0;
        for (var i = this.index; i < this.index + this.pageSize; i++) {
            var j = i - this.index;
            if (i >= values.size())
                break;
            var correct = errorOffset / (this.pageSize - j);
            errorOffset -= correct;
            offset += correct;
            var value = values.get(i);
            var widget = this.addRenderableWidget(
                new ConfigEntryWidget(
                    30,
                    viewportMin + 10 + j * ROW_SPACING + offset,
                    this.width - 60,
                    20,
                    value,
                    this.getConfigId()
                )
            );
            widget.setDescriptionRenderer(this);
            var field = value.getSerializationContext().getOwner();
            var adapter = DisplayAdapters.forValue(value);
            if (adapter == null) {
                AzureLib.LOGGER.error(
                    "Missing display adapter for {} ({}), it will not be displayed in GUI",
                    value.getId(),
                    value.getClass().getSimpleName()
                );
                continue;
            }
            try {
                adapter.placeWidgets(this.holder, value, field, widget);
            } catch (ClassCastException e) {
                AzureLib.LOGGER.error("Unable to create config field for {}", value.getId(), e);
            }
        }
        this.addFooter();
    }
}
