package mod.azure.azurelib.render;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public final class AzRenderContext {

    private static final FloatBuffer PROJECTION =
        BufferUtils.createFloatBuffer(16);

    private AzRenderContext() {}

    public static boolean isGuiRender() {
        PROJECTION.clear();
        RenderSystem.getProjectionMatrix().store(PROJECTION);

        return Math.abs(PROJECTION.get(15) - 1.0F) < 1.0E-6F;
    }
}
