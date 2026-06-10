package mod.azure.azurelib.util.client;

import net.minecraft.client.model.geom.ModelPart;

public final class ModelPartScaleCompat {

    public static float getXScale(ModelPart part) {
        return ModelPartScaleStore.getScale(part).x();
    }

    public static float getYScale(ModelPart part) {
        return ModelPartScaleStore.getScale(part).y();
    }

    public static float getZScale(ModelPart part) {
        return ModelPartScaleStore.getScale(part).z();
    }
}
