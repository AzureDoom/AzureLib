package mod.azure.azurelib.loading.json.raw;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for locator value information, only used in deserialization at startup
 */
public record LocatorValue(@Nullable LocatorClass locatorClass, double[] values) {
	public static JsonDeserializer<LocatorValue> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			if (json.isJsonArray()) {
				return new LocatorValue(null, JsonUtil.jsonArrayToDoubleArray(json.getAsJsonArray()));
			}
			else if (json.isJsonObject()) {
				return new LocatorValue(context.deserialize(json.getAsJsonObject(), mod.azure.azurelib.loading.json.raw.LocatorClass.class), new double[0]);
			}
			else {
				throw new JsonParseException("Invalid format for LocatorValue in json");
			}
		};
	}
}
