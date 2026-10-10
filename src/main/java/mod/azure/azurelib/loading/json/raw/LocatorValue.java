/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for locator value information, only used in deserialization at startup
 */
public final class LocatorValue {

    private final LocatorClass locatorClass;

    private final double[] values;

    public LocatorValue(LocatorClass locatorClass, double[] values) {
        this.locatorClass = locatorClass;
        this.values = values;
    }

    public LocatorClass locatorClass() {
        return this.locatorClass;
    }

    public double[] values() {
        return this.values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LocatorValue))
            return false;
        LocatorValue other = (LocatorValue) o;
        return java.util.Objects.equals(this.locatorClass, other.locatorClass)
            && java.util.Objects.equals(this.values, other.values);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.locatorClass);
        result = 31 * result + java.util.Objects.hashCode(this.values);
        return result;
    }

    @Override
    public String toString() {
        return "LocatorValue[locatorClass=" + this.locatorClass + ", values=" + this.values + "]";
    }

    public static JsonDeserializer<LocatorValue> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            if (json.isJsonArray()) {
                return new LocatorValue(null, JsonUtil.jsonArrayToDoubleArray(json.getAsJsonArray()));
            } else if (json.isJsonObject()) {
                return new LocatorValue(
                    context.deserialize(json.getAsJsonObject(), mod.azure.azurelib.loading.json.raw.LocatorClass.class),
                    new double[0]
                );
            } else {
                throw new JsonParseException("Invalid format for LocatorValue in json");
            }
        };
    }
}
