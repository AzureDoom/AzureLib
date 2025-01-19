/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.util.JsonUtil;

/**
 * Container class for face UV information, only used in deserialization at startup
 */
public record FaceUV(
    @Nullable String materialInstance,
    double[] uv,
    double[] uvSize,
    Rotation uvRotation
) {

    public static JsonDeserializer<FaceUV> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            String materialInstance = GsonHelper.getAsString(obj, "material_instance", null);
            double[] uv = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "uv", null));
            double[] uvSize = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "uv_size", null));
            Rotation uvRotation = Rotation.fromValue(GsonHelper.getAsInt(obj, "uv_rotation", 0));

            return new FaceUV(materialInstance, uv, uvSize, uvRotation);
        };
    }

    public enum Rotation {

        NONE,
        CLOCKWISE_90,
        CLOCKWISE_180,
        CLOCKWISE_270;

        public static Rotation fromValue(int value) throws JsonParseException {
            try {
                return Rotation.values()[(value % 360) / 90];
            } catch (Exception e) {
                AzureLib.LOGGER.error("Invalid Face UV rotation: " + value);

                return fromValue(Mth.floor(Math.abs(value) / 90f) * 90);
            }
        }

        public float[] rotateUvs(float u, float v, float uWidth, float vHeight) {
            return switch (this) {
                case NONE -> new float[] { u, v, uWidth, v, uWidth, vHeight, u, vHeight };
                case CLOCKWISE_90 -> new float[] { uWidth, v, uWidth, vHeight, u, vHeight, u, v };
                case CLOCKWISE_180 -> new float[] { uWidth, vHeight, u, vHeight, u, v, uWidth, v };
                case CLOCKWISE_270 -> new float[] { u, vHeight, u, v, uWidth, v, uWidth, vHeight };
            };
        }
    }
}
