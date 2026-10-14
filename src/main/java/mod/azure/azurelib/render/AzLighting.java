package mod.azure.azurelib.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

import mod.azure.azurelib.render.vertex.GlStateManager;

/**
 * Fixed-function item lighting, equivalent to vanilla's {@code RenderHelper} (Beta and 1.7.10 share the same two light
 * setup). Re-implemented with raw GL calls so AzureLib does not depend on that class's mapped names.
 */
public final class AzLighting {

    private static final FloatBuffer BUFFER = BufferUtils.createFloatBuffer(16);

    private AzLighting() {
        throw new UnsupportedOperationException();
    }

    public static void enableStandardItemLighting() {
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_LIGHT0);
        GL11.glEnable(GL11.GL_LIGHT1);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
        float diffuse = 0.6F;
        float ambient = 0.4F;
        setLight(GL11.GL_LIGHT0, 0.2F, 1.0F, -0.7F, diffuse);
        setLight(GL11.GL_LIGHT1, -0.2F, 1.0F, 0.7F, diffuse);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, buffer(ambient, ambient, ambient, 1.0F));
    }

    public static void enableGUIStandardItemLighting() {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(165.0F, 1.0F, 0.0F, 0.0F);
        enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    public static void disableStandardItemLighting() {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_LIGHT0);
        GL11.glDisable(GL11.GL_LIGHT1);
        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
    }

    private static void setLight(int light, float x, float y, float z, float diffuse) {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        GL11.glLight(light, GL11.GL_POSITION, buffer(x / length, y / length, z / length, 0.0F));
        GL11.glLight(light, GL11.GL_DIFFUSE, buffer(diffuse, diffuse, diffuse, 1.0F));
        GL11.glLight(light, GL11.GL_AMBIENT, buffer(0.0F, 0.0F, 0.0F, 1.0F));
        GL11.glLight(light, GL11.GL_SPECULAR, buffer(0.0F, 0.0F, 0.0F, 1.0F));
    }

    private static FloatBuffer buffer(float a, float b, float c, float d) {
        BUFFER.clear();
        BUFFER.put(a).put(b).put(c).put(d);
        BUFFER.flip();
        return BUFFER;
    }
}
