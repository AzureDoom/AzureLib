package mod.azure.azurelib.render.texture;

import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class TextureUtil {

    private static ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 64 * 64).order(ByteOrder.nativeOrder());

    private TextureUtil() {
        throw new UnsupportedOperationException();
    }

    public static void allocateTexture(int textureId, int width, int height) {
        int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        setParameters(false, false);
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA,
                width,
                height,
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                (ByteBuffer) null
        );
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previous);
    }

    public static void uploadTexture(int textureId, int[] pixels, int width, int height) {
        int count = width * height;
        if (pixels.length < count) {
            throw new IllegalArgumentException(
                    "Expected at least " + count + " pixels for " + width + "x" + height + ", got " + pixels.length
            );
        }
        fillBuffer(pixels, count);

        int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        setParameters(false, false);
        GL11.glTexSubImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                0,
                0,
                width,
                height,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                buffer
        );
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previous);
    }

    public static void uploadTextureImageAllocate(int textureId, BufferedImage image, boolean blur, boolean clamp) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] argb = new int[width * height];
        image.getRGB(0, 0, width, height, argb, 0, width);
        fillBuffer(argb, argb.length);

        int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        setParameters(blur, clamp);
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA,
                width,
                height,
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                buffer
        );
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previous);
    }

    private static void fillBuffer(int[] argb, int count) {
        int bytes = count * 4;
        if (buffer.capacity() < bytes) {
            buffer = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
        }
        buffer.clear();
        for (int i = 0; i < count; i++) {
            int pixel = argb[i];
            buffer.put((byte) (pixel >> 16 & 0xFF));
            buffer.put((byte) (pixel >> 8 & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) (pixel >>> 24));
        }
        buffer.flip();
    }

    private static void setParameters(boolean blur, boolean clamp) {
        int filter = blur ? GL11.GL_LINEAR : GL11.GL_NEAREST;
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
        int wrap = clamp ? GL11.GL_CLAMP : GL11.GL_REPEAT;
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrap);
    }
}