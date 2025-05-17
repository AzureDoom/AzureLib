/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.texture;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.texture.resource.GeoGlowingTextureMeta;

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

        Resource textureBaseResource = resourceManager.getResource(this.textureBase);
        NativeImage baseImage = originalTexture instanceof DynamicTexture
            ? ((DynamicTexture) originalTexture).getPixels()
            : NativeImage.read(textureBaseResource.getInputStream());
        NativeImage glowImage = null;
        TextureMetadataSection textureBaseMeta = textureBaseResource.getMetadata(TextureMetadataSection.SERIALIZER);
        boolean blur = textureBaseMeta != null && textureBaseMeta.isBlur();
        boolean clamp = textureBaseMeta != null && textureBaseMeta.isClamp();

        try {
            Resource glowLayerResource = resourceManager.getResource(this.glowLayer);
            GeoGlowingTextureMeta glowLayerMeta = null;

            if (glowLayerResource != null) {
                glowImage = NativeImage.read(glowLayerResource.getInputStream());
                glowLayerMeta = GeoGlowingTextureMeta.fromExistingImage(glowImage);
            } else {
                GeoGlowingTextureMeta meta = textureBaseResource.getMetadata(GeoGlowingTextureMeta.DESERIALIZER);
                if (meta != null) {
                    glowLayerMeta = meta;
                    glowImage = new NativeImage(baseImage.getWidth(), baseImage.getHeight(), true);
                }
            }

            if (glowLayerMeta != null) {
                glowLayerMeta.createImageMask(baseImage, glowImage);

                if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
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
            AzureLib.LOGGER.warn(
                "Missing glowmask texture. Base texture: {}, Expected glowmask: {}",
                this.textureBase,
                expectedGlowmask
            );
            return null;
        }

        boolean animated = originalTexture instanceof AnimatableTexture && ((AnimatableTexture) originalTexture)
            .isAnimated();

        if (animated)
            ((AnimatableTexture) originalTexture).animationContents.animatedTexture.setGlowMaskTexture(
                this,
                baseImage,
                mask
            );

        return () -> {
            if (!animated)
                uploadSimple(getId(), mask, blur, clamp);

            if (originalTexture instanceof DynamicTexture) {
                ((DynamicTexture) originalTexture).upload();
            } else {
                uploadSimple(originalTexture.getId(), baseImage, blur, clamp);
            }
        };
    }
}
