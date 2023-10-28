package mod.azure.azurelib.loading.json.raw;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.JsonUtil;
import net.minecraft.util.GsonHelper;

/**
 * Container class for locator class information, only used in deserialization at startup
 */
public record LocatorClass(@Nullable Boolean ignoreInheritedScale, double[] offset, double[] rotation) {
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
