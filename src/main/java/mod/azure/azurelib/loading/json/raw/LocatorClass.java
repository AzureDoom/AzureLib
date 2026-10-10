/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.GsonHelper;
import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for locator class information, only used in deserialization at startup
 */
public final class LocatorClass {

    private final Boolean ignoreInheritedScale;

    private final double[] offset;

    private final double[] rotation;

    public LocatorClass(Boolean ignoreInheritedScale, double[] offset, double[] rotation) {
        this.ignoreInheritedScale = ignoreInheritedScale;
        this.offset = offset;
        this.rotation = rotation;
    }

    public Boolean ignoreInheritedScale() {
        return this.ignoreInheritedScale;
    }

    public double[] offset() {
        return this.offset;
    }

    public double[] rotation() {
        return this.rotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LocatorClass))
            return false;
        LocatorClass other = (LocatorClass) o;
        return java.util.Objects.equals(this.ignoreInheritedScale, other.ignoreInheritedScale)
            && java.util.Objects.equals(this.offset, other.offset)
            && java.util.Objects.equals(this.rotation, other.rotation);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.ignoreInheritedScale);
        result = 31 * result + java.util.Objects.hashCode(this.offset);
        result = 31 * result + java.util.Objects.hashCode(this.rotation);
        return result;
    }

    @Override
    public String toString() {
        return "LocatorClass[ignoreInheritedScale=" + this.ignoreInheritedScale + ", offset=" + this.offset
            + ", rotation=" + this.rotation + "]";
    }

    public static JsonDeserializer<LocatorClass> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            Boolean ignoreInheritedScale = JsonUtil.getOptionalBoolean(obj, "ignore_inherited_scale");
            double[] offset = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "offset", null));
            double[] rotation = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "rotation", null));

            return new LocatorClass(ignoreInheritedScale, offset, rotation);
        };
    }
}
