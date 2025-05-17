/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.JSONUtils;

import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for locator class information, only used in deserialization at startup
 */
public class LocatorClass {

    private final Boolean ignoreInheritedScale;

    private final double[] offset;

    private final double[] rotation;

    public LocatorClass(Boolean ignoreInheritedScale, double[] offset, double[] rotation) {
        this.ignoreInheritedScale = ignoreInheritedScale;
        this.offset = offset;
        this.rotation = rotation;
    }

    public Boolean ignoreInheritedScale() {
        return ignoreInheritedScale;
    }

    public double[] offset() {
        return offset;
    }

    public double[] rotation() {
        return rotation;
    }

    public static JsonDeserializer<LocatorClass> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            Boolean ignoreInheritedScale = JsonUtil.getOptionalBoolean(obj, "ignore_inherited_scale");
            double[] offset = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getJsonArray(obj, "offset", null));
            double[] rotation = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getJsonArray(obj, "rotation", null));

            return new LocatorClass(ignoreInheritedScale, offset, rotation);
        };
    }
}
