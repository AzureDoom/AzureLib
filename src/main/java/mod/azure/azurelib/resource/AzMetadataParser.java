package mod.azure.azurelib.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import mod.azure.azurelib.render.texture.AnimationMetadataSection;
import mod.azure.azurelib.render.texture.TextureMetadataSection;

/**
 * Parses the {@code texture} and {@code animation} sections of {@code .mcmeta} files, with the same defaults as
 * 1.7.10's metadata serializers.
 */
final class AzMetadataParser {

    private AzMetadataParser() {
        throw new UnsupportedOperationException();
    }

    static Object parse(String section, JsonObject json) {
        switch (section) {
            case "texture":
                return new TextureMetadataSection(bool(json, "blur", false), bool(json, "clamp", false));
            case "animation":
                return animation(json);
            default:
                return json;
        }
    }

    private static AnimationMetadataSection animation(JsonObject json) {
        List<int[]> frames = new ArrayList<>();
        int frameTime = integer(json, "frametime", 1);
        if (json.has("frames") && json.get("frames").isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray("frames")) {
                if (element.isJsonPrimitive()) {
                    frames.add(new int[] { element.getAsInt(), -1 });
                } else if (element.isJsonObject()) {
                    JsonObject frame = element.getAsJsonObject();
                    frames.add(new int[] { integer(frame, "index", 0), integer(frame, "time", -1) });
                }
            }
        }
        return new AnimationMetadataSection(
            frames,
            integer(json, "width", -1),
            integer(json, "height", -1),
            frameTime,
            bool(json, "interpolate", false)
        );
    }

    private static int integer(JsonObject json, String key, int fallback) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsInt() : fallback;
    }

    private static boolean bool(JsonObject json, String key, boolean fallback) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsBoolean() : fallback;
    }
}
