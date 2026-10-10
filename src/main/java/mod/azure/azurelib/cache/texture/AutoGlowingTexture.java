package mod.azure.azurelib.cache.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.platform.Services;
import mod.azure.azurelib.render.vertex.RenderType;
import mod.azure.azurelib.resource.GeoGlowingTextureMeta;

/**
 * Texture object type responsible for AzureLib's emissive render textures
 */
public class AutoGlowingTexture extends GeoAbstractTexture {

    private static final String APPENDIX = "_glowmask";

    private static final Map<ResourceLocation, ResourceLocation> EMISSIVE_PATHS = new ConcurrentHashMap<>();

    protected final ResourceLocation textureBase;

    protected final ResourceLocation glowLayer;

    public AutoGlowingTexture(ResourceLocation originalLocation, ResourceLocation location) {
        this.textureBase = originalLocation;
        this.glowLayer = location;
    }

    /**
     * Get the emissive resource equivalent of the input resource path.<br>
     * Additionally prepares the texture manager for the missing texture if the resource is not present
     *
     * @return The glowlayer resourcepath for the provided input path
     */
    public static ResourceLocation getEmissiveResource(ResourceLocation baseResource) {
        ResourceLocation path = EMISSIVE_PATHS.computeIfAbsent(baseResource, base -> appendToPath(base, APPENDIX));

        generateTexture(
            path,
            textureManager -> textureManager.loadTexture(path, new AutoGlowingTexture(baseResource, path))
        );

        return path;
    }

    private static ITextureObject resolveBaseTexture(
        TextureManager textureManager,
        IResourceManager resourceManager,
        ResourceLocation location
    ) {
        AnimatableTexture.ensureLoaded(location);
        ITextureObject texture = textureManager.getTexture(location);

        if (texture == null) {
            textureManager.bindTexture(location);
            texture = textureManager.getTexture(location);
        }

        return texture;
    }

    @Nullable
    @Override
    protected Runnable loadTexture(IResourceManager resourceManager, Minecraft mc) throws IOException {
        ITextureObject originalTexture = resolveBaseTexture(mc.getTextureManager(), resourceManager, this.textureBase);
        BufferedImage baseImage;
        boolean blur = false;
        boolean clamp = false;

        try (
            IResource textureBaseResource = resourceManager.getResource(this.textureBase);
            InputStream stream = textureBaseResource.getInputStream()
        ) {
            baseImage = TextureUtil.readBufferedImage(stream);

            if (textureBaseResource.hasMetadata()) {
                TextureMetadataSection textureBaseMeta = textureBaseResource.getMetadata("texture");

                if (textureBaseMeta != null) {
                    blur = textureBaseMeta.getTextureBlur();
                    clamp = textureBaseMeta.getTextureClamp();
                }
            }
        }

        BufferedImage glowImage = null;

        try {
            GeoGlowingTextureMeta glowLayerMeta = null;

            if (resourceExists(resourceManager, this.glowLayer)) {
                try (IResource glowLayerResource = resourceManager.getResource(this.glowLayer)) {
                    glowImage = TextureUtil.readBufferedImage(glowLayerResource.getInputStream());
                }

                if (baseImage.getWidth() != glowImage.getWidth() || baseImage.getHeight() != glowImage.getHeight()) {
                    AzureLib.LOGGER.error(
                        "Glowmask size mismatch with base texture. Base size: {}x{}, Glowmask size: {}x{}, Location: {}",
                        baseImage.getWidth(),
                        baseImage.getHeight(),
                        glowImage.getWidth(),
                        glowImage.getHeight(),
                        this.glowLayer
                    );
                    AzGlowCoverage.unregister(this.glowLayer);
                    return null;
                }

                glowLayerMeta = GeoGlowingTextureMeta.fromExistingImage(glowImage);
            } else {
                GeoGlowingTextureMeta meta = GeoGlowingTextureMeta.fromMcmeta(resourceManager, this.textureBase);

                if (meta != null) {
                    glowLayerMeta = meta;
                    glowImage = new BufferedImage(
                        baseImage.getWidth(),
                        baseImage.getHeight(),
                        BufferedImage.TYPE_INT_ARGB
                    );
                }
            }

            if (glowLayerMeta != null) {
                glowLayerMeta.createImageMask(baseImage, glowImage);

                if (Services.PLATFORM.isDevelopmentEnvironment()) {
                    printDebugImageToDisk(this.textureBase, baseImage);
                    printDebugImageToDisk(this.glowLayer, glowImage);
                }
            }
        } catch (IOException e) {
            AzureLib.LOGGER.warn("Resource failed to open for glowlayer meta: {}", this.glowLayer, e);
        }

        final BufferedImage mask = glowImage;

        if (mask == null) {
            String expectedGlowmask = this.textureBase.toString().replace(".png", "_glowmask.png");
            AzureLib.LOGGER.warn(
                "Missing glowmask texture. Base texture: {}, Expected glowmask: {}",
                this.textureBase,
                expectedGlowmask
            );
            AzGlowCoverage.unregister(this.glowLayer);
            return null;
        }

        final boolean animated = originalTexture instanceof AnimatableTexture && ((AnimatableTexture) originalTexture)
            .isAnimated();

        if (animated) {
            AnimatableTexture animatableTexture = (AnimatableTexture) originalTexture;
            AzGlowCoverage.registerFrames(
                this.glowLayer,
                mask,
                animatableTexture.frameWidth(),
                animatableTexture.frameHeight()
            );
            animatableTexture.setGlowMaskTexture(this, baseImage, mask);
        } else {
            AzGlowCoverage.register(this.glowLayer, mask);
        }

        final boolean finalBlur = blur;
        final boolean finalClamp = clamp;
        final BufferedImage finalBase = baseImage;

        return () -> {
            if (!animated) {
                uploadSimple(getGlTextureId(), mask, finalBlur, finalClamp);
                // Re-upload the base texture with the glowing pixels removed, so they don't render twice.
                if (originalTexture != null && !(originalTexture instanceof DynamicTexture))
                    uploadSimple(originalTexture.getGlTextureId(), finalBase, finalBlur, finalClamp);
            } else {
                // Allocate the glowmask texture; the animated texture uploads the individual frames into it.
                uploadSimple(getGlTextureId(), mask, finalBlur, finalClamp);
                ((AnimatableTexture) originalTexture).forceFrameUpload();
            }
        };
    }

    private static boolean resourceExists(IResourceManager resourceManager, ResourceLocation location) {
        try (IResource ignored = resourceManager.getResource(location)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Return a cached instance of the RenderType for the given texture for AzAutoGlowingLayer rendering.
     *
     * @param texture The texture of the resource to apply a glow layer to
     */
    public static RenderType getRenderType(ResourceLocation texture) {
        return RenderType.emissive(getEmissiveResource(texture), false);
    }

    /**
     * Return a cached instance of the RenderType for the given texture for AzAutoGlowingLayer rendering, while the
     * entity has an outline
     *
     * @param texture The texture of the resource to apply a glow layer to
     */
    public static RenderType getOutlineRenderType(ResourceLocation texture) {
        return RenderType.emissive(getEmissiveResource(texture), true);
    }
}
