package mod.azure.azurelib.util.client;

import net.minecraft.client.model.ModelRenderer;

public final class ModelPartScaleCompat {

    public static float getXScale(ModelRenderer part) {
        return ModelPartScaleStore.getScale(part).x();
    }

    public static float getYScale(ModelRenderer part) {
        return ModelPartScaleStore.getScale(part).y();
    }

    public static float getZScale(ModelRenderer part) {
        return ModelPartScaleStore.getScale(part).z();
    }
}
