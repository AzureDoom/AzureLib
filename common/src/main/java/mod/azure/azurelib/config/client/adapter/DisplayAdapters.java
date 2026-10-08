/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client.adapter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.ConfigUtils;
import mod.azure.azurelib.config.Configurable;
import mod.azure.azurelib.config.client.ClientErrors;
import mod.azure.azurelib.config.client.IValidationHandler;
import mod.azure.azurelib.config.client.WidgetAdder;
import mod.azure.azurelib.config.client.screen.ArrayConfigScreen;
import mod.azure.azurelib.config.client.screen.ConfigScreen;
import mod.azure.azurelib.config.client.screen.WidgetPlacerHelper;
import mod.azure.azurelib.config.client.widget.ColorWidget;
import mod.azure.azurelib.config.client.widget.ConfigEntryWidget;
import mod.azure.azurelib.config.validate.ValidationResult;
import mod.azure.azurelib.config.value.*;

/**
 * Registry of {@link DisplayAdapter}s, keyed by {@link ConfigValue} class. Custom registrations take precedence over
 * the built-in ones, so mods can override how a value type is displayed.
 */
@SuppressWarnings("unused")
public final class DisplayAdapters {

    private static final List<Entry> ADAPTERS = new ArrayList<>();

    private static final Component TRUE = Component.translatable("text.azurelib.value.true");

    private static final Component FALSE = Component.translatable("text.azurelib.value.false");

    private static final int DEFAULT_TEXT_LIMIT = Short.MAX_VALUE;

    static {
        register(BooleanValue.class, DisplayAdapters::booleanAdapter);
        register(CharValue.class, DisplayAdapters::charAdapter);
        register(IntValue.class, integerAdapter(Integer::parseInt));
        register(LongValue.class, integerAdapter(Long::parseLong));
        register(FloatValue.class, decimalAdapter(Float::parseFloat));
        register(DoubleValue.class, decimalAdapter(Double::parseDouble));
        register(StringValue.class, DisplayAdapters::stringAdapter);
        register(EnumValue.class, DisplayAdapters::enumAdapter);
        register(ObjectValue.class, DisplayAdapters::objectAdapter);
        register(ArrayValue.class, DisplayAdapters::arrayAdapter);
    }

    private DisplayAdapters() {
        throw new UnsupportedOperationException();
    }

    public static void registerCustom(Class<?> valueType, DisplayAdapter adapter) {
        ADAPTERS.addFirst(new Entry(Objects.requireNonNull(valueType), Objects.requireNonNull(adapter)));
    }

    private static void register(Class<?> valueType, DisplayAdapter adapter) {
        ADAPTERS.add(new Entry(valueType, adapter));
    }

    @Nullable
    public static DisplayAdapter forValue(ConfigValue<?> value) {
        for (Entry entry : ADAPTERS) {
            if (entry.type.isInstance(value)) {
                return entry.adapter;
            }
        }
        return null;
    }

    private static int left(int x, int width) {
        return WidgetPlacerHelper.getLeft(x, width);
    }

    private static int width(int width) {
        return WidgetPlacerHelper.getWidth(width);
    }

    /**
     * Applies a new value: clears the row status, then runs the value's {@code @ValueUpdateCallback} (which may report
     * a warning/error through the handler) and stores the value.
     */
    @SuppressWarnings("unchecked")
    public static <T> void applyValue(ConfigValue<?> value, T newValue, IValidationHandler handler) {
        handler.setOkStatus();
        ((ConfigValue<T>) value).setWithValidationHandler(newValue, handler);
    }

    private static EditBox createEditBox(WidgetAdder container, Field field, int leftOffset) {
        var font = Minecraft.getInstance().font;
        var box = container.addConfigWidget(
            (x, y, w, h, configId) -> new EditBox(
                font,
                left(x, w) + leftOffset,
                y,
                width(w) - leftOffset,
                h,
                CommonComponents.EMPTY
            )
        );
        box.setMaxLength(DEFAULT_TEXT_LIMIT);
        ConfigUtils.adjustCharacterLimit(field, box);
        return box;
    }

    private static void createOpenButton(WidgetAdder container, Button.OnPress onPress) {
        container.addConfigWidget(
            (x, y, w, h, configId) -> Button.builder(ConfigEntryWidget.EDIT, onPress)
                .pos(left(x, w), y)
                .size(width(w), h)
                .build()
        );
    }

    private static void booleanAdapter(
        ConfigHolder<?> holder,
        ConfigValue<?> value,
        Field field,
        WidgetAdder container
    ) {
        var booleanValue = (BooleanValue) value;
        container.addConfigWidget(
            (x, y, w, h, configId) -> Button.builder(booleanLabel(booleanValue.get()), btn -> {
                applyValue(booleanValue, !booleanValue.get(), container);
                btn.setMessage(booleanLabel(booleanValue.get()));
            }).pos(left(x, w), y).size(width(w), h).build()
        );
    }

    private static Component booleanLabel(boolean value) {
        return value ? TRUE : FALSE;
    }

    private static void charAdapter(ConfigHolder<?> holder, ConfigValue<?> value, Field field, WidgetAdder container) {
        var charValue = (CharValue) value;
        var box = createEditBox(container, field, 0);
        box.setMaxLength(1);
        box.setValue(charValue.get().toString());
        box.setResponder(text -> {
            if (text.isEmpty()) {
                container.setValidationResult(ValidationResult.error(ClientErrors.CHAR_VALUE_EMPTY));
                return;
            }
            applyValue(charValue, text.charAt(0), container);
        });
    }

    private static <N extends Number> DisplayAdapter integerAdapter(Function<String, N> parser) {
        return (holder, value, field, container) -> {
            var range = ((IntegerValue<?>) value).getRange();
            placeNumberBox(
                value,
                field,
                container,
                parser,
                number -> range.isWithin(number.longValue())
                    ? null
                    : ClientErrors.outOfBounds(number, range.min(), range.max())
            );
        };
    }

    private static <N extends Number> DisplayAdapter decimalAdapter(Function<String, N> parser) {
        return (holder, value, field, container) -> {
            var range = ((DecimalValue<?>) value).getRange();
            placeNumberBox(value, field, container, parser, number -> {
                var d = number.doubleValue();
                if (Double.isNaN(d) || Double.isInfinite(d))
                    return ClientErrors.notANumber(number.toString());
                return range.isWithin(d) ? null : ClientErrors.outOfBounds(number, range.min(), range.max());
            });
        };
    }

    private static <N extends Number> void placeNumberBox(
        ConfigValue<?> value,
        Field field,
        WidgetAdder container,
        Function<String, N> parser,
        Function<N, MutableComponent> rangeCheck
    ) {
        var box = createEditBox(container, field, 0);
        box.setValue(String.valueOf(value.get()));
        var format = ConfigUtils.getDecimalFormat(field);
        if (format != null) {
            box.addFormatter(new NumberFormatter(format, () -> (Number) value.get(), box::isFocused));
        }
        box.setResponder(text -> {
            N parsed;
            try {
                parsed = parser.apply(text.trim());
            } catch (NumberFormatException e) {
                container.setValidationResult(ValidationResult.error(ClientErrors.notANumber(text)));
                return;
            }
            var error = rangeCheck.apply(parsed);
            if (error != null) {
                container.setValidationResult(ValidationResult.error(error));
                return;
            }
            applyValue(value, parsed, container);
        });
    }

    private static void stringAdapter(
        ConfigHolder<?> holder,
        ConfigValue<?> value,
        Field field,
        WidgetAdder container
    ) {
        var stringValue = (StringValue) value;
        var colorValue = field.getAnnotation(Configurable.Gui.ColorValue.class);
        var box = createEditBox(container, field, colorValue != null ? 21 : 0);
        box.setValue(stringValue.get());
        box.setResponder(text -> {
            var pattern = stringValue.getPattern();
            if (pattern != null && !pattern.matcher(text).matches()) {
                var descriptor = stringValue.getErrorDescriptor();
                var error = descriptor != null
                    ? Component.translatable(descriptor, text, pattern.pattern())
                    : ClientErrors.invalidText(text, pattern);
                container.setValidationResult(ValidationResult.error(error));
                return;
            }
            applyValue(stringValue, text, container);
        });

        if (colorValue != null) {
            container.addConfigWidget((x, y, w, h, configId) -> {
                var currentScreen = Minecraft.getInstance().screen;
                return new ColorWidget(
                    left(x, w),
                    y,
                    20,
                    h,
                    colorValue,
                    ColorWidget.GetSet.of(box::getValue, box::setValue),
                    currentScreen
                );
            });
        }
    }

    private static void enumAdapter(ConfigHolder<?> holder, ConfigValue<?> value, Field field, WidgetAdder container) {
        container.addConfigWidget(
            (x, y, w, h, configId) -> Button.builder(enumLabel(value.get()), btn -> {
                var current = (Enum<?>) value.get();
                var constants = current.getDeclaringClass().getEnumConstants();
                var next = constants[(current.ordinal() + 1) % constants.length];
                applyValue(value, next, container);
                btn.setMessage(enumLabel(value.get()));
            }).pos(left(x, w), y).size(width(w), h).build()
        );
    }

    private static Component enumLabel(Object value) {
        return Component.literal(((Enum<?>) value).name());
    }

    private static void objectAdapter(
        ConfigHolder<?> holder,
        ConfigValue<?> value,
        Field field,
        WidgetAdder container
    ) {
        var objectValue = (ObjectValue) value;
        createOpenButton(container, btn -> {
            var client = Minecraft.getInstance();
            var current = client.screen;
            client.setScreen(new ConfigScreen(holder, container.getComponentName(), objectValue.get(), current));
        });
    }

    private static void arrayAdapter(ConfigHolder<?> holder, ConfigValue<?> value, Field field, WidgetAdder container) {
        createOpenButton(container, btn -> {
            var client = Minecraft.getInstance();
            var current = client.screen;
            client.setScreen(new ArrayConfigScreen(holder, container.getComponentName(), value, current));
        });
    }

    private record Entry(
        Class<?> type,
        DisplayAdapter adapter
    ) {}

    /**
     * Displays the value through {@code @Configurable.Gui.NumberFormat} while the box is not being edited.
     */
    private record NumberFormatter(
        DecimalFormat format,
        Supplier<Number> value,
        BooleanSupplier focused
    ) implements EditBox.TextFormatter {

        @Nullable
        @Override
        public FormattedCharSequence format(@NonNull String text, int offset) {
            if (this.focused.getAsBoolean())
                return null;
            var formatted = this.format.format(this.value.get());
            return output -> StringDecomposer.iterate(formatted, Style.EMPTY, output);
        }
    }
}
