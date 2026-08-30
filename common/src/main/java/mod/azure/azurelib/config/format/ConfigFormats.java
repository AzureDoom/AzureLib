/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.format;

import java.util.function.Supplier;

/**
 * Collection and factory methods for config formats natively supported by this library. Note that there are provided
 * methods which allow you to customize the config format, for example you can customize the GSON object in for JSON
 * configs or spacing/separators for Properties configs.
 *
 * @author Toma
 */
@SuppressWarnings("unused")
public final class ConfigFormats {

    private static final String EXT_JSON = "json";

    private static final String EXT_YAML = "yaml";

    private static final String EXT_PROPERTIES = "properties";

    public static IConfigFormatHandler json() {
        return new SimpleFormatImpl(EXT_JSON, GsonFormat::new);
    }

    public static IConfigFormatHandler yaml() {
        return new SimpleFormatImpl(EXT_YAML, YamlFormat::new);
    }

    public static IConfigFormatHandler properties(PropertiesFormat.Settings settings) {
        return new SimpleFormatImpl(EXT_PROPERTIES, () -> new PropertiesFormat(settings));
    }

    public static IConfigFormatHandler properties() {
        return properties(new PropertiesFormat.Settings());
    }

    private record SimpleFormatImpl(
        String fileExt,
        Supplier<IConfigFormat> factory
    ) implements IConfigFormatHandler {

        @Override
        public IConfigFormat createFormat() {
            return factory.get();
        }
    }
}
