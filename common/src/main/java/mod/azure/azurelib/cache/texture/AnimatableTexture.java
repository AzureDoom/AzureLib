/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.texture;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TickableTexture;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.texture.util.Frame;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * Wrapper for {@link SimpleTexture} allowing vanilla animated texture metadata on non-atlas textures. Updated for the
 * 1.21.6+/26.2 GPU texture path.
 */
public class AnimatableTexture extends SimpleTexture implements TickableTexture {

    protected @Nullable AnimationContents animationContents = null;

    protected int frameWidth;

    protected int frameHeight;

    protected @Nullable NativeImage baseImage;

    public AnimatableTexture(final Identifier location) {
        super(location);
    }

    @Override
    public @NonNull TextureContents loadContents(ResourceManager manager) throws IOException {
        Resource resource = manager.getResourceOrThrow(resourceId());

        try (InputStream stream = resource.open()) {
            this.baseImage = NativeImage.read(stream);
        }

        this.animationContents = resource.metadata()
            .getSection(AnimationMetadataSection.TYPE)
            .map(this::buildAnimationContents)
            .orElse(null);

        return new TextureContents(
            this.baseImage,
            resource.metadata().getSection(TextureMetadataSection.TYPE).orElse(null)
        );
    }

    @Override
    public void apply(@NonNull TextureContents textureContents) {
        if (this.baseImage == null)
            return;

        AddressMode address = textureContents.clamp() ? AddressMode.CLAMP_TO_EDGE : AddressMode.REPEAT;
        FilterMode filter = textureContents.blur() ? FilterMode.LINEAR : FilterMode.NEAREST;
        this.sampler = RenderSystem.getSamplerCache().getSampler(address, address, filter, filter, false);

        doLoad(this.baseImage);
    }

    @Override
    public void doLoad(@NonNull NativeImage image) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        Identifier textureId = resourceId();

        Objects.requireNonNull(textureId);

        this.texture = gpuDevice.createTexture(
            textureId::toString,
            5,
            GpuFormat.RGBA8_UNORM,
            this.frameWidth,
            this.frameHeight,
            1,
            1
        );
        this.textureView = gpuDevice.createTextureView(this.texture);

        uploadFrame(gpuDevice, image, 0, 0, this.texture);
    }

    /**
     * Returns whether the texture found any valid animation metadata when loading. If false, then this is no different
     * from a standard {@link SimpleTexture}.
     */
    public boolean isAnimated() {
        return this.animationContents != null;
    }

    public static void setAndUpdate(Identifier texturePath) {
        setAndUpdate(texturePath, (int) RenderUtils.getCurrentTick());
    }

    /**
     * Setting a specific frame is still best-effort because render passes may be buffered.
     */
    public static void setAndUpdate(Identifier texturePath, int frameTick) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(texturePath);

        try {
            var method = texture.getClass().getMethod("setAnimationFrame", int.class);
            method.invoke(texture, frameTick);
        } catch (ReflectiveOperationException ignored) {}

        // RenderSystem.setShaderTexture(0, texture.getTexture());
    }

    public void setAnimationFrame(int tick) {
        if (this.animationContents != null)
            this.animationContents.setCurrentFrame(tick);
    }

    @Override
    public void tick() {
        if (this.animationContents != null)
            this.animationContents.tick();
    }

    @Override
    public void close() {
        if (this.baseImage != null)
            this.baseImage.close();

        if (this.animationContents != null)
            this.animationContents.close();

        super.close();
    }

    protected @Nullable AnimationContents buildAnimationContents(AnimationMetadataSection animMeta) {
        if (this.baseImage == null)
            return null;

        final FrameSize frameSize = animMeta.calculateFrameSize(this.baseImage.getWidth(), this.baseImage.getHeight());
        this.frameWidth = frameSize.width();
        this.frameHeight = frameSize.height();

        if (
            !Mth.isMultipleOf(this.baseImage.getWidth(), this.frameWidth) || !Mth.isMultipleOf(
                this.baseImage.getHeight(),
                this.frameHeight
            )
        ) {
            AzureLib.LOGGER.error(
                "Image {} size {},{} is not multiple of frame size {},{}",
                resourceId(),
                this.baseImage.getWidth(),
                this.baseImage.getHeight(),
                this.frameWidth,
                this.frameHeight
            );
            return null;
        }

        final int columns = this.baseImage.getWidth() / this.frameWidth;
        final int rows = this.baseImage.getHeight() / this.frameHeight;
        final int availableFrames = columns * rows;
        final int defaultFrameTime = animMeta.defaultFrameTime();
        final int frameCount = animMeta.frames().map(List::size).orElse(availableFrames);

        if (frameCount <= 1)
            return null;

        final List<Frame> frames = new ObjectArrayList<>(frameCount);

        if (animMeta.frames().isEmpty()) {
            for (int i = 0; i < availableFrames; i++)
                frames.add(new Frame(i, defaultFrameTime));
        } else {
            for (AnimationFrame frame : animMeta.frames().get())
                frames.add(new Frame(frame.index(), frame.timeOr(defaultFrameTime)));

            int frameIndex = 0;
            IntSet validFrames = new IntOpenHashSet();

            for (Iterator<Frame> iterator = frames.iterator(); iterator.hasNext(); frameIndex++) {
                Frame frame = iterator.next();
                boolean validFrame = true;

                if (frame.time() <= 0) {
                    AzureLib.LOGGER.warn(
                        "Invalid frame duration on sprite {} frame {}: {}",
                        resourceId(),
                        frameIndex,
                        frame.time()
                    );
                    validFrame = false;
                }

                if (frame.index() < 0 || frame.index() >= availableFrames) {
                    AzureLib.LOGGER.warn(
                        "Invalid frame index on sprite {} frame {}: {}",
                        resourceId(),
                        frameIndex,
                        frame.index()
                    );
                    validFrame = false;
                }

                if (validFrame)
                    validFrames.add(frame.index());
                else
                    iterator.remove();
            }

            int[] unusedFrames = IntStream.range(0, availableFrames)
                .filter(frame -> !validFrames.contains(frame))
                .toArray();

            if (unusedFrames.length > 0)
                AzureLib.LOGGER.warn("Unused frames in sprite {}: {}", resourceId(), Arrays.toString(unusedFrames));
        }

        return frames.size() <= 1
            ? null
            : new AnimationContents(List.copyOf(frames), columns, animMeta.interpolatedFrames());
    }

    protected void uploadFrame(GpuDevice gpuDevice, NativeImage image, int x, int y, GpuTexture gpuTexture) {
        gpuDevice.createCommandEncoder()
            .writeToTexture(gpuTexture, image.getPixelBytes(), 0, 0, x, y, this.frameWidth, this.frameHeight);
    }

    protected class AnimationContents implements AutoCloseable {

        protected final List<Frame> frames;

        protected final int frameRowSize;

        protected final boolean interpolateFrames;

        protected final @Nullable InterpolationData interpolationData;

        protected final NativeImage currentFrameBuffer;

        protected @Nullable GpuTexture glowMaskTexture;

        protected @Nullable NativeImage glowMaskImage;

        protected @Nullable NativeImage glowMaskFrameBuffer;

        protected @Nullable InterpolationData glowMaskInterpolationData;

        int currentFrame;

        int subFrame;

        int totalFrameTime;

        public AnimationContents(List<Frame> frames, int frameRowSize, boolean interpolateFrames) {
            this.frames = frames;
            this.frameRowSize = frameRowSize;
            this.interpolateFrames = interpolateFrames;
            this.interpolationData = interpolateFrames
                ? new InterpolationData(AnimatableTexture.this.frameWidth, AnimatableTexture.this.frameHeight)
                : null;
            this.currentFrameBuffer = new NativeImage(
                AnimatableTexture.this.frameWidth,
                AnimatableTexture.this.frameHeight,
                false
            );

            for (Frame frame : frames)
                this.totalFrameTime += frame.time();
        }

        int getFrameColumn(int frameIndex) {
            return frameIndex % this.frameRowSize;
        }

        int getFrameRow(int frameIndex) {
            return frameIndex / this.frameRowSize;
        }

        public void setGlowMaskTexture(AutoGlowingTexture texture, NativeImage baseImage, NativeImage glowMask) {
            this.glowMaskTexture = texture.getTexture();
            this.glowMaskImage = glowMask;
            this.glowMaskFrameBuffer = new NativeImage(
                AnimatableTexture.this.frameWidth,
                AnimatableTexture.this.frameHeight,
                false
            );

            if (this.interpolateFrames)
                this.glowMaskInterpolationData = new InterpolationData(
                    AnimatableTexture.this.frameWidth,
                    AnimatableTexture.this.frameHeight
                );

            if (AnimatableTexture.this.baseImage != null)
                AnimatableTexture.this.baseImage.copyFrom(baseImage);
        }

        public void setCurrentFrame(int ticks) {
            if (this.totalFrameTime <= 0)
                return;

            ticks %= this.totalFrameTime;
            int accumulated = 0;

            for (int i = 0; i < this.frames.size(); i++) {
                Frame frame = this.frames.get(i);
                accumulated += frame.time();

                if (ticks < accumulated) {
                    boolean changedFrame = this.currentFrame != i;
                    this.currentFrame = i;
                    this.subFrame = ticks - (accumulated - frame.time());
                    uploadCurrentFrame(changedFrame);
                    return;
                }
            }
        }

        public void tick() {
            if (AnimatableTexture.this.baseImage == null)
                return;

            this.subFrame++;
            Frame prevFrameInfo = this.frames.get(this.currentFrame);

            if (this.subFrame >= prevFrameInfo.time()) {
                this.currentFrame = (this.currentFrame + 1) % this.frames.size();
                this.subFrame = 0;
                uploadCurrentFrame(prevFrameInfo.index() != this.frames.get(this.currentFrame).index());
            } else if (this.interpolationData != null) {
                this.interpolationData.tickAndUpload(AnimatableTexture.this.baseImage, getTexture());

                if (
                    this.glowMaskInterpolationData != null && this.glowMaskImage != null && this.glowMaskTexture != null
                )
                    this.glowMaskInterpolationData.tickAndUpload(this.glowMaskImage, this.glowMaskTexture);
            }
        }

        protected void uploadCurrentFrame(boolean frameChanged) {
            if (!frameChanged || AnimatableTexture.this.baseImage == null)
                return;

            int frameIndex = this.frames.get(this.currentFrame).index();
            int frameX = getFrameColumn(frameIndex) * AnimatableTexture.this.frameWidth;
            int frameY = getFrameRow(frameIndex) * AnimatableTexture.this.frameHeight;

            AnimatableTexture.this.baseImage.copyRect(
                this.currentFrameBuffer,
                frameX,
                frameY,
                0,
                0,
                AnimatableTexture.this.frameWidth,
                AnimatableTexture.this.frameHeight,
                false,
                false
            );
            uploadFrame(RenderSystem.getDevice(), this.currentFrameBuffer, 0, 0, getTexture());

            if (this.glowMaskImage != null && this.glowMaskFrameBuffer != null && this.glowMaskTexture != null) {
                this.glowMaskImage.copyRect(
                    this.glowMaskFrameBuffer,
                    frameX,
                    frameY,
                    0,
                    0,
                    AnimatableTexture.this.frameWidth,
                    AnimatableTexture.this.frameHeight,
                    false,
                    false
                );
                uploadFrame(RenderSystem.getDevice(), this.glowMaskFrameBuffer, 0, 0, this.glowMaskTexture);
            }
        }

        @Override
        public void close() {
            if (this.interpolationData != null)
                this.interpolationData.close();

            if (this.glowMaskInterpolationData != null)
                this.glowMaskInterpolationData.close();

            if (this.glowMaskFrameBuffer != null)
                this.glowMaskFrameBuffer.close();

            if (this.glowMaskImage != null)
                this.glowMaskImage.close();

            this.currentFrameBuffer.close();
        }

        protected class InterpolationData implements AutoCloseable {

            protected final NativeImage buffer;

            public InterpolationData(int frameWidth, int frameHeight) {
                this.buffer = new NativeImage(frameWidth, frameHeight, false);
            }

            protected void tickAndUpload(NativeImage image, GpuTexture gpuTexture) {
                AnimationContents instance = AnimationContents.this;
                List<Frame> frames = instance.frames;
                Frame currentFrameInfo = frames.get(instance.currentFrame);
                int nextFrameIndex = frames.get((instance.currentFrame + 1) % frames.size()).index();

                if (currentFrameInfo.index() != nextFrameIndex) {
                    float partialFrame = instance.subFrame / (float) currentFrameInfo.time();
                    int frameHeight = AnimatableTexture.this.frameHeight;
                    int frameWidth = AnimatableTexture.this.frameWidth;

                    for (int pixelY = 0; pixelY < frameHeight; pixelY++) {
                        for (int pixelX = 0; pixelX < frameWidth; pixelX++) {
                            int framePixel = getPixel(
                                image,
                                instance,
                                currentFrameInfo.index(),
                                pixelX,
                                pixelY,
                                frameWidth,
                                frameHeight
                            );
                            int nextFramePixel = getPixel(
                                image,
                                instance,
                                nextFrameIndex,
                                pixelX,
                                pixelY,
                                frameWidth,
                                frameHeight
                            );

                            this.buffer.setPixel(
                                pixelX,
                                pixelY,
                                ARGB.linearLerp(partialFrame, framePixel, nextFramePixel)
                            );
                        }
                    }

                    AnimatableTexture.this.uploadFrame(RenderSystem.getDevice(), this.buffer, 0, 0, gpuTexture);
                }
            }

            protected int getPixel(
                NativeImage image,
                AnimationContents animationInfo,
                int frameIndex,
                int x,
                int y,
                int frameWidth,
                int frameHeight
            ) {
                return image.getPixel(
                    x + animationInfo.getFrameColumn(frameIndex) * frameWidth,
                    y + animationInfo.getFrameRow(frameIndex) * frameHeight
                );
            }

            @Override
            public void close() {
                this.buffer.close();
            }
        }
    }
}
