package mod.azure.azurelib.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;

public final class AzRenderContext {

    private AzRenderContext() {}

    public static boolean isGuiRender() {
        return RenderSystem.getProjectionType() == ProjectionType.ORTHOGRAPHIC;
    }
}
