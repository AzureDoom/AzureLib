/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for field to config serialization. Only public instance fields are allowed.
 *
 * @author Toma
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configurable {

    Side side() default Side.COMMON;

    enum Side {
        COMMON,
        CLIENT,
        SERVER
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Comment {

        String[] value();
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Synchronized {}

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Range {

        long min() default Long.MIN_VALUE;

        long max() default Long.MAX_VALUE;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface DecimalRange {

        double min() default -Double.MAX_VALUE;

        double max() default Double.MAX_VALUE;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface StringPattern {

        String value();

        String defaultValue() default "";

        int flags() default 0;

        String errorDescriptor() default "";
    }

    /**
     * Allows you to lock array size based on default provided value. Applicable to all arrays.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface FixedSize {}

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface ValueUpdateCallback {

        String method();

        boolean allowPrimitivesMapping() default true;
    }

    final class Gui {

        @Target(ElementType.FIELD)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface NumberFormat {

            String value();
        }

        @Target(ElementType.FIELD)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface ColorValue {

            boolean isARGB() default false;

            String getGuiColorPrefix() default "#";
        }

        @Target(ElementType.FIELD)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface CharacterLimit {

            int value() default 32;
        }
    }
}
