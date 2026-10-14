package mod.azure.azurelib.cache.texture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import mod.azure.azurelib.platform.Services;
import mod.azure.azurelib.render.texture.AbstractTexture;
import mod.azure.azurelib.render.texture.ITextureObject;
import mod.azure.azurelib.render.texture.TextureManager;
import mod.azure.azurelib.render.texture.TextureUtil;
import mod.azure.azurelib.resource.AzResourceManager;
import mod.azure.azurelib.util.ResourceLocation;

/**
 * Abstract texture wrapper for AzureLib textures. On Beta all texture loading already happens on the client thread, so
 * the upload task returned by {@link #prepareTexture(AzResourceManager)} is executed immediately.
 */
public abstract class GeoAbstractTexture extends AbstractTexture {

    /**
     * Generates the texture instance for the given path with the given appendix if it hasn't already been generated
     */
    protected static void generateTexture(
        ResourceLocation texturePath,
        Consumer<TextureManager> textureManagerConsumer
    ) {
        TextureManager textureManager = TextureManager.getInstance();
        ITextureObject existing = textureManager.getTexture(texturePath);

        if (!(existing instanceof GeoAbstractTexture))
            textureManagerConsumer.accept(textureManager);
    }

    @Override
    public final void loadTexture(AzResourceManager resourceManager) throws IOException {
        Runnable uploadTask = prepareTexture(resourceManager);

        if (uploadTask != null)
            uploadTask.run();
    }

    /**
     * Debugging function to write out the generated glowmap image to disk
     */
    protected void printDebugImageToDisk(ResourceLocation id, BufferedImage newImage) {
        try {
            File file = new File(Services.PLATFORM.getGameDir().toFile(), "GeoTexture Debug Printouts");

            if (!file.exists()) {
                file.mkdirs();
            } else if (!file.isDirectory()) {
                file.delete();
                file.mkdirs();
            }

            file = new File(file, id.getResourcePath().replace('/', '.'));

            if (!file.exists())
                file.createNewFile();

            ImageIO.write(newImage, "png", file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Called at {@link AbstractTexture#loadTexture} time to prepare the texture. Return a task that performs the GL
     * upload, or {@code null} if there's nothing to upload.
     */
    @Nullable
    protected abstract Runnable prepareTexture(AzResourceManager resourceManager) throws IOException;

    /**
     * No-frills helper method for uploading {@link BufferedImage images} into memory for use
     */
    public static void uploadSimple(int texture, BufferedImage image, boolean blur, boolean clamp) {
        TextureUtil.uploadTextureImageAllocate(texture, image, blur, clamp);
    }

    public static ResourceLocation appendToPath(ResourceLocation location, String suffix) {
        String path = location.getResourcePath();
        int i = path.lastIndexOf('.');

        return new ResourceLocation(location.getResourceDomain(), path.substring(0, i) + suffix + path.substring(i));
    }
}
