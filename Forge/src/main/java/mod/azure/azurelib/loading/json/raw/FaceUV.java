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
import net.minecraft.util.math.MathHelper;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for face UV information, only used in deserialization at startup
 */
public class FaceUV {

    private final String materialInstance;

    private final double[] uv;

    private final double[] uvSize;

    private final Rotation uvRotation;

    public FaceUV(String materialInstance, double[] uv, double[] uvSize, Rotation uvRotation) {
        this.materialInstance = materialInstance;
        this.uv = uv;
        this.uvSize = uvSize;
        this.uvRotation = uvRotation;
    }

    public String getMaterialInstance() {
        return materialInstance;
    }

    public double[] getUv() {
        return uv;
    }

    public double[] getUvSize() {
        return uvSize;
    }

    public Rotation getUvRotation() {
        return uvRotation;
    }

    public static JsonDeserializer<FaceUV> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            String materialInstance = JSONUtils.getAsString(obj, "material_instance", null);
            double[] uv = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "uv", null));
            double[] uvSize = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "uv_size", null));
            Rotation uvRotation = Rotation.fromValue(JSONUtils.getAsInt(obj, "uv_rotation", 0));

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
                return fromValue(MathHelper.floor(Math.abs(value) / 90f) * 90);
            }
        }

        public float[] rotateUvs(float u, float v, float uWidth, float vHeight) {
            switch (this) {
                case NONE:
                    return new float[] { u, v, uWidth, v, uWidth, vHeight, u, vHeight };
                case CLOCKWISE_90:
                    return new float[] { uWidth, v, uWidth, vHeight, u, vHeight, u, v };
                case CLOCKWISE_180:
                    return new float[] { uWidth, vHeight, u, vHeight, u, v, uWidth, v };
                case CLOCKWISE_270:
                    return new float[] { u, vHeight, u, v, uWidth, v, uWidth, vHeight };
                default:
                    throw new IllegalStateException("Unexpected value: " + this);
            }
        }
    }
}
