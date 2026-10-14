package mod.azure.azurelib.render.texture;

import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Uploads {@link BufferedImage}s to GL textures, the way 1.7.10's {@code TextureUtil#uploadTextureImageAllocate} does.
 * Uses plain RGBA/unsigned-byte uploads, which every GL 1.1 driver that runs Beta supports.
 */
public final class TextureUtil {

    private static ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 64 * 64).order(ByteOrder.nativeOrder());

    private TextureUtil() {
        throw new UnsupportedOperationException();
    }

    public static void uploadTextureImageAllocate(int textureId, BufferedImage image, boolean blur, boolean clamp) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] argb = new int[width * height];
        image.getRGB(0, 0, width, height, argb, 0, width);

        int bytes = argb.length * 4;
        if (buffer.capacity() < bytes) {
            buffer = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
        }
        buffer.clear();
        for (int pixel : argb) {
            buffer.put((byte) (pixel >> 16 & 0xFF));
            buffer.put((byte) (pixel >> 8 & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) (pixel >>> 24));
        }
        buffer.flip();

        int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        int filter = blur ? GL11.GL_LINEAR : GL11.GL_NEAREST;
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
        int wrap = clamp ? GL11.GL_CLAMP : GL11.GL_REPEAT;
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrap);
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
}
