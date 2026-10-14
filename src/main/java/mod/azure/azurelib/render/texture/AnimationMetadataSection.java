package mod.azure.azurelib.render.texture;

import java.util.List;

/**
 * The {@code animation} section of a {@code .mcmeta} file. Mirrors 1.7.10's class of the same name.
 */
public final class AnimationMetadataSection {

    private final List<int[]> frames;

    private final int frameWidth;

    private final int frameHeight;

    private final int frameTime;

    private final boolean interpolate;

    /**
     * @param frames each entry is {@code {index, time}}, with {@code time == -1} meaning "use the default frame time"
     */
    public AnimationMetadataSection(
        List<int[]> frames,
        int frameWidth,
        int frameHeight,
        int frameTime,
        boolean interpolate
    ) {
        this.frames = frames;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameTime = frameTime;
        this.interpolate = interpolate;
    }

    public int getFrameHeight() {
        return this.frameHeight;
    }

    public int getFrameWidth() {
        return this.frameWidth;
    }

    public int getFrameCount() {
        return this.frames.size();
    }

    public int getFrameTime() {
        return this.frameTime;
    }

    public boolean isInterpolate() {
        return this.interpolate;
    }

    public int getFrameIndex(int frame) {
        return this.frames.get(frame)[0];
    }

    public int getFrameTimeSingle(int frame) {
        int time = this.frames.get(frame)[1];
        return time == -1 ? this.frameTime : time;
    }
}
