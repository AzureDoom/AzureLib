package mod.azure.azurelib.common.internal.common.cache.texture.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.resources.metadata.animation.FrameSize;

import static mod.azure.azurelib.common.internal.common.cache.texture.AzAbstractTexture.onRenderThread;

public class Texture implements AutoCloseable {

    private final NativeImage baseImage;

    private final Frame[] frames;

    private final int framePanelSize;

    private final boolean interpolating;

    private final NativeImage interpolatedFrame;

    private final int totalFrameTime;

    private int currentFrame;

    private int currentSubframe;

    private final FrameSize frameSize;

    private int getID;

    public Texture(
        NativeImage baseImage,
        Frame[] frames,
        int framePanelSize,
        boolean interpolating,
        FrameSize frameSize,
        int getID
    ) {
        this.baseImage = baseImage;
        this.frames = frames;
        this.framePanelSize = framePanelSize;
        this.interpolating = interpolating;
        this.interpolatedFrame = interpolating
            ? new NativeImage(
                frameSize.width(),
                frameSize.height(),
                false
            )
            : null;
        int time = 0;

        for (Frame frame : this.frames) {
            time += frame.time();
        }

        this.totalFrameTime = time;
        this.frameSize = frameSize;
        this.getID = getID;
    }

    private int getFrameX(int frameIndex) {
        return frameIndex % this.framePanelSize;
    }

    private int getFrameY(int frameIndex) {
        return frameIndex / this.framePanelSize;
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
                TextureUtil.prepareImage(
                    this.getID,
                    0,
                    this.frameSize.width(),
                    this.frameSize.height()
                );
                this.baseImage.upload(
                    0,
                    0,
                    0,
                    getFrameX(this.currentFrame) * this.frameSize.width(),
                    getFrameY(this.currentFrame) * this.frameSize.height(),
                    this.frameSize.width(),
                    this.frameSize.height(),
                    false,
                    false
                );
            });
        } else if (this.currentSubframe != lastSubframe && this.interpolating) {
            onRenderThread(this::generateInterpolatedFrame);
        }
    }

    private void generateInterpolatedFrame() {
        Frame frame = this.frames[this.currentFrame];
        double frameProgress = 1 - this.currentSubframe / (double) frame.time();
        int nextFrameIndex = this.frames[(this.currentFrame + 1) % this.frames.length].index();

        if (frame.index() != nextFrameIndex) {
            for (int y = 0; y < this.interpolatedFrame.getHeight(); ++y) {
                for (int x = 0; x < this.interpolatedFrame.getWidth(); ++x) {
                    int prevFramePixel = getPixel(frame.index(), x, y);
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
                this.getID,
                0,
                this.frameSize.width(),
                this.frameSize.height()
            );
            this.interpolatedFrame.upload(
                0,
                0,
                0,
                0,
                0,
                this.frameSize.width(),
                this.frameSize.height(),
                false,
                false
            );
        }
    }

    private int getPixel(int frameIndex, int x, int y) {
        return this.baseImage.getPixelRGBA(
            x + getFrameX(frameIndex) * this.frameSize.width(),
            y + getFrameY(frameIndex) * this.frameSize.height()
        );
    }

    private int interpolate(double frameProgress, double prevColour, double nextColour) {
        return (int) (frameProgress * prevColour + (1 - frameProgress) * nextColour);
    }

    @Override
    public void close() {
        this.baseImage.close();

        if (this.interpolatedFrame != null)
            this.interpolatedFrame.close();
    }
}
