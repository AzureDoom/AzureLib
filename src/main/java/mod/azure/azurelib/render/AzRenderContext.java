package mod.azure.azurelib.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public final class AzRenderContext {

    private static final FloatBuffer PROJECTION = BufferUtils.createFloatBuffer(16);

    private AzRenderContext() {}

    public static boolean isGuiRender() {
        PROJECTION.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION);

        return Math.abs(PROJECTION.get(15) - 1.0F) < 1.0E-6F;
    }
}
