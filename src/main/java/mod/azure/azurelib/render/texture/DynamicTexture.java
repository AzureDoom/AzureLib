package mod.azure.azurelib.render.texture;

import java.awt.image.BufferedImage;

import mod.azure.azurelib.resource.AzResourceManager;

/**
 * A texture whose pixels are supplied in code rather than loaded from a file. Mirrors 1.7.10's class of the same name.
 */
public class DynamicTexture extends AbstractTexture {

    private final BufferedImage image;

    public DynamicTexture(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void loadTexture(AzResourceManager resourceManager) {
        TextureUtil.uploadTextureImageAllocate(getGlTextureId(), this.image, false, false);
    }
}
