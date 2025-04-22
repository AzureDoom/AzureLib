/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.cache.texture;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.platform.Services;

/**
 * Texture object type responsible for AzureLib's emissive render textures
 */
public class AutoGlowingTexture extends AzAbstractTexture {

    protected final ResourceLocation textureBase;

    protected final ResourceLocation glowLayer;

    public AutoGlowingTexture(ResourceLocation originalLocation, ResourceLocation location) {
        super(originalLocation);
        this.textureBase = originalLocation;
        this.glowLayer = location;
    }

    /**
     * Get the emissive resource equivalent of the input resource path.<br>
     * Additionally prepares the texture manager for the missing texture if the resource is not present
     *
     * @return The glowlayer resourcepath for the provided input path
     */
    protected static ResourceLocation getEmissiveResource(ResourceLocation baseResource) {
        ResourceLocation path = appendToPath(baseResource, APPENDIX);

        generateTexture(
            path,
            textureManager -> textureManager.register(path, new AutoGlowingTexture(baseResource, path))
        );

        return path;
    }

    /**
     * Return a cached instance of the RenderType for the given texture for GeoGlowingLayer rendering.
     *
     * @param texture The texture of the resource to apply a glow layer to
     */
    public static RenderType getRenderType(ResourceLocation texture) {
        return GLOWING_RENDER_TYPE.apply(getEmissiveResource(texture), false);
    }

    /**
     * Return a cached instance of the RenderType for the given texture for AutoGlowingGeoLayer rendering, while the
     * entity has an outline
     *
     * @param texture The texture of the resource to apply a glow layer to
     */
    public static RenderType getOutlineRenderType(ResourceLocation texture) {
        return GLOWING_RENDER_TYPE.apply(getEmissiveResource(texture), true);
    }

    /**
     * Generates the glow layer {@link NativeImage} and appropriately modifies the base texture for use in glow render
     * layers
     */
    @Nullable
    @Override
    protected RenderCall loadTexture(ResourceManager resourceManager, Minecraft mc) throws IOException {
        AbstractTexture originalTexture;

        try {
            originalTexture = mc.submit(() -> mc.getTextureManager().getTexture(this.textureBase)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to load original texture: " + this.textureBase, e);
        }

        Resource textureBaseResource = resourceManager.getResource(this.textureBase).get();
        NativeImage baseImage = originalTexture instanceof DynamicTexture dynamicTexture
            ? dynamicTexture.getPixels()
            : NativeImage.read(textureBaseResource.open());
        NativeImage glowImage = null;
        Optional<TextureMetadataSection> textureBaseMeta = textureBaseResource.metadata()
            .getSection(TextureMetadataSection.SERIALIZER);
        boolean blur = textureBaseMeta.isPresent() && textureBaseMeta.get().isBlur();
        boolean clamp = textureBaseMeta.isPresent() && textureBaseMeta.get().isClamp();

        try {
            Optional<Resource> glowLayerResource = resourceManager.getResource(this.glowLayer);
            GeoGlowingTextureMeta glowLayerMeta = null;

            if (glowLayerResource.isPresent()) {
                glowImage = NativeImage.read(glowLayerResource.get().open());
                glowLayerMeta = GeoGlowingTextureMeta.fromExistingImage(glowImage);
            } else {
                Optional<GeoGlowingTextureMeta> meta = textureBaseResource.metadata()
                    .getSection(GeoGlowingTextureMeta.DESERIALIZER);

                if (meta.isPresent()) {
                    glowLayerMeta = meta.get();
                    glowImage = new NativeImage(baseImage.getWidth(), baseImage.getHeight(), true);
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

        NativeImage mask = glowImage;

        if (mask == null) {
            String expectedGlowmask = this.textureBase.toString().replace(".png", "_glowmask.png");
            AzureLib.LOGGER.warn("Missing glowmask texture. Base texture: {}, Expected glowmask: {}", this.textureBase, expectedGlowmask);
            return null;
        }

        return () -> {
            uploadSimple(getId(), mask, blur, clamp);

            if (originalTexture instanceof DynamicTexture dynamicTexture) {
                dynamicTexture.upload();
            } else {
                uploadSimple(originalTexture.getId(), baseImage, blur, clamp);
            }
        };
    }
}
