package mod.azure.azurelib.rewrite.animation.controller.keyframe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * Stores a triplet of {@link AzKeyframe Keyframes} in an ordered stack.
 */
public class AzKeyframeStack<T extends AzKeyframe<?>> {

    private final List<T> xKeyframes;

    private final List<T> yKeyframes;

    private final List<T> zKeyframes;

    public AzKeyframeStack() {
        this(new ObjectArrayList<>(), new ObjectArrayList<>(), new ObjectArrayList<>());
    }

    public AzKeyframeStack(List<T> xKeyframes, List<T> yKeyframes, List<T> zKeyframes) {
        this.xKeyframes = xKeyframes;
        this.yKeyframes = yKeyframes;
        this.zKeyframes = zKeyframes;
    }

    public static <F extends AzKeyframe<?>> AzKeyframeStack<F> from(AzKeyframeStack<F> otherStack) {
        return new AzKeyframeStack<>(otherStack.xKeyframes(), otherStack.yKeyframes(), otherStack.zKeyframes());
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

    public List<T> xKeyframes() {
        return xKeyframes;
    }

    public List<T> yKeyframes() {
        return yKeyframes;
    }

    public List<T> zKeyframes() {
        return zKeyframes;
    }

    @Override
    public String toString() {
        return "AzKeyframeStack{" +
            "xKeyframes=" + xKeyframes +
            ", yKeyframes=" + yKeyframes +
            ", zKeyframes=" + zKeyframes +
            '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        AzKeyframeStack<?> that = (AzKeyframeStack<?>) obj;

        if (!xKeyframes.equals(that.xKeyframes)) {
            return false;
        }
        if (!yKeyframes.equals(that.yKeyframes)) {
            return false;
        }
        return zKeyframes.equals(that.zKeyframes);
    }

    @Override
    public int hashCode() {
        int result = xKeyframes.hashCode();
        result = 31 * result + yKeyframes.hashCode();
        result = 31 * result + zKeyframes.hashCode();
        return result;
    }
}
