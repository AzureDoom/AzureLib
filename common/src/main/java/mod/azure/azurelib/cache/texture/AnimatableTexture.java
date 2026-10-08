package mod.azure.azurelib.cache.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

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

public class AnimatableTexture extends SimpleTexture implements TickableTexture {

    protected AnimationContents animationContents = null;

    protected boolean isAnimated = false;

    protected int frameWidth;

    protected int frameHeight;

    protected NativeImage sourceImage;

    protected NativeImage pendingImage;

    protected AnimationContents pendingContents;

    public AnimatableTexture(final Identifier location) {
        super(location);
    }

    @Override
    public @NonNull TextureContents loadContents(ResourceManager manager) throws IOException {
        Resource resource = manager.getResourceOrThrow(resourceId());
        TextureMetadataSection textureMeta = resource.metadata()
            .getSection(TextureMetadataSection.TYPE)
            .orElse(null);
        AnimationMetadataSection animMeta = resource.metadata()
            .getSection(AnimationMetadataSection.TYPE)
            .orElse(null);
        NativeImage image;

        try (InputStream inputstream = resource.open()) {
            image = NativeImage.read(inputstream);
        }

        AnimationContents contents = null;

        if (animMeta != null) {
            contents = new AnimationContents(image, animMeta);

            if (!contents.isValid())
                contents = null;
        }

        discardPending();
        this.pendingImage = image;
        this.pendingContents = contents;

        return new TextureContents(image, textureMeta);
    }

    public boolean hasPendingAnimation() {
        return this.pendingContents != null;
    }

    public boolean isAnimated() {
        return this.isAnimated;
    }

    @Override
    public void apply(@NonNull TextureContents textureContents) {
        NativeImage image = this.pendingImage;
        AnimationContents contents = this.pendingContents;

        this.pendingImage = null;
        this.pendingContents = null;

        if (image == null) {
            image = textureContents.image();
            contents = null;
        } else if (textureContents.image() != image) {
            textureContents.image().close();
        }

        AutoGlowingTexture boundGlowmask = this.animationContents != null
            && this.animationContents.animatedTexture != null
                ? this.animationContents.animatedTexture.glowMaskTexture
                : null;

        closeAnimationContents();

        if (this.sourceImage != null && this.sourceImage != image)
            this.sourceImage.close();

        this.sourceImage = image;
        this.animationContents = contents;
        this.isAnimated = contents != null;
        this.frameWidth = this.isAnimated ? contents.frameSize.width() : image.getWidth();
        this.frameHeight = this.isAnimated ? contents.frameSize.height() : image.getHeight();

        boolean clamp = textureContents.clamp();
        boolean blur = textureContents.blur();
        var addressMode = clamp ? AddressMode.CLAMP_TO_EDGE : AddressMode.REPEAT;
        var filterMode = blur ? FilterMode.LINEAR : FilterMode.NEAREST;

        this.sampler = RenderSystem.getSamplerCache()
            .getSampler(addressMode, addressMode, filterMode, filterMode, false);
        doLoad(image);

        if (boundGlowmask != null) {
            RenderSystem.queueFencedTask(
                () -> Minecraft.getInstance()
                    .getTextureManager()
                    .registerAndLoad(boundGlowmask.glowLayer, boundGlowmask)
            );
        }
    }

    @Override
    public void doLoad(@NonNull NativeImage image) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        Identifier textureId = resourceId();

        Objects.requireNonNull(textureId);

        super.close();

        this.texture = gpuDevice.createTexture(
            textureId::toString,
            5,
            TextureFormat.RGBA8,
            this.frameWidth,
            this.frameHeight,
            1,
            1
        );
        this.textureView = gpuDevice.createTextureView(this.texture);

        upload(this.texture, image, this.frameWidth, this.frameHeight);
    }

    @Override
    public void close() {
        discardPending();
        closeAnimationContents();

        if (this.sourceImage != null) {
            this.sourceImage.close();
            this.sourceImage = null;
        }

        super.close();
    }

    private void closeAnimationContents() {
        if (this.animationContents != null && this.animationContents.animatedTexture != null) {
            this.animationContents.animatedTexture.close();
        }
        this.animationContents = null;
        this.isAnimated = false;
    }

    private void discardPending() {
        if (this.pendingContents != null && this.pendingContents.animatedTexture != null)
            this.pendingContents.animatedTexture.close();

        if (this.pendingImage != null)
            this.pendingImage.close();

        this.pendingContents = null;
        this.pendingImage = null;
    }

    public static void setAndUpdate(Identifier texturePath) {
        setAndUpdate(texturePath, (int) RenderUtils.getCurrentTick());
    }

    public static void setAndUpdate(Identifier texturePath, int frameTick) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(texturePath);

        if (texture instanceof AnimatableTexture animatableTexture)
            animatableTexture.setAnimationFrame(frameTick);
    }

    public void setAnimationFrame(int tick) {
        if (this.animationContents != null && this.animationContents.animatedTexture != null)
            this.animationContents.animatedTexture.setCurrentFrame(tick);
    }

    private static void onRenderThread(Runnable renderCall) {
        if (RenderSystem.isOnRenderThread()) {
            renderCall.run();
        } else {
            RenderSystem.queueFencedTask(renderCall);
        }
    }

    private void upload(GpuTexture target, NativeImage image, int width, int height) {
        if (target == null || target.isClosed())
            return;

        RenderSystem.getDevice()
            .createCommandEncoder()
            .writeToTexture(target, image, 0, 0, 0, 0, width, height, 0, 0);
    }

    @Override
    public void tick() {
        if (this.animationContents != null) {
            this.animationContents.tick();
        }
    }

    protected class AnimationContents {

        protected final FrameSize frameSize;

        protected final Texture animatedTexture;

        private AnimationContents(NativeImage image, AnimationMetadataSection animMeta) {
            this.frameSize = animMeta.calculateFrameSize(image.getWidth(), image.getHeight());
            this.animatedTexture = generateAnimatedTexture(image, animMeta);
        }

        private boolean isValid() {
            return this.animatedTexture != null;
        }

        protected void tick() {
            if (this.animatedTexture != null)
                this.animatedTexture.setCurrentFrame((int) RenderUtils.getCurrentTick());
        }

        private Texture generateAnimatedTexture(NativeImage image, AnimationMetadataSection animMeta) {
            if (
                !Mth.isMultipleOf(image.getWidth(), this.frameSize.width()) || !Mth.isMultipleOf(
                    image.getHeight(),
                    this.frameSize.height()
                )
            ) {
                AzureLib.LOGGER.error(
                    "Image {} size {},{} is not multiple of frame size {},{}",
                    AnimatableTexture.this.resourceId(),
                    image.getWidth(),
                    image.getHeight(),
                    this.frameSize.width(),
                    this.frameSize.height()
                );

                return null;
            }

            int columns = image.getWidth() / this.frameSize.width();
            int rows = image.getHeight() / this.frameSize.height();
            int frameCount = columns * rows;
            List<Frame> frames = new ObjectArrayList<>();

            animMeta.frames()
                .ifPresent(
                    animationFrames -> animationFrames.forEach(
                        animationFrame -> frames.add(
                            new Frame(animationFrame.index(), animationFrame.time().orElse(animMeta.defaultFrameTime()))
                        )
                    )
                );

            if (frames.isEmpty()) {
                for (int frame = 0; frame < frameCount; ++frame) {
                    frames.add(new Frame(frame, animMeta.defaultFrameTime()));
                }
            } else {
                int index = 0;
                IntSet validFrames = new IntOpenHashSet();

                for (Iterator<Frame> iterator = frames.iterator(); iterator.hasNext(); index++) {
                    Frame frame = iterator.next();
                    boolean validFrame = true;

                    if (frame.time() <= 0) {
                        AzureLib.LOGGER.warn(
                            "Invalid frame duration on sprite {} frame {}: {}",
                            AnimatableTexture.this.resourceId(),
                            index,
                            frame.time()
                        );
                        validFrame = false;
                    }

                    if (frame.index() < 0 || frame.index() >= frameCount) {
                        AzureLib.LOGGER.warn(
                            "Invalid frame index on sprite {} frame {}: {}",
                            AnimatableTexture.this.resourceId(),
                            index,
                            frame.index()
                        );
                        validFrame = false;
                    }

                    if (validFrame) {
                        validFrames.add(frame.index());
                    } else {
                        iterator.remove();
                    }
                }

                int[] unusedFrames = IntStream.range(0, frameCount)
                    .filter(frame -> !validFrames.contains(frame))
                    .toArray();

                if (!(unusedFrames.length == 0)) {
                    AzureLib.LOGGER.warn(
                        "Unused frames in sprite {}: {}",
                        AnimatableTexture.this.resourceId(),
                        Arrays.toString(unusedFrames)
                    );
                }
            }

            if (frames.size() <= 1)
                return null;

            return new Texture(image, frames.toArray(new Frame[0]), columns, animMeta.interpolatedFrames());
        }

        protected class Texture implements AutoCloseable {

            protected final NativeImage baseImage;

            protected final Frame[] frames;

            protected final int framePanelSize;

            protected final boolean interpolating;

            protected final NativeImage frameBuffer;

            protected final NativeImage interpolatedFrame;

            protected final int totalFrameTime;

            protected AutoGlowingTexture glowMaskTexture = null;

            protected NativeImage glowmaskImage = null;

            protected NativeImage glowmaskFrameBuffer = null;

            protected NativeImage glowmaskInterpolatedFrame = null;

            protected int currentFrame;

            protected int currentSubframe;

            private Texture(NativeImage baseImage, Frame[] frames, int framePanelSize, boolean interpolating) {
                this.baseImage = baseImage;
                this.frames = frames;
                this.framePanelSize = framePanelSize;
                this.interpolating = interpolating;
                this.frameBuffer = newFrameImage();
                this.interpolatedFrame = interpolating ? newFrameImage() : null;
                int time = 0;

                for (Frame frame : this.frames) {
                    time += frame.time();
                }

                this.totalFrameTime = time;
            }

            private NativeImage newFrameImage() {
                return new NativeImage(
                    AnimationContents.this.frameSize.width(),
                    AnimationContents.this.frameSize.height(),
                    true
                );
            }

            private int getFrameX(int frameIndex) {
                return frameIndex % this.framePanelSize;
            }

            private int getFrameY(int frameIndex) {
                return frameIndex / this.framePanelSize;
            }

            private void blitFrame(NativeImage sheet, NativeImage target, int frameIndex) {
                sheet.copyRect(
                    target,
                    getFrameX(frameIndex) * AnimationContents.this.frameSize.width(),
                    getFrameY(frameIndex) * AnimationContents.this.frameSize.height(),
                    0,
                    0,
                    AnimationContents.this.frameSize.width(),
                    AnimationContents.this.frameSize.height(),
                    false,
                    false
                );
            }

            public NativeImage setGlowMaskTexture(
                AutoGlowingTexture texture,
                NativeImage baseImage,
                NativeImage glowMask
            ) {
                releaseGlowmaskImages();

                this.glowMaskTexture = texture;
                this.glowmaskFrameBuffer = newFrameImage();
                this.glowmaskInterpolatedFrame = this.interpolating ? newFrameImage() : null;
                this.glowmaskImage = glowMask;
                this.baseImage.copyFrom(baseImage);

                NativeImage firstFrame = newFrameImage();

                blitFrame(glowMask, firstFrame, 0);

                return firstFrame;
            }

            private void releaseGlowmaskImages() {
                NativeImage mask = this.glowmaskImage;
                NativeImage frame = this.glowmaskFrameBuffer;
                NativeImage interpolated = this.glowmaskInterpolatedFrame;

                this.glowmaskImage = null;
                this.glowmaskFrameBuffer = null;
                this.glowmaskInterpolatedFrame = null;

                if (mask == null && frame == null && interpolated == null)
                    return;

                onRenderThread(() -> {
                    if (mask != null)
                        mask.close();

                    if (frame != null)
                        frame.close();

                    if (interpolated != null)
                        interpolated.close();
                });
            }

            private GpuTexture glowMaskGpuTexture() {
                return this.glowMaskTexture == null ? null : this.glowMaskTexture.getTexture();
            }

            public void setCurrentFrame(int ticks) {
                ticks %= this.totalFrameTime;

                if (ticks == this.currentSubframe)
                    return;

                int lastSubframe = this.currentSubframe;
                int lastFrame = this.currentFrame;
                int time = 0;

                for (Frame frame : this.frames) {
                    time += frame.time();

                    if (ticks < time) {
                        this.currentFrame = frame.index();
                        this.currentSubframe = ticks % frame.time();

                        break;
                    }
                }

                if (this.currentFrame != lastFrame && this.currentSubframe == 0) {
                    onRenderThread(() -> {
                        blitFrame(this.baseImage, this.frameBuffer, this.currentFrame);
                        AnimatableTexture.this.upload(
                            AnimatableTexture.this.texture,
                            this.frameBuffer,
                            AnimatableTexture.this.frameWidth,
                            AnimatableTexture.this.frameHeight
                        );

                        NativeImage glowmask = this.glowmaskImage;
                        NativeImage glowmaskFrame = this.glowmaskFrameBuffer;

                        if (glowmask != null && glowmaskFrame != null) {
                            blitFrame(glowmask, glowmaskFrame, this.currentFrame);
                            AnimatableTexture.this.upload(
                                glowMaskGpuTexture(),
                                glowmaskFrame,
                                AnimatableTexture.this.frameWidth,
                                AnimatableTexture.this.frameHeight
                            );
                        }
                    });
                } else if (this.currentSubframe != lastSubframe && this.interpolating) {
                    onRenderThread(() -> {
                        generateInterpolatedFrame(
                            AnimatableTexture.this.texture,
                            this.baseImage,
                            this.interpolatedFrame
                        );

                        NativeImage glowmask = this.glowmaskImage;
                        NativeImage glowmaskInterpolated = this.glowmaskInterpolatedFrame;

                        if (glowmask != null && glowmaskInterpolated != null) {
                            generateInterpolatedFrame(
                                glowMaskGpuTexture(),
                                glowmask,
                                glowmaskInterpolated
                            );
                        }
                    });
                }
            }

            private void generateInterpolatedFrame(
                GpuTexture target,
                NativeImage image,
                NativeImage interpolatedFrame
            ) {
                Frame frame = this.frames[this.currentFrame];
                double frameProgress = 1 - (double) this.currentSubframe / (double) frame.time();
                int nextFrameIndex = this.frames[(this.currentFrame + 1) % this.frames.length].index();

                if (frame.index() != nextFrameIndex) {
                    for (int y = 0; y < interpolatedFrame.getHeight(); ++y) {
                        for (int x = 0; x < interpolatedFrame.getWidth(); ++x) {
                            int prevFramePixel = getPixel(image, frame.index(), x, y);
                            int nextFramePixel = getPixel(image, nextFrameIndex, x, y);
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

                            interpolatedFrame.setPixel(
                                x,
                                y,
                                prevFramePixel & -16777216 | blendedRed << 16 | blendedGreen << 8 | blendedBlue
                            );
                        }
                    }

                    AnimatableTexture.this.upload(
                        target,
                        interpolatedFrame,
                        AnimatableTexture.this.frameWidth,
                        AnimatableTexture.this.frameHeight
                    );
                }
            }

            private int getPixel(NativeImage image, int frameIndex, int x, int y) {
                return image.getPixel(
                    x + getFrameX(frameIndex) * AnimationContents.this.frameSize.width(),
                    y + getFrameY(frameIndex) * AnimationContents.this.frameSize.height()
                );
            }

            private int interpolate(double frameProgress, double prevColor, double nextColor) {
                return (int) (frameProgress * prevColor + (1 - frameProgress) * nextColor);
            }

            @Override
            public void close() {
                this.frameBuffer.close();

                if (this.interpolatedFrame != null) {
                    this.interpolatedFrame.close();
                }

                if (this.glowmaskImage != null) {
                    this.glowmaskImage.close();
                }

                if (this.glowmaskFrameBuffer != null) {
                    this.glowmaskFrameBuffer.close();
                }

                if (this.glowmaskInterpolatedFrame != null) {
                    this.glowmaskInterpolatedFrame.close();
                }
            }
        }
    }
}
