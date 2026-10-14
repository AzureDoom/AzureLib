package mod.azure.azurelib.render.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import mod.azure.azurelib.resource.AzResource;
import mod.azure.azurelib.resource.AzResourceManager;
import mod.azure.azurelib.util.ResourceLocation;

/**
 * A texture loaded from a single PNG, honouring the {@code texture} section ({@code blur}/{@code clamp}) of an optional
 * {@code .mcmeta}. Mirrors 1.7.10's class of the same name.
 */
public class SimpleTexture extends AbstractTexture {

    protected final ResourceLocation textureLocation;

    public SimpleTexture(ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
    }

    @Override
    public void loadTexture(AzResourceManager resourceManager) throws IOException {
        this.deleteGlTexture();
        AzResource resource = resourceManager.getResource(this.textureLocation);
        BufferedImage image;
        boolean blur = false;
        boolean clamp = false;
        try (InputStream stream = resource.getInputStream()) {
            image = ImageIO.read(stream);
        }
        if (image == null) {
            throw new IOException("Not a readable image: " + this.textureLocation);
        }
        if (resource.hasMetadata()) {
            TextureMetadataSection meta = (TextureMetadataSection) resource.getMetadata("texture");
            if (meta != null) {
                blur = meta.getTextureBlur();
                clamp = meta.getTextureClamp();
            }
        }
        TextureUtil.uploadTextureImageAllocate(getGlTextureId(), image, blur, clamp);
    }
}
