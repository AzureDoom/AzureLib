package mod.azure.azurelib.common.internal.common.config.format;

import java.util.function.Supplier;

/**
 * Collection and factory methods for config formats natively supported by
 * this library. Note that there are provided methods which allow you to
 * customize the config format, for example you can customize the GSON object
 * in for JSON configs or spacing/separators for Properties configs.
 *
 * @author Toma
 */
public final class ConfigFormats {

    // file extensions
    private static final String EXT_JSON = "json";
    private static final String EXT_YAML = "yaml";
    private static final String EXT_PROPERTIES = "properties";

    /**
     * Creates new JSON config format handler with customized format settings.
     *
     * @param settings Settings to be used by this format
     * @return new instance of config format handler for JSON configs
     */
    public static IConfigFormatHandler json(GsonFormat.Settings settings) {
        return new SimpleFormatImpl(EXT_JSON, () -> new GsonFormat(settings));
    }

    /**
     * Creates new JSON config format handler with default format settings.
     *
     * @return new instance of config format handler for JSON configs
     */
    public static IConfigFormatHandler json() {
        return json(new GsonFormat.Settings());
    }

    /**
     * Creates new YAML config format handler with default format settings
     *
     * @return new instance of config format handler for YAML configs
     */
    public static IConfigFormatHandler yaml() {
        return new SimpleFormatImpl(EXT_YAML, YamlFormat::new);
    }

    /**
     * Creates new Properties based config format handler with customized format settings
     *
     * @param settings Settings to be used by this format
     * @return new instance of config format handler for Properties configs
     */
    public static IConfigFormatHandler properties(PropertiesFormat.Settings settings) {
        return new SimpleFormatImpl(EXT_PROPERTIES, () -> new PropertiesFormat(settings));
    }

    /**
     * Creates new Properties based config format handler with default format settings

     * @return new instance of config format handler for Properties configs
     */
    public static IConfigFormatHandler properties() {
        return properties(new PropertiesFormat.Settings());
    }

    private static final class SimpleFormatImpl implements IConfigFormatHandler {

        private final String extension;
        private final Supplier<IConfigFormat> factory;

        public SimpleFormatImpl(String extension, Supplier<IConfigFormat> factory) {
            this.extension = extension;
            this.factory = factory;
        }

        @Override
        public IConfigFormat createFormat() {
            return factory.get();
        }

        @Override
        public String fileExt() {
            return extension;
        }
    }
}
