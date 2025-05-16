/*
 * Copyright (c) 2020. Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe;

@Deprecated()
public class KeyframeLocation<T extends Keyframe> {

    /**
     * The curent frame.
     */
    public T currentFrame;

    /**
     * This is the combined total time of all the previous keyframes
     */
    public double currentTick;

    /**
     * Instantiates a new Key frame location.
     *
     * @param keyframe  the current frame
     * @param startTick the current animation tick
     */
    public KeyframeLocation(T keyframe, double startTick) {
        this.currentFrame = keyframe;
        this.currentTick = startTick;
    }

    public T keyframe() {
        return currentFrame;
    }

    public double startTick() {
        return currentTick;
    }
}
