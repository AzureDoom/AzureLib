package mod.azure.azurelib.util.client;

import net.minecraft.client.model.geom.ModelPart;

import java.util.Map;
import java.util.WeakHashMap;

public final class ModelPartScaleStore {

    private static final Map<ModelPart, Scale> SCALES = new WeakHashMap<>();

    public static void setScale(ModelPart part, float x, float y, float z) {
        SCALES.put(part, new Scale(x, y, z));
    }

    public static Scale getScale(ModelPart part) {
        return SCALES.getOrDefault(part, Scale.ONE);
    }

    public record Scale(
        float x,
        float y,
        float z
    ) {

        public static final Scale ONE = new Scale(1.0F, 1.0F, 1.0F);
    }
}
