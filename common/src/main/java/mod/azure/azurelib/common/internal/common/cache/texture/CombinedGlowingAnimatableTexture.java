package mod.azure.azurelib.common.internal.common.cache.texture;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import mod.azure.azurelib.common.internal.common.cache.texture.util.AnimationContents;

/**
 * A texture class that combines animation and auto-glowing features.
 */
public class CombinedGlowingAnimatableTexture extends AzAbstractTexture {

    private AnimationContents animationContents = null;

    private boolean isAnimated = false;

    private static final String GLOW_LAYER_SUFFIX = "_glowmask";

    private final ResourceLocation glowLayer;

    public CombinedGlowingAnimatableTexture(ResourceLocation baseLocation) {
        super(baseLocation);
        this.glowLayer = appendToPath(baseLocation, GLOW_LAYER_SUFFIX);
    }

    @Override
    public void load(ResourceManager manager) throws IOException {
        Resource originalResource = manager.getResourceOrThrow(this.location);
        NativeImage baseImage;

        try (InputStream inputStream = originalResource.open()) {
            baseImage = NativeImage.read(inputStream);
        }

        // Process animation metadata, if available
        this.animationContents = originalResource.metadata()
            .getSection(AnimationMetadataSection.SERIALIZER)
            .map(metadata -> new AnimationContents(baseImage, metadata, getId(), location))
            .orElse(null);

        if (this.animationContents != null && this.animationContents.isValid()) {
            this.isAnimated = true;
        }

        // Handle glow mask layer
        NativeImage glowImage = processGlowLayer(manager, baseImage);

        // Upload textures
        uploadTextures(baseImage, glowImage);
    }

    @Nullable
    @Override
    protected RenderCall loadTexture(ResourceManager resourceManager, Minecraft mc) throws IOException {
        return null; // Since `load` already handles all the texture loading logic
    }

    /**
     * Processes and retrieves the glow layer image.
     */
    private NativeImage processGlowLayer(ResourceManager manager, NativeImage baseImage) {
        try {
            Optional<Resource> glowLayerResource = manager.getResource(this.glowLayer);
            if (glowLayerResource.isPresent()) {
                try (InputStream inputStream = glowLayerResource.get().open()) {
                    return NativeImage.read(inputStream);
                }
            } else {
                // If no glow layer, create one from metadata
                Optional<TextureMetadataSection> metadataSection = manager.getResourceOrThrow(this.location)
                    .metadata()
                    .getSection(TextureMetadataSection.SERIALIZER);

                if (metadataSection.isPresent()) {
                    NativeImage glowImage = new NativeImage(baseImage.getWidth(), baseImage.getHeight(), true);
                    // Perform any additional glow mask creation logic here, if necessary
                    return glowImage;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Return null if no glow layer could be processed
    }

    /**
     * Uploads the base and glow textures into the render pipeline.
     */
    private void uploadTextures(NativeImage baseImage, NativeImage glowImage) {
        if (this.animationContents != null) {
            // Handle animated textures with multiple frames
            TextureUtil.prepareImage(
                getId(),
                0,
                this.animationContents.frameSize.width(),
                this.animationContents.frameSize.height()
            );

            baseImage.upload(
                0,
                0,
                0,
                0,
                0,
                this.animationContents.frameSize.width(),
                this.animationContents.frameSize.height(),
                false,
                false
            );
        } else {
            // Handle static textures
            TextureUtil.prepareImage(getId(), 0, baseImage.getWidth(), baseImage.getHeight());
            baseImage.upload(0, 0, 0, 0, 0, baseImage.getWidth(), baseImage.getHeight(), false, false);
        }

        if (glowImage != null) {
            int glowTextureId = Minecraft.getInstance()
                .getTextureManager()
                .getTexture(this.glowLayer, new DynamicTexture(glowImage))
                .getId();
            TextureUtil.prepareImage(glowTextureId, 0, glowImage.getWidth(), glowImage.getHeight());
            glowImage.upload(0, 0, 0, 0, 0, glowImage.getWidth(), glowImage.getHeight(), false, false);
        }
    }

    public static void setAndUpdate(ResourceLocation texturePath, int frameTick) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(texturePath);

        if (texture instanceof AnimatableTexture animatableTexture)
            animatableTexture.setAnimationFrame(frameTick);

        RenderSystem.setShaderTexture(0, texture.getId());
    }

    public boolean isAnimated() {
        return this.isAnimated;
    }
}
