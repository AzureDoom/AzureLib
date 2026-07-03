/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.platform.Services;

/** Texture object type responsible for AzureLib's emissive render textures. */
public class AutoGlowingTexture extends AzAbstractTexture {

    protected final Identifier textureBase;

    protected final Identifier glowLayer;

    protected @Nullable NativeImage baseImage;

    protected @Nullable NativeImage glowImage;

    protected @Nullable TextureMetadataSection textureMeta;

    protected @Nullable AbstractTexture originalTexture;

    protected boolean animated;

    public AutoGlowingTexture(Identifier originalLocation, Identifier location) {
        super(location);
        this.textureBase = originalLocation;
        this.glowLayer = location;
    }

    @Override
    public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
        this.originalTexture = Minecraft.getInstance().getTextureManager().getTexture(this.textureBase);

        Resource textureBaseResource = resourceManager.getResourceOrThrow(this.textureBase);
        this.baseImage = this.originalTexture instanceof DynamicTexture dynamicTexture
            ? dynamicTexture.getPixels()
            : NativeImage.read(textureBaseResource.open());
        this.textureMeta = textureBaseResource.metadata().getSection(TextureMetadataSection.TYPE).orElse(null);

        try {
            Optional<Resource> glowLayerResource = resourceManager.getResource(this.glowLayer);
            GeoGlowingTextureMeta glowLayerMeta = null;

            if (glowLayerResource.isPresent()) {
                this.glowImage = NativeImage.read(glowLayerResource.get().open());

                if (
                    this.baseImage.getWidth() != this.glowImage.getWidth() || this.baseImage
                        .getHeight() != this.glowImage.getHeight()
                ) {
                    AzureLib.LOGGER.error(
                        "Glowmask size mismatch with base texture. Base size: {}x{}, Glowmask size: {}x{}, Location: {}",
                        this.baseImage.getWidth(),
                        this.baseImage.getHeight(),
                        this.glowImage.getWidth(),
                        this.glowImage.getHeight(),
                        this.glowLayer
                    );
                    this.glowImage.close();
                    this.glowImage = null;
                } else {
                    glowLayerMeta = GeoGlowingTextureMeta.fromExistingImage(this.glowImage);
                }
            } else {
                Optional<GeoGlowingTextureMeta> meta = textureBaseResource.metadata()
                    .getSection(GeoGlowingTextureMeta.TYPE);

                if (meta.isPresent()) {
                    glowLayerMeta = meta.get();
                    this.glowImage = new NativeImage(this.baseImage.getWidth(), this.baseImage.getHeight(), true);
                }
            }

            if (glowLayerMeta != null && this.glowImage != null) {
                glowLayerMeta.createImageMask(this.baseImage, this.glowImage);

                if (Services.PLATFORM.isDevelopmentEnvironment()) {
                    printDebugImageToDisk(this.textureBase, this.baseImage);
                    printDebugImageToDisk(this.glowLayer, this.glowImage);
                }
            }
        } catch (IOException e) {
            AzureLib.LOGGER.warn("Resource failed to open for glowlayer meta: {}", this.glowLayer, e);
        }

        if (this.glowImage == null) {
            String expectedGlowmask = this.textureBase.toString().replace(".png", "_glowmask.png");
            AzureLib.LOGGER.warn(
                "Missing glowmask texture. Base texture: {}, Expected glowmask: {}",
                this.textureBase,
                expectedGlowmask
            );
            this.glowImage = new NativeImage(1, 1, true);
            this.glowImage.setPixel(0, 0, 0);
        }

        this.animated = this.originalTexture instanceof AnimatableTexture animatableTexture && animatableTexture
            .isAnimated();

        return new TextureContents(this.glowImage, this.textureMeta);
    }

    @Override
    public void apply(@NonNull TextureContents textureContents) {
        if (this.glowImage == null)
            return;

        AddressMode address = textureContents.clamp() ? AddressMode.CLAMP_TO_EDGE : AddressMode.REPEAT;
        FilterMode filter = textureContents.blur() ? FilterMode.LINEAR : FilterMode.NEAREST;
        this.sampler = RenderSystem.getSamplerCache().getSampler(address, address, filter, filter, false);

        uploadSimple(this.glowImage);

        if (this.originalTexture instanceof AnimatableTexture animatableTexture && this.baseImage != null) {
            animatableTexture.animationContents.setGlowMaskTexture(this, this.baseImage, this.glowImage);
        } else if (this.originalTexture != null && this.baseImage != null) {
            this.originalTexture.doLoad(this.baseImage);
        }
    }

    @Override
    public void close() {
        if (this.baseImage != null && !(this.originalTexture instanceof DynamicTexture))
            this.baseImage.close();

        if (this.glowImage != null && !this.animated)
            this.glowImage.close();

        super.close();
    }

    protected void printDebugImageToDisk(Identifier id, NativeImage newImage) {
        try {
            File file = new File(Services.PLATFORM.getGameDir().toFile(), "GeoTexture Debug Printouts");

            if (!file.exists()) {
                file.mkdirs();
            } else if (!file.isDirectory()) {
                file.delete();
                file.mkdirs();
            }

            file = new File(file, id.getPath().replace('/', '.'));

            if (!file.exists())
                file.createNewFile();

            newImage.writeToFile(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
