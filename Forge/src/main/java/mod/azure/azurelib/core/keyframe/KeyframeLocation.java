/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe;

@Deprecated()
public class KeyframeLocation<T extends Keyframe<?>> {
    T keyframe;
    double startTick;

    /**
     * A named pair object that stores a {@link Keyframe} and a double representing a temporally placed {@code Keyframe}
     *
     * @param keyframe  The {@code Keyframe} at the tick time
     * @param startTick The animation tick time at the start of this {@code Keyframe}
     */
    public KeyframeLocation(T keyframe, double startTick) {
        this.keyframe = keyframe;
        this.startTick = startTick;
    }

    public T keyframe() {
        return keyframe;
    }

    public double startTick() {
        return startTick;
    }
}
