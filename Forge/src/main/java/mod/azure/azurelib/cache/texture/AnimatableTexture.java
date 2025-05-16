/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.texture;

import com.mojang.blaze3d.systems.IRenderCall;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.resource.AzureAnimationMetadataSection;
import mod.azure.azurelib.util.AzureLibUtil;
import mod.azure.azurelib.util.RenderUtils;

/**
 * Wrapper for {@link SimpleTexture SimpleTexture} implementation allowing for casual use of animated non-atlas textures
 */
public class AnimatableTexture extends SimpleTexture {

    protected AnimationContents animationContents = null;

    protected boolean isAnimated = false;

    public AnimatableTexture(final ResourceLocation location) {
        super(location);
    }

    public static void setAndUpdate(ResourceLocation texturePath) {
        setAndUpdate(texturePath, (int) RenderUtils.getCurrentTick());
    }

    public static void setAndUpdate(ResourceLocation texturePath, int frameTick) {
        Texture texture = Minecraft.getInstance().getTextureManager().getTexture(texturePath);

        if (texture instanceof AnimatableTexture)
            ((AnimatableTexture) texture).setAnimationFrame(frameTick);
    }

    protected static void onRenderThread(IRenderCall renderCall) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(renderCall);
        } else {
            renderCall.execute();
        }
    }

    @Override
    public void load(IResourceManager manager) throws IOException {
        IResource resource = manager.getResource(this.location);
        AnimationMetadataSection animMeta = resource.getMetadata(AnimationMetadataSection.SERIALIZER);

        if (animMeta != null) {
            NativeImage nativeImage;

            try (InputStream inputstream = resource.getInputStream()) {
                nativeImage = NativeImage.read(inputstream);
            }

            this.animationContents = new AnimationContents(nativeImage, (AzureAnimationMetadataSection) animMeta);

            if (!this.animationContents.isValid()) {
                nativeImage.close();

                return;
            }

            this.isAnimated = true;

            onRenderThread(() -> {
                TextureUtil.prepareImage(
                    getId(),
                    0,
                    this.animationContents.frameSize.getFirst(),
                    this.animationContents.frameSize.getSecond()
                );
                nativeImage.upload(
                    0,
                    0,
                    0,
                    0,
                    0,
                    this.animationContents.frameSize.getFirst(),
                    this.animationContents.frameSize.getSecond(),
                    false,
                    false
                );
            });
        }
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

    protected class AnimationContents {

        protected final Pair<Integer, Integer> frameSize;

        protected final Texture animatedTexture;

        protected AnimationContents(NativeImage image, AzureAnimationMetadataSection animMeta) {
            this.frameSize = animMeta.getFrameSize(image.getWidth(), image.getHeight());
            this.animatedTexture = generateAnimatedTexture(image, animMeta);
        }

        protected boolean isValid() {
            return this.animatedTexture != null;
        }

        protected Texture generateAnimatedTexture(NativeImage image, AzureAnimationMetadataSection animMeta) {
            if (
                !AzureLibUtil.isMultipleOf(image.getWidth(), this.frameSize.getFirst()) || !AzureLibUtil.isMultipleOf(
                    image.getHeight(),
                    this.frameSize.getSecond()
                )
            ) {
                AzureLib.LOGGER.error(
                    "Image {} size {},{} is not multiple of frame size {},{}",
                    AnimatableTexture.this.location,
                    image.getWidth(),
                    image.getHeight(),
                    this.frameSize.getFirst(),
                    this.frameSize.getSecond()
                );

                return null;
            }

            int columns = image.getWidth() / this.frameSize.getFirst();
            int rows = image.getHeight() / this.frameSize.getSecond();
            int frameCount = columns * rows;
            List<Frame> frames = new ObjectArrayList<>();

            animMeta.forEachFrame((frame, frameTime) -> frames.add(new Frame(frame, frameTime)));

            if (frames.isEmpty()) {
                for (int frame = 0; frame < frameCount; ++frame) {
                    frames.add(new Frame(frame, animMeta.getDefaultFrameTime()));
                }
            } else {
                int index = 0;
                IntSet unusedFrames = new IntOpenHashSet();

                for (Frame frame : frames) {
                    if (frame.time <= 0) {
                        AzureLib.LOGGER.warn(
                            "Invalid frame duration on sprite {} frame {}: {}",
                            AnimatableTexture.this.location,
                            index,
                            frame.time
                        );
                        unusedFrames.add(frame.index);
                    } else if (frame.index < 0 || frame.index >= frameCount) {
                        AzureLib.LOGGER.warn(
                            "Invalid frame index on sprite {} frame {}: {}",
                            AnimatableTexture.this.location,
                            index,
                            frame.index
                        );
                        unusedFrames.add(frame.index);
                    }

                    index++;
                }

                if (!unusedFrames.isEmpty())
                    AzureLib.LOGGER.warn(
                        "Unused frames in sprite {}: {}",
                        AnimatableTexture.this.location,
                        Arrays.toString(unusedFrames.toArray())
                    );
            }

            return frames.size() <= 1
                ? null
                : new Texture(
                    image,
                    frames.toArray(new Frame[0]),
                    columns,
                    animMeta.isInterpolatedFrames()
                );
        }

        protected class Frame {

            int index;

            int time;

            public Frame(int index, int time) {
                this.index = index;
                this.time = time;
            }

        }

        protected class Texture implements AutoCloseable {

            protected final NativeImage baseImage;

            protected final Frame[] frames;

            protected final int framePanelSize;

            protected final boolean interpolating;

            protected final NativeImage interpolatedFrame;

            protected final int totalFrameTime;

            protected int glowMaskTextureId = -1;

            protected NativeImage glowmaskImage = null;

            protected NativeImage glowmaskInterpolatedFrame = null;

            protected int currentFrame;

            protected int currentSubframe;

            protected Texture(NativeImage baseImage, Frame[] frames, int framePanelSize, boolean interpolating) {
                this.baseImage = baseImage;
                this.frames = frames;
                this.framePanelSize = framePanelSize;
                this.interpolating = interpolating;
                this.interpolatedFrame = interpolating
                    ? new NativeImage(
                        AnimationContents.this.frameSize.getFirst(),
                        AnimationContents.this.frameSize.getSecond(),
                        false
                    )
                    : null;
                int time = 0;

                for (Frame frame : this.frames) {
                    time += frame.time;
                }

                this.totalFrameTime = time;
            }

            protected int getFrameX(int frameIndex) {
                return frameIndex % this.framePanelSize;
            }

            protected int getFrameY(int frameIndex) {
                return frameIndex / this.framePanelSize;
            }

            public void setGlowMaskTexture(AutoGlowingTexture texture, NativeImage baseImage, NativeImage glowMask) {
                this.glowMaskTextureId = texture.getId();
                this.glowmaskImage = glowMask;
                this.glowmaskInterpolatedFrame = this.interpolating
                    ? new NativeImage(
                        AnimationContents.this.frameSize.getFirst(),
                        AnimationContents.this.frameSize.getSecond(),
                        false
                    )
                    : null;
                this.baseImage.copyFrom(baseImage);
            }

            public void setCurrentFrame(int ticks) {
                ticks %= this.totalFrameTime;

                if (ticks == this.currentSubframe)
                    return;

                int lastSubframe = this.currentSubframe;
                int lastFrame = this.currentFrame;
                int time = 0;

                for (Frame frame : this.frames) {
                    time += frame.time;

                    if (ticks < time) {
                        this.currentFrame = frame.index;
                        this.currentSubframe = ticks % frame.time;

                        break;
                    }
                }

                if (this.currentFrame != lastFrame && this.currentSubframe == 0) {
                    onRenderThread(() -> {
                        TextureUtil.prepareImage(
                            AnimatableTexture.this.getId(),
                            0,
                            AnimationContents.this.frameSize.getFirst(),
                            AnimationContents.this.frameSize.getSecond()
                        );
                        this.baseImage.upload(
                            0,
                            0,
                            0,
                            getFrameX(this.currentFrame) * AnimationContents.this.frameSize.getFirst(),
                            getFrameY(this.currentFrame) * AnimationContents.this.frameSize.getSecond(),
                            AnimationContents.this.frameSize.getFirst(),
                            AnimationContents.this.frameSize.getSecond(),
                            false,
                            false
                        );
                    });
                } else if (this.currentSubframe != lastSubframe && this.interpolating) {
                    onRenderThread(this::generateInterpolatedFrame);
                }
            }

            protected void generateInterpolatedFrame() {
                Frame frame = this.frames[this.currentFrame];
                double frameProgress = 1 - (double) this.currentSubframe / (double) frame.time;
                int nextFrameIndex = this.frames[(this.currentFrame + 1) % this.frames.length].index;

                if (frame.index != nextFrameIndex) {
                    for (int y = 0; y < this.interpolatedFrame.getHeight(); ++y) {
                        for (int x = 0; x < this.interpolatedFrame.getWidth(); ++x) {
                            int prevFramePixel = getPixel(frame.index, x, y);
                            int nextFramePixel = getPixel(nextFrameIndex, x, y);
                            int blendedRed = interpolate(
                                frameProgress,
                                prevFramePixel >> 16 & 255,
                                nextFramePixel >> 16 & 255
                            );
                            int blendedGreen = interpolate(
                                frameProgress,
                                prevFramePixel >> 8 & 255,
                                nextFramePixel >> 8 & 255
                            );
                            int blendedBlue = interpolate(frameProgress, prevFramePixel & 255, nextFramePixel & 255);

                            this.interpolatedFrame.setPixelRGBA(
                                x,
                                y,
                                prevFramePixel & -16777216 | blendedRed << 16 | blendedGreen << 8 | blendedBlue
                            );
                        }
                    }

                    TextureUtil.prepareImage(
                        AnimatableTexture.this.getId(),
                        0,
                        AnimationContents.this.frameSize.getFirst(),
                        AnimationContents.this.frameSize.getSecond()
                    );
                    this.interpolatedFrame.upload(
                        0,
                        0,
                        0,
                        0,
                        0,
                        AnimationContents.this.frameSize.getFirst(),
                        AnimationContents.this.frameSize.getSecond(),
                        false,
                        false
                    );
                }
            }

            protected int getPixel(int frameIndex, int x, int y) {
                return this.baseImage.getPixelRGBA(
                    x + getFrameX(frameIndex) * AnimationContents.this.frameSize.getFirst(),
                    y + getFrameY(frameIndex) * AnimationContents.this.frameSize.getSecond()
                );
            }

            protected int interpolate(double frameProgress, double prevColour, double nextColour) {
                return (int) (frameProgress * prevColour + (1 - frameProgress) * nextColour);
            }

            @Override
            public void close() {
                this.baseImage.close();

                if (this.interpolatedFrame != null)
                    this.interpolatedFrame.close();
            }
        }
    }
}
