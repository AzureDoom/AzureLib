package mod.azure.azurelib.render.vertex;

import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.FloatBuffer;

/**
 * A thin stand-in for 1.7.10's {@code GlStateManager}. Minecraft 1.7.10 has no GL state cache, so these methods call
 * OpenGL directly; they exist so AzureLib's renderer code reads the same on every branch.
 */
public final class GlStateManager {

    private GlStateManager() {
        throw new UnsupportedOperationException();
    }

    public static void pushMatrix() {
        GL11.glPushMatrix();
    }

    public static void popMatrix() {
        GL11.glPopMatrix();
    }

    public static void translate(float x, float y, float z) {
        GL11.glTranslatef(x, y, z);
    }

    public static void translate(double x, double y, double z) {
        GL11.glTranslated(x, y, z);
    }

    public static void scale(float x, float y, float z) {
        GL11.glScalef(x, y, z);
    }

    public static void rotate(float angle, float x, float y, float z) {
        GL11.glRotatef(angle, x, y, z);
    }

    public static void multMatrix(FloatBuffer matrix) {
        GL11.glMultMatrix(matrix);
    }

    public static void matrixMode(int mode) {
        GL11.glMatrixMode(mode);
    }

    public static void loadIdentity() {
        GL11.glLoadIdentity();
    }

    public static void enableTexture2D() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static void disableTexture2D() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    public static void enableCull() {
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public static void disableCull() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    public static void enableBlend() {
        GL11.glEnable(GL11.GL_BLEND);
    }

    public static void disableBlend() {
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void blendFunc(SourceFactor src, DestFactor dst) {
        GL11.glBlendFunc(src.factor, dst.factor);
    }

    public static void tryBlendFuncSeparate(
        SourceFactor srcColor,
        DestFactor dstColor,
        SourceFactor srcAlpha,
        DestFactor dstAlpha
    ) {
        OpenGlHelper.glBlendFunc(srcColor.factor, dstColor.factor, srcAlpha.factor, dstAlpha.factor);
    }

    public static void enableAlpha() {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    public static void disableAlpha() {
        GL11.glDisable(GL11.GL_ALPHA_TEST);
    }

    public static void alphaFunc(int func, float ref) {
        GL11.glAlphaFunc(func, ref);
    }

    public static void enableLighting() {
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    public static void disableLighting() {
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    public static void enableRescaleNormal() {
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }

    public static void disableRescaleNormal() {
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    public static void depthMask(boolean flag) {
        GL11.glDepthMask(flag);
    }

    public static void depthFunc(int func) {
        GL11.glDepthFunc(func);
    }

    public static void enableDepth() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public static void disableDepth() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    public static void color(float red, float green, float blue, float alpha) {
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void bindTexture(int texture) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    }

    public static void enableColorMaterial() {
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
    }

    public static void disableColorMaterial() {
        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
    }

    public enum SourceFactor {

        ONE(GL11.GL_ONE),
        ZERO(GL11.GL_ZERO),
        SRC_ALPHA(GL11.GL_SRC_ALPHA),
        ONE_MINUS_SRC_ALPHA(GL11.GL_ONE_MINUS_SRC_ALPHA),
        SRC_COLOR(GL11.GL_SRC_COLOR),
        DST_COLOR(GL11.GL_DST_COLOR);

        public final int factor;

        SourceFactor(int factor) {
            this.factor = factor;
        }
    }

    public enum DestFactor {

        ONE(GL11.GL_ONE),
        ZERO(GL11.GL_ZERO),
        SRC_ALPHA(GL11.GL_SRC_ALPHA),
        ONE_MINUS_SRC_ALPHA(GL11.GL_ONE_MINUS_SRC_ALPHA),
        SRC_COLOR(GL11.GL_SRC_COLOR),
        ONE_MINUS_SRC_COLOR(GL11.GL_ONE_MINUS_SRC_COLOR);

        public final int factor;

        DestFactor(int factor) {
            this.factor = factor;
        }
    }
}
