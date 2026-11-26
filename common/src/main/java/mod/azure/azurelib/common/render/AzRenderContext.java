package mod.azure.azurelib.common.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;

public final class AzRenderContext {

    private AzRenderContext() {}

    public static boolean isGuiRender() {
        return RenderSystem.getVertexSorting() == VertexSorting.ORTHOGRAPHIC_Z;
    }
}
