package mod.azure.azurelib.common.internal.common.config.adapter;

import java.util.Objects;
import java.util.function.Predicate;

import mod.azure.azurelib.common.internal.common.AzureLib;
import net.minecraft.resources.ResourceLocation;

public interface TypeMatcher extends Predicate<Class<?>> {

    ResourceLocation getIdentifier();

    int priority();

    static TypeMatcher matchBoolean() {
        return NamedMatcherImpl.vanilla("boolean", Boolean.TYPE);
    }

    static TypeMatcher matchCharacter() {
        return NamedMatcherImpl.vanilla("character", Character.TYPE);
    }

    static TypeMatcher matchInteger() {
        return NamedMatcherImpl.vanilla("integer", Integer.TYPE);
    }

    static TypeMatcher matchLong() {
        return NamedMatcherImpl.vanilla("long", Long.TYPE);
    }

    static TypeMatcher matchFloat() {
        return NamedMatcherImpl.vanilla("float", Float.TYPE);
    }

    static TypeMatcher matchDouble() {
        return NamedMatcherImpl.vanilla("double", Double.TYPE);
    }

    static TypeMatcher matchString() {
        return NamedMatcherImpl.vanilla("string", String.class);
    }

    static TypeMatcher matchBooleanArray() {
        return NamedMatcherImpl.vanilla("array/boolean", boolean[].class);
    }

    static TypeMatcher matchIntegerArray() {
        return NamedMatcherImpl.vanilla("array/integer", int[].class);
    }

    static TypeMatcher matchLongArray() {
        return NamedMatcherImpl.vanilla("array/long", long[].class);
    }

    static TypeMatcher matchFloatArray() {
        return NamedMatcherImpl.vanilla("array/float", float[].class);
    }

    static TypeMatcher matchDoubleArray() {
        return NamedMatcherImpl.vanilla("array/double", double[].class);
    }

    static TypeMatcher matchStringArray() {
        return NamedMatcherImpl.vanilla("array/string", String[].class);
    }

    static TypeMatcher matchEnum() {
        return NamedMatcherImpl.vanilla("enum", Class::isEnum);
    }

    static TypeMatcher matchEnumArray() {
        return NamedMatcherImpl.vanilla("array/enum", type -> type.isArray() && type.getComponentType().isEnum());
    }

    static TypeMatcher matchObject() {
        return NamedMatcherImpl.vanilla("object", type -> !type.isArray())
                .withPriority(Integer.MAX_VALUE);
    }

    class NamedMatcherImpl implements TypeMatcher {

        private final ResourceLocation identifier;
        private final Predicate<Class<?>> matcher;
        private int priority;

        public NamedMatcherImpl(ResourceLocation identifier, Predicate<Class<?>> matcher) {
            this.identifier = Objects.requireNonNull(identifier);
            this.matcher = Objects.requireNonNull(matcher);
        }

        public static NamedMatcherImpl vanilla(String path, Predicate<Class<?>> matcher) {
            return new NamedMatcherImpl(AzureLib.modResource(path), matcher);
        }

        public static NamedMatcherImpl vanilla(String path, Class<?> requiredType) {
            return new NamedMatcherImpl(AzureLib.modResource(path), type -> type.equals(requiredType));
        }

        public NamedMatcherImpl withPriority(int priority) {
            this.priority = priority;
            return this;
        }

        @Override
        public boolean test(Class<?> aClass) {
            return matcher.test(aClass);
        }

        @Override
        public ResourceLocation getIdentifier() {
            return identifier;
        }

        @Override
        public int priority() {
            return priority;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NamedMatcherImpl that = (NamedMatcherImpl) o;
            return identifier.equals(that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }

        @Override
        public String toString() {
            return "NamedMatcherImpl{identifier=" + identifier + "}";
        }
    }
}
