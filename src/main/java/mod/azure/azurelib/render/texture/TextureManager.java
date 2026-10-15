package mod.azure.azurelib.render.texture;

import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.resource.AzResourceManager;
import mod.azure.azurelib.util.ResourceLocation;

/**
 * AzureLib's own texture registry. Beta's {@code TextureManager} only knows string paths and cannot host custom texture
 * objects (animated or generated glowmask textures), so AzureLib manages the GL textures it draws with itself. Mirrors
 * the parts of 1.7.10's {@code TextureManager} that AzureLib uses.
 * <p>
 * Only touch this on the client (render) thread.
 */
public final class TextureManager {

    private static final TextureManager INSTANCE = new TextureManager();

    private final Map<ResourceLocation, ITextureObject> textures = new HashMap<>();

    private final ITextureObject missing = new MissingTexture();

    private TextureManager() {}

    public static TextureManager getInstance() {
        return INSTANCE;
    }

    public void bindTexture(ResourceLocation location) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, getOrLoad(location).getGlTextureId());
    }

    public ITextureObject getOrLoad(ResourceLocation location) {
        ITextureObject texture = this.textures.get(location);
        if (texture == null) {
            texture = new SimpleTexture(location);
            if (!loadTexture(location, texture)) {
                texture = this.textures.get(location);
            }
        }
        return texture;
    }

    @Nullable
    public ITextureObject getTexture(ResourceLocation location) {
        return this.textures.get(location);
    }

    public boolean loadTexture(ResourceLocation location, ITextureObject texture) {
        try {
            texture.loadTexture(AzResourceManager.getInstance());
            this.textures.put(location, texture);
            return true;
        } catch (Exception e) {
            AzureLib.LOGGER.warn("Failed to load texture {}: {}", location, e.toString());
            this.textures.put(location, this.missing);
            return false;
        }
    }

    public void deleteTexture(ResourceLocation location) {
        ITextureObject texture = this.textures.remove(location);
        if (texture instanceof AbstractTexture && texture != this.missing) {
            ((AbstractTexture) texture).deleteGlTexture();
        }
    }

    public void clear() {
        for (ITextureObject texture : this.textures.values()) {
            if (texture instanceof AbstractTexture && texture != this.missing) {
                ((AbstractTexture) texture).deleteGlTexture();
            }
        }
        this.textures.clear();
    }

    private static final class MissingTexture extends AbstractTexture {

        private boolean uploaded;

        @Override
        public int getGlTextureId() {
            int id = super.getGlTextureId();
            if (!this.uploaded) {
                this.uploaded = true;
                BufferedImage image = new BufferedImage(
                    16,
                    16,
                    BufferedImage.TYPE_INT_ARGB
                );
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        image.setRGB(x, y, (x < 8) == (y < 8) ? 0xFFF800F8 : 0xFF000000);
                    }
                }
                TextureUtil.uploadTextureImageAllocate(id, image, false, false);
            }
            return id;
        }

        @Override
        public void loadTexture(AzResourceManager resourceManager) {}
    }
}
