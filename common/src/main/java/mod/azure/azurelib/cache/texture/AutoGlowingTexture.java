/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import mod.azure.azurelib.AzureLib;

/**
 * Texture object type responsible for AzureLib's emissive render textures
 */
public class AutoGlowingTexture extends AzAbstractTexture {

    protected final Identifier textureBase;

    protected final Identifier glowLayer;

    public AutoGlowingTexture(Identifier originalLocation, Identifier location) {
        super(location);
        this.textureBase = originalLocation;
        this.glowLayer = location;
    }

    @Override
    public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
        AbstractTexture originalTexture = Minecraft.getInstance().getTextureManager().getTexture(this.textureBase);

        Resource textureBaseResource = resourceManager.getResourceOrThrow(this.textureBase);
        NativeImage baseImage = originalTexture instanceof DynamicTexture dynamicTexture
            ? dynamicTexture.getPixels()
            : readImage(textureBaseResource);
        NativeImage glowImage = null;
        TextureMetadataSection textureBaseMeta = textureBaseResource.metadata()
            .getSection(TextureMetadataSection.TYPE)
            .orElse(null);

        try {
            Optional<Resource> glowLayerResource = resourceManager.getResource(this.glowLayer);
            AzGlowingTextureMeta glowLayerMeta = null;

            if (glowLayerResource.isPresent()) {
                glowImage = readImage(glowLayerResource.get());

                if (baseImage.getWidth() != glowImage.getWidth() || baseImage.getHeight() != glowImage.getHeight()) {
                    AzureLib.LOGGER.error(
                        "Glowmask size mismatch with base texture. Base size: {}x{}, Glowmask size: {}x{}, Location: {}",
                        baseImage.getWidth(),
                        baseImage.getHeight(),
                        glowImage.getWidth(),
                        glowImage.getHeight(),
                        this.glowLayer
                    );
                    glowImage.close();
                    closeIfOwned(baseImage, originalTexture);

                    return emptyContents();
                }

                glowLayerMeta = AzGlowingTextureMeta.fromExistingImage(glowImage);
            } else {
                Optional<AzGlowingTextureMeta> meta = textureBaseResource.metadata()
                    .getSection(AzGlowingTextureMeta.TYPE);

                if (meta.isPresent()) {
                    glowLayerMeta = meta.get();
                    glowImage = new NativeImage(baseImage.getWidth(), baseImage.getHeight(), true);
                }
            }

            if (glowLayerMeta != null) {
                glowLayerMeta.createImageMask(baseImage, glowImage);
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
            closeIfOwned(baseImage, originalTexture);

            return emptyContents();
        }

        if (originalTexture instanceof AnimatableTexture animatableTexture && animatableTexture.isAnimated()) {
            return new TextureContents(
                animatableTexture.animationContents.animatedTexture.setGlowMaskTexture(this, baseImage, mask),
                textureBaseMeta
            );
        }

        onRenderThread(() -> {
            if (originalTexture instanceof DynamicTexture dynamicTexture) {
                dynamicTexture.upload();
            } else {
                uploadSimple(originalTexture.getTexture(), baseImage);
                baseImage.close();
            }
        });

        return new TextureContents(mask, textureBaseMeta);
    }

    private static TextureContents emptyContents() {
        return new TextureContents(new NativeImage(1, 1, true), null);
    }

    private static NativeImage readImage(Resource resource) throws IOException {
        try (InputStream input = resource.open()) {
            return NativeImage.read(input);
        }
    }

    private static void closeIfOwned(NativeImage image, AbstractTexture originalTexture) {
        if (!(originalTexture instanceof DynamicTexture)) {
            image.close();
        }
    }
}
