package mod.azure.azurelib.rewrite.animation.controller.keyframe;

import mod.azure.azurelib.core.keyframe.Keyframe;

/**
 * A named pair object that stores a {@link Keyframe} and a double representing a temporally placed {@code Keyframe}
 *
 * @param keyframe  The {@code Keyframe} at the tick time
 * @param startTick The animation tick time at the start of this {@code Keyframe}
 */
public record AzKeyframeLocation<T extends AzKeyframe<?>>(
    T keyframe,
    double startTick
) {}
