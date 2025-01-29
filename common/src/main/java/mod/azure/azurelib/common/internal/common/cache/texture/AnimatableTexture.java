/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.cache.texture;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.cache.texture.util.AnimationContents;

/**
 * Wrapper for {@link SimpleTexture SimpleTexture} implementation allowing for casual use of animated non-atlas textures
 */
public class AnimatableTexture extends AzAbstractTexture {

    private AnimationContents animationContents = null;

    private boolean isAnimated = false;

    public AnimatableTexture(final ResourceLocation location) {
        super(location);
    }

    public static void setAndUpdate(ResourceLocation texturePath, int frameTick) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(texturePath);

        if (texture instanceof AnimatableTexture animatableTexture)
            animatableTexture.setAnimationFrame(frameTick);

        RenderSystem.setShaderTexture(0, texture.getId());
    }

    @Override
    public void load(ResourceManager manager) throws IOException {
        Resource resource = manager.getResourceOrThrow(this.location);

        try {
            NativeImage nativeImage;

            try (InputStream inputstream = resource.open()) {
                nativeImage = NativeImage.read(inputstream);
            }

            this.animationContents = resource.metadata()
                .getSection(AnimationMetadataSection.SERIALIZER)
                .map(animMeta -> new AnimationContents(nativeImage, animMeta, this.getId(), location))
                .orElse(null);

            if (this.animationContents != null) {
                if (!this.animationContents.isValid()) {
                    nativeImage.close();

                    return;
                }

                this.isAnimated = true;

                onRenderThread(() -> {
                    TextureUtil.prepareImage(
                        getId(),
                        0,
                        this.animationContents.frameSize.width(),
                        this.animationContents.frameSize.height()
                    );
                    nativeImage.upload(
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
                });
            }
        } catch (RuntimeException exception) {
            AzureLib.LOGGER.warn("Failed reading metadata of: {}", this.location, exception);
        }
    }

    @Override
    protected @Nullable RenderCall loadTexture(ResourceManager resourceManager, Minecraft mc) throws IOException {
        return null;
    }

    /**
     * Returns whether the texture found any valid animation metadata when loading.
     * <p>
     * If false, then this is no different to a standard {@link SimpleTexture}
     */
    public boolean isAnimated() {
        return this.isAnimated;
    }

    public void setAnimationFrame(int tick) {
        if (this.animationContents != null)
            this.animationContents.animatedTexture.setCurrentFrame(tick);
    }
}
