package mod.azure.azurelib.render.texture;

import org.lwjgl.opengl.GL11;

/**
 * Owns a single GL texture name, allocated lazily. Mirrors 1.7.10's class of the same name.
 */
public abstract class AbstractTexture implements ITextureObject {

    protected int glTextureId = -1;

    @Override
    public int getGlTextureId() {
        if (this.glTextureId == -1) {
            this.glTextureId = GL11.glGenTextures();
        }
        return this.glTextureId;
    }

    public void deleteGlTexture() {
        if (this.glTextureId != -1) {
            GL11.glDeleteTextures(this.glTextureId);
            this.glTextureId = -1;
        }
    }
}
