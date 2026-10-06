/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collections;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.adapter.TypeAdapter;
import mod.azure.azurelib.config.adapter.TypeAdapters;
import mod.azure.azurelib.config.client.adapter.DisplayAdapters;
import mod.azure.azurelib.config.client.widget.ConfigEntryWidget;
import mod.azure.azurelib.config.value.ArrayValue;
import mod.azure.azurelib.config.value.ConfigValue;

/**
 * Edits any AzureLib array config value ({@code boolean[]}, {@code int[]}, {@code long[]}, {@code float[]},
 * {@code double[]}, {@code String[]}, {@code Enum[]}). Every element is exposed as a temporary single-value
 * {@link ConfigValue} created through the regular {@link TypeAdapter}, so element widgets reuse the normal display
 * adapters and pick up {@code @Range}, {@code @DecimalRange}, {@code @StringPattern} etc. from the array field.
 */
@SuppressWarnings("unused")
public class ArrayConfigScreen extends AbstractConfigScreen {

    public static final Component ADD_ELEMENT = Component.translatable("text.azurelib.value.add_element");

    public static final Component REMOVE_ELEMENT = Component.literal("-");

    private final ConfigValue<?> array;

    private final boolean fixedSize;

    private final Class<?> componentType;

    public ArrayConfigScreen(ConfigHolder<?> holder, Component title, ConfigValue<?> array, Screen previous) {
        super(title, previous, holder);
        if (!(array instanceof ArrayValue arrayValue)) {
            throw new IllegalArgumentException("Config value " + array.getId() + " is not an array value");
        }
        this.array = array;
        this.fixedSize = arrayValue.isFixedSize();
        this.componentType = array.get().getClass().getComponentType();
    }

    private int getSize() {
        return Array.getLength(this.array.get());
    }

    @Override
    protected int getEntryCount() {
        return this.fixedSize ? this.getSize() : this.getSize() + 1;
    }

    @Override
    protected void init() {
        final var viewportMin = HEADER_HEIGHT;
        final var viewportHeight = this.height - viewportMin - FOOTER_HEIGHT;
        this.pageSize = Math.max((viewportHeight - 20) / ROW_SPACING, 1);
        this.correctScrollingIndex(this.getEntryCount());
        var errorOffset = (viewportHeight - 20) - (this.pageSize * ROW_SPACING - 5);
        var offset = 0;

        var owner = this.array.getSerializationContext().getOwner();
        for (var i = this.index; i < this.index + this.pageSize; i++) {
            var j = i - this.index;
            if (i >= this.getSize()) {
                if (!this.fixedSize) {
                    this.addRenderableWidget(
                        Button.builder(ADD_ELEMENT, btn -> {
                            this.insertElement();
                            this.init(this.width, this.height);
                        }).pos(30, viewportMin + 10 + j * ROW_SPACING + offset).size(this.width - 60, 20).build()
                    );
                }
                break;
            }
            var correct = errorOffset / (this.pageSize - j);
            errorOffset -= correct;
            offset += correct;

            var element = this.createElementValue(i, owner);
            if (element == null)
                continue;
            var widget = this.addRenderableWidget(
                new ConfigEntryWidget(
                    30,
                    viewportMin + 10 + j * ROW_SPACING + offset,
                    this.width - 60,
                    20,
                    Component.literal("[" + i + "]"),
                    element,
                    this.getConfigId()
                )
            );
            widget.setDescriptionRenderer(this);
            var adapter = DisplayAdapters.forValue(element);
            if (adapter == null) {
                AzureLib.LOGGER.error(
                    "Missing display adapter for {} array elements, they will not be displayed in GUI",
                    this.componentType.getSimpleName()
                );
                continue;
            }
            try {
                adapter.placeWidgets(this.holder, element, owner, widget);
            } catch (ClassCastException e) {
                AzureLib.LOGGER.error("Unable to create config field for {}[{}]", this.array.getId(), i, e);
            }
            if (!this.fixedSize) {
                final var elementIndex = i;
                this.addRenderableWidget(
                    Button.builder(REMOVE_ELEMENT, btn -> {
                        this.removeElement(elementIndex);
                        this.init(this.width, this.height);
                    }).pos(this.width - 29, widget.getY()).size(20, 20).build()
                );
            }
        }
        this.addFooter();
    }

    private ConfigValue<?> createElementValue(int index, Field owner) {
        var adapter = TypeAdapters.forType(this.componentType);
        if (adapter == null) {
            AzureLib.LOGGER.error("Missing type adapter for array component type {}", this.componentType);
            return null;
        }
        var element = Array.get(this.array.get(), index);
        try {
            var value = adapter.serialize(
                this.array.getId(),
                new String[0],
                element,
                (type, instance) -> Collections.emptyMap(),
                new ElementContext(adapter, owner, index)
            );
            value.processFieldData(owner);
            return value;
        } catch (IllegalAccessException e) {
            AzureLib.LOGGER.error("Failed to create element {} of array {}", index, this.array.getId(), e);
            return null;
        }
    }

    private Object copyArray(int newLength, int copyLength) {
        var src = this.array.get();
        var dest = Array.newInstance(this.componentType, newLength);
        System.arraycopy(src, 0, dest, 0, copyLength);
        return dest;
    }

    private void insertElement() {
        var size = this.getSize();
        var expanded = this.copyArray(size + 1, size);
        if (this.componentType == String.class) {
            Array.set(expanded, size, "");
        } else if (this.componentType.isEnum()) {
            Array.set(expanded, size, this.componentType.getEnumConstants()[0]);
        }
        this.setArray(expanded);
    }

    private void removeElement(int index) {
        int size = this.getSize();
        if (index < 0 || index >= size)
            return;
        var src = this.array.get();
        var trimmed = Array.newInstance(this.componentType, size - 1);
        System.arraycopy(src, 0, trimmed, 0, index);
        System.arraycopy(src, index + 1, trimmed, index, size - 1 - index);
        this.setArray(trimmed);
    }

    @SuppressWarnings("unchecked")
    private void setArray(Object newArray) {
        ((ConfigValue<Object>) this.array).set(newArray);
    }

    /**
     * Writes element edits back into the parent array. Uses copy-on-write: the field's initial array is the same
     * instance as the stored default value, so mutating in place would corrupt the defaults.
     */
    private final class ElementContext implements TypeAdapter.AdapterContext {

        private final TypeAdapter adapter;

        private final Field owner;

        private final int index;

        private ElementContext(TypeAdapter adapter, Field owner, int index) {
            this.adapter = adapter;
            this.owner = owner;
            this.index = index;
        }

        @Override
        public TypeAdapter getAdapter() {
            return this.adapter;
        }

        @Override
        public Field getOwner() {
            return this.owner;
        }

        @Override
        public void setFieldValue(Object value) {
            var size = ArrayConfigScreen.this.getSize();
            if (this.index >= size)
                return;
            var current = Array.get(ArrayConfigScreen.this.array.get(), this.index);
            if (value.equals(current))
                return;
            var copy = ArrayConfigScreen.this.copyArray(size, size);
            Array.set(copy, this.index, value);
            ArrayConfigScreen.this.setArray(copy);
        }
    }
}
