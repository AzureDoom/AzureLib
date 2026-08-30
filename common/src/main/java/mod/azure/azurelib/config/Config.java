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
 * Config marker annotation. Every registered config class must have this annotation. Inside this class you should
 * define all configurable fields <b>(cannot be {@code STATIC})!</b>. All configurable fields must be annotated with
 * {@link Configurable} annotation, otherwise it will be ignored.
 *
 * @author Toma
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {

    /**
     * This value should be globally unique. It is suggested to use your mod ID as prefix or standalone based on how
     * many configs you're creating.
     *
     * @return Unique config identifier
     */
    String id();

    String filename() default "";

    String group() default "";

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface NoAutoSync {}
}
