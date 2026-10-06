package mod.azure.azurelib.common.animation.controller.keyframe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Objects;

/**
 * Stores a triplet of {@link AzKeyframe Keyframes} in an ordered stack, along with a baked {@link AzKeyframeChannel}
 * per axis that the animation runtime samples from.
 */
@SuppressWarnings("unused")
public final class AzKeyframeStack<T extends AzKeyframe<?>> {

    private final List<T> xKeyframes;

    private final List<T> yKeyframes;

    private final List<T> zKeyframes;

    private final AzKeyframeChannel xChannel;

    private final AzKeyframeChannel yChannel;

    private final AzKeyframeChannel zChannel;

    public AzKeyframeStack() {
        this(new ObjectArrayList<>(), new ObjectArrayList<>(), new ObjectArrayList<>());
    }

    public AzKeyframeStack(List<T> xKeyframes, List<T> yKeyframes, List<T> zKeyframes) {
        this(
            xKeyframes,
            yKeyframes,
            zKeyframes,
            new AzKeyframeChannel(xKeyframes),
            new AzKeyframeChannel(yKeyframes),
            new AzKeyframeChannel(zKeyframes)
        );
    }

    private AzKeyframeStack(
        List<T> xKeyframes,
        List<T> yKeyframes,
        List<T> zKeyframes,
        AzKeyframeChannel xChannel,
        AzKeyframeChannel yChannel,
        AzKeyframeChannel zChannel
    ) {
        this.xKeyframes = xKeyframes;
        this.yKeyframes = yKeyframes;
        this.zKeyframes = zKeyframes;
        this.xChannel = xChannel;
        this.yChannel = yChannel;
        this.zChannel = zChannel;
    }

    public static <F extends AzKeyframe<?>> AzKeyframeStack<F> from(AzKeyframeStack<F> otherStack) {
        return new AzKeyframeStack<>(
            otherStack.xKeyframes,
            otherStack.yKeyframes,
            otherStack.zKeyframes,
            otherStack.xChannel,
            otherStack.yChannel,
            otherStack.zChannel
        );
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

    /**
     * @return the baked channel for {@link #xKeyframes()}
     */
    public AzKeyframeChannel xChannel() {
        return xChannel;
    }

    /**
     * @return the baked channel for {@link #yKeyframes()}
     */
    public AzKeyframeChannel yChannel() {
        return yChannel;
    }

    /**
     * @return the baked channel for {@link #zKeyframes()}
     */
    public AzKeyframeChannel zChannel() {
        return zChannel;
    }

    public double getLastKeyframeTime() {
        return Math.max(xChannel.totalLength(), Math.max(yChannel.totalLength(), zChannel.totalLength()));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AzKeyframeStack<?> other)) {
            return false;
        }

        return Objects.equals(xKeyframes, other.xKeyframes) && Objects.equals(yKeyframes, other.yKeyframes)
            && Objects.equals(zKeyframes, other.zKeyframes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(xKeyframes);
        result = 31 * result + Objects.hashCode(yKeyframes);
        result = 31 * result + Objects.hashCode(zKeyframes);
        return result;
    }

    @Override
    public String toString() {
        return "AzKeyframeStack[xKeyframes=" + xKeyframes + ", yKeyframes=" + yKeyframes + ", zKeyframes=" + zKeyframes
            + "]";
    }
}
