package mod.azure.azurelib.common.internal.client.config;

import mod.azure.azurelib.common.internal.client.config.screen.ArrayConfigScreen;
import mod.azure.azurelib.common.internal.client.config.screen.ConfigScreen;
import mod.azure.azurelib.common.internal.client.config.widget.BooleanWidget;
import mod.azure.azurelib.common.internal.client.config.widget.ColorWidget;
import mod.azure.azurelib.common.internal.client.config.widget.ConfigEntryWidget;
import mod.azure.azurelib.common.internal.client.config.widget.EnumWidget;
import mod.azure.azurelib.common.internal.common.config.ConfigUtils;
import mod.azure.azurelib.common.internal.common.config.Configurable;
import mod.azure.azurelib.common.internal.common.config.validate.ValidationResult;
import mod.azure.azurelib.common.internal.common.config.value.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

@FunctionalInterface
public interface DisplayAdapter {

    void placeWidgets(ConfigValue<?> value, Field field, WidgetAdder container);

    static DisplayAdapter booleanValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> new BooleanWidget(getValueX(x, width), y, getValueWidth(width), 20, (BooleanValue) value));
    }

    static DisplayAdapter characterValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> {
            EditBox widget = new EditBox(Minecraft.getInstance().font, getValueX(x, width), y, getValueWidth(width), 20, CommonComponents.EMPTY);
            CharValue charValue = (CharValue) value;
            char character = charValue.get();
            widget.setValue(String.valueOf(character));
            widget.setFilter(str -> str.length() <= 1);
            widget.setResponder(str -> {
                if (!str.isEmpty()) {
                    container.setOkStatus();
                    char toSet = str.charAt(0);
                    charValue.setWithValidationHandler(toSet, container);
                } else {
                    container.setValidationResult(ValidationResult.error(ClientErrors.CHAR_VALUE_EMPTY));
                }
            });
            ConfigUtils.adjustCharacterLimit(field, widget);
            return widget;
        });
    }

    static DisplayAdapter integerValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> {
            EditBox tfw = new EditBox(Minecraft.getInstance().font, getValueX(x, width), y, getValueWidth(width), 20, CommonComponents.EMPTY);
            IntValue intValue = (IntValue) value;
            int num = intValue.get();
            tfw.setValue(String.valueOf(num));
            tfw.setFilter(str -> ConfigUtils.containsOnlyValidCharacters(str, ConfigUtils.INTEGER_CHARS));
            tfw.setResponder(str -> {
                if (!ConfigUtils.INTEGER_PATTERN.matcher(str).matches()) {
                    container.setValidationResult(ValidationResult.error(ClientErrors.notANumber(str)));
                    return;
                }
                int n;
                try {
                    n = Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    container.setValidationResult(ValidationResult.error(ClientErrors.notANumber(str)));
                    return;
                }
                IntegerValue.Range range = intValue.getRange();
                if (!range.isWithin(n)) {
                    container.setValidationResult(ValidationResult.error(ClientErrors.outOfBounds(n, range)));
                    return;
                }
                container.setOkStatus();
                intValue.setWithValidationHandler(n, container);
            });
            ConfigUtils.adjustCharacterLimit(field, tfw);
            return tfw;
        });
    }

    static DisplayAdapter longValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> {
            EditBox tfw = new EditBox(Minecraft.getInstance().font, getValueX(x, width), y, getValueWidth(width), 20, CommonComponents.EMPTY);
            LongValue longValue = (LongValue) value;
            long num = longValue.get();
            tfw.setValue(String.valueOf(num));
            tfw.setFilter(str -> ConfigUtils.containsOnlyValidCharacters(str, ConfigUtils.INTEGER_CHARS));
            tfw.setResponder(str -> {
                if (!ConfigUtils.INTEGER_PATTERN.matcher(str).matches()) {
                    container.setValidationResult(ValidationResult.error(ClientErrors.notANumber(str)));
                    return;
                }
                long n;
                try {
                    n = Long.parseLong(str);
                } catch (NumberFormatException e) {
                    container.setValidationResult(ValidationResult.error(ClientErrors.notANumber(str)));
                    return;
                }
                IntegerValue.Range range = longValue.getRange();
                if (!range.isWithin(n)) {
                    container.setValidationResult(ValidationResult.error(ClientErrors.outOfBounds(n, range)));
                    return;
                }
                container.setOkStatus();
                longValue.setWithValidationHandler(n, container);
            });
            ConfigUtils.adjustCharacterLimit(field, tfw);
            return tfw;
        });
    }

    static DisplayAdapter floatValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> {
            EditBox tfw = new EditBox(Minecraft.getInstance().font, getValueX(x, width), y, getValueWidth(width), 20, CommonComponents.EMPTY);
            FloatValue floatValue = (FloatValue) value;
            DecimalFormat format = ConfigUtils.getDecimalFormat(field);
            float number = floatValue.get();
            tfw.setValue(format != null ? format.format(number) : String.valueOf(number));
            tfw.setFilter(str -> ConfigUtils.containsOnlyValidCharacters(str, ConfigUtils.DECIMAL_CHARS));
            tfw.setResponder(str -> {
                if (!ConfigUtils.DECIMAL_PATTERN.matcher(str).matches()) {
                    container.setValidationResult(ValidationResult.error(ClientErrors.notANumber(str)));
                    return;
                }
                float n;
                try {
                    n = Float.parseFloat(str);
                } catch (NumberFormatException e) {
                    container.setValidationResult(ValidationResult.error(ClientErrors.notANumber(str)));
                    return;
                }
                DecimalValue.Range range = floatValue.getRange();
                if (!range.isWithin(n)) {
                    container.setValidationResult(ValidationResult.error(ClientErrors.outOfBounds(n, range)));
                    return;
                }
                container.setOkStatus();
                floatValue.setWithValidationHandler(n, container);
            });
            ConfigUtils.adjustCharacterLimit(field, tfw);
            return tfw;
        });
    }

    static DisplayAdapter doubleValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> {
            EditBox tfw = new EditBox(Minecraft.getInstance().font, getValueX(x, width), y, getValueWidth(width), 20, CommonComponents.EMPTY);
            DoubleValue doubleValue = (DoubleValue) value;
            DecimalFormat format = ConfigUtils.getDecimalFormat(field);
            double number = doubleValue.get();
            tfw.setValue(format != null ? format.format(number) : String.valueOf(number));
            tfw.setFilter(str -> ConfigUtils.containsOnlyValidCharacters(str, ConfigUtils.DECIMAL_CHARS));
            tfw.setResponder(str -> {
                if (!ConfigUtils.DECIMAL_PATTERN.matcher(str).matches()) {
                    container.setValidationResult(ValidationResult.error(ClientErrors.notANumber(str)));
                    return;
                }
                double n;
                try {
                    n = Double.parseDouble(str);
                } catch (NumberFormatException e) {
                    container.setValidationResult(ValidationResult.error(ClientErrors.notANumber(str)));
                    return;
                }
                DecimalValue.Range range = doubleValue.getRange();
                if (!range.isWithin(n)) {
                    container.setValidationResult(ValidationResult.error(ClientErrors.outOfBounds(n, range)));
                    return;
                }
                container.setOkStatus();
                doubleValue.setWithValidationHandler(n, container);
            });
            ConfigUtils.adjustCharacterLimit(field, tfw);
            return tfw;
        });
    }

    static DisplayAdapter stringValue() {
        return (value, field, container) -> {
            Configurable.Gui.ColorValue colorValue = field.getAnnotation(Configurable.Gui.ColorValue.class);
            StringValue strValue = (StringValue) value;
            EditBox widget = container.addConfigWidget((x, y, width, height, configId) -> {
                EditBox tfw = new EditBox(Minecraft.getInstance().font, getValueX(x, width), y, getValueWidth(width), 20, CommonComponents.EMPTY);
                String val = strValue.get();
                tfw.setValue(val);
                tfw.setResponder(str -> {
                    Pattern pattern = strValue.getPattern();
                    if (pattern != null) {
                        if (!pattern.matcher(str).matches()) {
                            String errDescriptor = strValue.getErrorDescriptor();
                            MutableComponent error = errDescriptor != null ? Component.translatable(errDescriptor, str, pattern) : ClientErrors.invalidText(str, pattern);
                            container.setValidationResult(ValidationResult.error(error));
                            return;
                        }
                    }
                    container.setOkStatus();
                    strValue.setWithValidationHandler(str, container);
                });
                ConfigUtils.adjustCharacterLimit(field, tfw);
                return tfw;
            });
            if (colorValue != null) {
                container.addConfigWidget((x, y, width, height, configId) -> {
                    int left = getValueX(x, width) - 25;
                    ColorWidget.GetSet<String> provider = ColorWidget.GetSet.of(widget::getValue, widget::setValue);
                    Screen currentScreen = Minecraft.getInstance().screen;
                    return new ColorWidget(left, y, 20, 20, colorValue, provider, currentScreen);
                });
            }
        };
    }

    static DisplayAdapter booleanArrayValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> {
            BooleanArrayValue arrayValue = (BooleanArrayValue) value;
            BiConsumer<Boolean, Integer> setCallback = (val, i) -> {
                boolean[] arr = arrayValue.get();
                arr[i] = val;
                arrayValue.set(arr);
            };
            Button.OnPress pressable = btn -> {
                Minecraft client = Minecraft.getInstance();
                Screen usedScreen = client.screen;
                ArrayConfigScreen<boolean[], BooleanArrayValue> screen = new ArrayConfigScreen<>(value.getId(), configId, arrayValue, usedScreen);
                screen.fetchSize(() -> arrayValue.get().length);
                screen.valueFactory((id, i) -> {
                    boolean[] arr = arrayValue.get();
                    return new BooleanValue(ValueData.of(id, arr[i], ArrayConfigScreen.callbackCtx(field, Boolean.TYPE, setCallback, i)));
                });
                screen.addElement(() -> {
                    boolean[] arr = arrayValue.get();
                    boolean[] expanded = new boolean[arr.length + 1];
                    System.arraycopy(arr, 0, expanded, 0, arr.length);
                    expanded[arr.length] = false;
                    arrayValue.set(expanded);
                });
                screen.removeElement((i, trimmer) -> {
                    boolean[] arr = arrayValue.get();
                    arrayValue.set(trimmer.trim(i, arr, new boolean[arr.length - 1]));
                });
                client.setScreen(screen);
            };
            return Button.builder(ConfigEntryWidget.EDIT, pressable).pos(getValueX(x, width), y).size(getValueWidth(width), 20).build();
        });
    }

    static DisplayAdapter integerArrayValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> {
            IntArrayValue arrayValue = (IntArrayValue) value;
            BiConsumer<Integer, Integer> setCallback = (val, i) -> {
                int[] arr = arrayValue.get();
                arr[i] = val;
                arrayValue.set(arr);
            };
            Button.OnPress pressable = btn -> {
                Minecraft client = Minecraft.getInstance();
                Screen usedScreen = client.screen;
                ArrayConfigScreen<int[], IntArrayValue> screen = new ArrayConfigScreen<>(value.getId(), configId, arrayValue, usedScreen);
                screen.fetchSize(() -> arrayValue.get().length);
                screen.valueFactory((id, i) -> {
                    int[] arr = arrayValue.get();
                    return new IntValue(ValueData.of(id, arr[i], ArrayConfigScreen.callbackCtx(field, Integer.TYPE, setCallback, i)));
                });
                screen.addElement(() -> {
                    int[] arr = arrayValue.get();
                    int[] expanded = new int[arr.length + 1];
                    System.arraycopy(arr, 0, expanded, 0, arr.length);
                    expanded[arr.length] = Math.max((int) arrayValue.getRange().min(), 0);
                    arrayValue.set(expanded);
                });
                screen.removeElement((i, trimmer) -> {
                    int[] arr = arrayValue.get();
                    arrayValue.set(trimmer.trim(i, arr, new int[arr.length - 1]));
                });
                client.setScreen(screen);
            };
            return Button.builder(ConfigEntryWidget.EDIT, pressable).pos(getValueX(x, width), y).size(getValueWidth(width), 20).build();
        });
    }

    static DisplayAdapter longArrayValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> {
            LongArrayValue arrayValue = (LongArrayValue) value;
            BiConsumer<Long, Integer> setCallback = (val, i) -> {
                long[] arr = arrayValue.get();
                arr[i] = val;
                arrayValue.set(arr);
            };
            Button.OnPress pressable = btn -> {
                Minecraft client = Minecraft.getInstance();
                Screen usedScreen = client.screen;
                ArrayConfigScreen<long[], LongArrayValue> screen = new ArrayConfigScreen<>(value.getId(), configId, arrayValue, usedScreen);
                screen.fetchSize(() -> arrayValue.get().length);
                screen.valueFactory((id, i) -> {
                    long[] arr = arrayValue.get();
                    return new LongValue(ValueData.of(id, arr[i], ArrayConfigScreen.callbackCtx(field, Long.TYPE, setCallback, i)));
                });
                screen.addElement(() -> {
                    long[] arr = arrayValue.get();
                    long[] expanded = new long[arr.length + 1];
                    System.arraycopy(arr, 0, expanded, 0, arr.length);
                    expanded[arr.length] = Math.max(arrayValue.getRange().min(), 0);
                    arrayValue.set(expanded);
                });
                screen.removeElement((i, trimmer) -> {
                    long[] arr = arrayValue.get();
                    arrayValue.set(trimmer.trim(i, arr, new long[arr.length - 1]));
                });
                client.setScreen(screen);
            };
            return Button.builder(ConfigEntryWidget.EDIT, pressable).pos(getValueX(x, width), y).size(getValueWidth(width), 20).build();
        });
    }

    static DisplayAdapter floatArrayValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> {
            FloatArrayValue arrayValue = (FloatArrayValue) value;
            BiConsumer<Float, Integer> setCallback = (val, i) -> {
                float[] arr = arrayValue.get();
                arr[i] = val;
                arrayValue.set(arr);
            };
            Button.OnPress pressable = btn -> {
                Minecraft client = Minecraft.getInstance();
                Screen usedScreen = client.screen;
                ArrayConfigScreen<float[], FloatArrayValue> screen = new ArrayConfigScreen<>(value.getId(), configId, arrayValue, usedScreen);
                screen.fetchSize(() -> arrayValue.get().length);
                screen.valueFactory((id, i) -> {
                    float[] arr = arrayValue.get();
                    return new FloatValue(ValueData.of(id, arr[i], ArrayConfigScreen.callbackCtx(field, Float.TYPE, setCallback, i)));
                });
                screen.addElement(() -> {
                    float[] arr = arrayValue.get();
                    float[] expanded = new float[arr.length + 1];
                    System.arraycopy(arr, 0, expanded, 0, arr.length);
                    expanded[arr.length] = Math.max((float) arrayValue.getRange().min(), 0);
                    arrayValue.set(expanded);
                });
                screen.removeElement((i, trimmer) -> {
                    float[] arr = arrayValue.get();
                    arrayValue.set(trimmer.trim(i, arr, new float[arr.length - 1]));
                });
                client.setScreen(screen);
            };
            return Button.builder(ConfigEntryWidget.EDIT, pressable).pos(getValueX(x, width), y).size(getValueWidth(width), 20).build();
        });
    }

    static DisplayAdapter doubleArrayValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> {
            DoubleArrayValue arrayValue = (DoubleArrayValue) value;
            BiConsumer<Double, Integer> setCallback = (val, i) -> {
                double[] arr = arrayValue.get();
                arr[i] = val;
                arrayValue.set(arr);
            };
            Button.OnPress pressable = btn -> {
                Minecraft client = Minecraft.getInstance();
                Screen usedScreen = client.screen;
                ArrayConfigScreen<double[], DoubleArrayValue> screen = new ArrayConfigScreen<>(value.getId(), configId, arrayValue, usedScreen);
                screen.fetchSize(() -> arrayValue.get().length);
                screen.valueFactory((id, i) -> {
                    double[] arr = arrayValue.get();
                    return new DoubleValue(ValueData.of(id, arr[i], ArrayConfigScreen.callbackCtx(field, Double.TYPE, setCallback, i)));
                });
                screen.addElement(() -> {
                    double[] arr = arrayValue.get();
                    double[] expanded = new double[arr.length + 1];
                    System.arraycopy(arr, 0, expanded, 0, arr.length);
                    expanded[arr.length] = Math.max(arrayValue.getRange().min(), 0);
                    arrayValue.set(expanded);
                });
                screen.removeElement((i, trimmer) -> {
                    double[] arr = arrayValue.get();
                    arrayValue.set(trimmer.trim(i, arr, new double[arr.length - 1]));
                });
                client.setScreen(screen);
            };
            return Button.builder(ConfigEntryWidget.EDIT, pressable).pos(getValueX(x, width), y).size(getValueWidth(width), 20).build();
        });
    }

    static DisplayAdapter stringArrayValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> {
            StringArrayValue arrayValue = (StringArrayValue) value;
            BiConsumer<String, Integer> setCallback = (val, i) -> {
                String[] arr = arrayValue.get();
                arr[i] = val;
                arrayValue.set(arr);
            };
            Button.OnPress pressable = btn -> {
                Minecraft client = Minecraft.getInstance();
                Screen usedScreen = client.screen;
                ArrayConfigScreen<String[], StringArrayValue> screen = new ArrayConfigScreen<>(value.getId(), configId, arrayValue, usedScreen);
                screen.fetchSize(() -> arrayValue.get().length);
                screen.valueFactory((id, i) -> {
                    String[] arr = arrayValue.get();
                    return new StringValue(ValueData.of(id, arr[i], ArrayConfigScreen.callbackCtx(field, String.class, setCallback, i)));
                });
                screen.addElement(() -> {
                    String[] arr = arrayValue.get();
                    String[] expanded = new String[arr.length + 1];
                    System.arraycopy(arr, 0, expanded, 0, arr.length);
                    expanded[arr.length] = arrayValue.getDefaultElementValue();
                    arrayValue.set(expanded);
                });
                screen.removeElement((i, trimmer) -> {
                    String[] arr = arrayValue.get();
                    arrayValue.set(trimmer.trim(i, arr, new String[arr.length - 1]));
                });
                client.setScreen(screen);
            };
            return Button.builder(ConfigEntryWidget.EDIT, pressable).pos(getValueX(x, width), y).size(getValueWidth(width), 20).build();
        });
    }

    static DisplayAdapter enumValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> new EnumWidget<>(getValueX(x, width), y, getValueWidth(width), 20, (EnumValue<?>) value));
    }

    static DisplayAdapter objectValue() {
        return (value, field, container) -> container.addConfigWidget((x, y, width, height, configId) -> {
            ObjectValue objectValue = (ObjectValue) value;
            Map<String, ConfigValue<?>> valueMap = objectValue.get();
            Button.OnPress pressable = btn -> {
                Minecraft client = Minecraft.getInstance();
                Screen currentScreen = client.screen;
                Screen nestedConfigScreen = new ConfigScreen(container.getComponentName(), configId, valueMap, currentScreen);
                client.setScreen(nestedConfigScreen);
            };
            return Button.builder(ConfigEntryWidget.EDIT, pressable).pos(getValueX(x, width), y).size(getValueWidth(width), 20).build();
        });
    }

    static int getValueX(int x, int width) {
        return x + width - getValueWidth(width);
    }

    static int getValueWidth(int width) {
        return width / 3;
    }
}
