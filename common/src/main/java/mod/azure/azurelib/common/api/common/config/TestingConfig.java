package mod.azure.azurelib.common.api.common.config;

import java.util.Arrays;
import java.util.regex.Pattern;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.client.config.IValidationHandler;
import mod.azure.azurelib.common.internal.common.config.Configurable;
import mod.azure.azurelib.common.internal.common.config.validate.ValidationResult;
import net.minecraft.network.chat.Component;

@Config(id = AzureLib.MOD_ID)
public final class TestingConfig {

    @Configurable
    public boolean bool = true;

    @Configurable
    @Configurable.Synchronized
    public int number = 15;

    @Configurable
    public long longNumber = 16644564564561651L;

    @Configurable
    public float floatNumber = 151.3123F;

    @Configurable
    public double doubleNumber = 316.15646556D;

    @Configurable
    @Configurable.StringPattern(value = "[a-z\\s]+", flags = Pattern.CASE_INSENSITIVE)
    public String string = "random text";

    @Configurable
    @Configurable.StringPattern(value = "#[0-9a-fA-F]{1,6}")
    @Configurable.Gui.ColorValue
    public String color = "#33AADD";

    @Configurable
    @Configurable.StringPattern(value = "#[0-9a-fA-F]{1,8}")
    @Configurable.Gui.ColorValue(isARGB = true)
    public String color2 = "#66771166";

    @Configurable
    @Configurable.FixedSize
    public boolean[] boolArray = {false, false, true, false};

    @Configurable
    @Configurable.Range(min = 50, max = 160)
    public int[] intArray = {153, 123, 54};

    @Configurable
    public long[] longArray = {13, 56, 133};

    @Configurable
    @Configurable.DecimalRange(min = 500.0F)
    public float[] floatArray = {135.32F, 1561.23F};

    @Configurable
    @Configurable.ValueUpdateCallback(method = "onUpdate")
    public String[] stringArray = {"minecraft:test"};

    @Configurable
    public TestEnum testEnum = TestEnum.C;

    @Configurable
    public NestedTest nestedTest = new NestedTest();

    public enum TestEnum {
        A, B, C, D
    }

    public void onUpdate(String[] value, IValidationHandler handler) {
        AzureLib.LOGGER.debug(() -> Arrays.toString(value));
        handler.setValidationResult(ValidationResult.warn(Component.translatable("config.azurelib.option.genericwarning")));
    }

    public static class NestedTest {

        @Configurable
        @Configurable.ValueUpdateCallback(method = "onUpdate")
        public int testInt = 13;

        public void onUpdate(int value, IValidationHandler handler) {
            if (value == 0) {
                handler.setValidationResult(ValidationResult.warn(Component.literal("value is 0")));
            }
        }
    }
}
