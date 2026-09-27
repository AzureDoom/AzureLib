package mod.azure.azurelib.cache.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import mod.azure.azurelib.AzureLib;

/**
 * Texture object type responsible for AzureLib's emissive render textures
 */
public class AutoGlowingTexture extends AzAbstractTexture {

    protected final Identifier textureBase;

    protected final Identifier glowLayer;

    private NativeImage pendingBaseImage;

    private AbstractTexture pendingBaseTexture;

    public AutoGlowingTexture(Identifier originalLocation, Identifier location) {
        super(location);
        this.textureBase = originalLocation;
        this.glowLayer = location;
    }

    @Override
    public @NonNull TextureContents loadContents(@NonNull ResourceManager resourceManager) throws IOException {
        var mc = Minecraft.getInstance();
        Supplier<AbstractTexture> resolveBase = () -> {
            var textureManager = mc.getTextureManager();
            var texture = textureManager.getTexture(this.textureBase);

            if (
                !(texture instanceof AnimatableTexture)
                    && resourceManager.getResource(this.textureBase)
                        .flatMap(resource -> {
                            try {
                                return resource.metadata()
                                    .getSection(AnimationMetadataSection.TYPE);
                            } catch (IOException e) {
                                return Optional.empty();
                            }
                        })
                        .isPresent()
            ) {
                var animatableTexture = new AnimatableTexture(this.textureBase);

                textureManager.registerAndLoad(this.textureBase, animatableTexture);
                texture = animatableTexture;
            }

            return texture;
        };
        AbstractTexture originalTexture;

        try {
            originalTexture = RenderSystem.isOnRenderThread()
                ? resolveBase.get()
                : mc.submit(resolveBase).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(
                "Failed to load original texture: " + this.textureBase,
                e
            );
        }

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
            NativeImage firstFrame = animatableTexture.animationContents.animatedTexture.setGlowMaskTexture(
                this,
                baseImage,
                mask
            );

            closeIfOwned(baseImage, originalTexture);

            return new TextureContents(firstFrame, textureBaseMeta);
        }

        setPendingBaseUpload(originalTexture, baseImage);

        return new TextureContents(mask, textureBaseMeta);
    }

    @Override
    public void apply(@NonNull TextureContents contents) {
        super.apply(contents);

        AbstractTexture target = this.pendingBaseTexture;
        NativeImage baseImage = this.pendingBaseImage;

        this.pendingBaseTexture = null;
        this.pendingBaseImage = null;

        if (target == null)
            return;

        RenderSystem.queueFencedTask(() -> {
            if (target instanceof DynamicTexture dynamicTexture) {
                dynamicTexture.upload();
            } else if (baseImage != null) {
                uploadSimple(target.getTexture(), baseImage);
                baseImage.close();
            }
        });
    }

    @Override
    public void close() {
        clearPendingBaseUpload();
        super.close();
    }

    private void setPendingBaseUpload(AbstractTexture target, NativeImage baseImage) {
        clearPendingBaseUpload();
        this.pendingBaseTexture = target;
        this.pendingBaseImage = target instanceof DynamicTexture ? null : baseImage;
    }

    private void clearPendingBaseUpload() {
        if (this.pendingBaseImage != null)
            this.pendingBaseImage.close();

        this.pendingBaseImage = null;
        this.pendingBaseTexture = null;
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
