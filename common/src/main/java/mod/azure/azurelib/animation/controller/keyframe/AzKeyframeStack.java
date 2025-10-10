package mod.azure.azurelib.animation.controller.keyframe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * Stores a triplet of {@link AzKeyframe Keyframes} in an ordered stack
 */
public record AzKeyframeStack<T extends AzKeyframe<?>>(
    List<T> xKeyframes,
    List<T> yKeyframes,
    List<T> zKeyframes
) {

    public AzKeyframeStack() {
        this(new ObjectArrayList<>(), new ObjectArrayList<>(), new ObjectArrayList<>());
    }

    public static <F extends AzKeyframe<?>> AzKeyframeStack<F> from(AzKeyframeStack<F> otherStack) {
        return new AzKeyframeStack<>(otherStack.xKeyframes, otherStack.yKeyframes, otherStack.zKeyframes);
    }

    public double getLastKeyframeTime() {
        double xTime = 0;
        double yTime = 0;
        double zTime = 0;

        for (T frame : xKeyframes()) {
            xTime += frame.length();
        }

        for (T frame : yKeyframes()) {
            yTime += frame.length();
        }

        for (T frame : zKeyframes()) {
            zTime += frame.length();
        }

        return Math.max(xTime, Math.max(yTime, zTime));
    }
}
