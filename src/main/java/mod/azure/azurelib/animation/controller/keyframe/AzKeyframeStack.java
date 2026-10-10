package mod.azure.azurelib.animation.controller.keyframe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Stores a triplet of {@link AzKeyframe Keyframes} in an ordered stack, along with a baked {@link AzKeyframeChannel}
 * per axis that the animation runtime samples from.
 * <p>
 * Previously a record. It is now a class so it can hold the baked channels next to the keyframe lists; its
 * constructors, accessors, {@code equals} and {@code hashCode} behave the same as before.
 * </p>
 * <p>
 * Channels are immutable snapshots of the keyframe lists. A stack built from lists bakes them immediately; a stack made
 * with the no-argument constructor bakes them the first time they're needed, so its lists can still be filled before
 * then. Once a stack's channels are baked, changing its lists has no effect on playback.
 * </p>
 */
@SuppressWarnings("unused")
public final class AzKeyframeStack<T extends AzKeyframe<?>> {

    private final List<T> xKeyframes;

    private final List<T> yKeyframes;

    private final List<T> zKeyframes;

    /*
     * Baked on construction, or on first use for the no-argument constructor. A racing first use bakes twice and keeps
     * either result; channels are immutable, so both are equivalent and safely published.
     */
    @Nullable
    private AzKeyframeChannel xChannel;

    @Nullable
    private AzKeyframeChannel yChannel;

    @Nullable
    private AzKeyframeChannel zChannel;

    /**
     * Creates a stack with empty, mutable keyframe lists. Fill them before the stack is first animated.
     */
    public AzKeyframeStack() {
        this.xKeyframes = new ArrayList<>();
        this.yKeyframes = new ArrayList<>();
        this.zKeyframes = new ArrayList<>();
    }

    /**
     * Creates a stack from complete keyframe lists, baking them into channels right away.
     */
    public AzKeyframeStack(List<T> xKeyframes, List<T> yKeyframes, List<T> zKeyframes) {
        this(xKeyframes, yKeyframes, zKeyframes, bake(xKeyframes), bake(yKeyframes), bake(zKeyframes));
    }

    private AzKeyframeStack(
        List<T> xKeyframes,
        List<T> yKeyframes,
        List<T> zKeyframes,
        @Nullable AzKeyframeChannel xChannel,
        @Nullable AzKeyframeChannel yChannel,
        @Nullable AzKeyframeChannel zChannel
    ) {
        this.xKeyframes = xKeyframes;
        this.yKeyframes = yKeyframes;
        this.zKeyframes = zKeyframes;
        this.xChannel = xChannel;
        this.yChannel = yChannel;
        this.zChannel = zChannel;
    }

    public static <F extends AzKeyframe<?>> AzKeyframeStack<F> from(AzKeyframeStack<F> otherStack) {
        // Same lists, so already-baked channels can be shared as well.
        return new AzKeyframeStack<>(
            otherStack.xKeyframes,
            otherStack.yKeyframes,
            otherStack.zKeyframes,
            otherStack.xChannel,
            otherStack.yChannel,
            otherStack.zChannel
        );
    }

    private static AzKeyframeChannel bake(List<? extends AzKeyframe<?>> keyframes) {
        return keyframes.isEmpty() ? AzKeyframeChannel.EMPTY : new AzKeyframeChannel(keyframes);
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
        AzKeyframeChannel channel = xChannel;

        if (channel == null)
            xChannel = channel = bake(xKeyframes);

        return channel;
    }

    /**
     * @return the baked channel for {@link #yKeyframes()}
     */
    public AzKeyframeChannel yChannel() {
        AzKeyframeChannel channel = yChannel;

        if (channel == null)
            yChannel = channel = bake(yKeyframes);

        return channel;
    }

    /**
     * @return the baked channel for {@link #zKeyframes()}
     */
    public AzKeyframeChannel zChannel() {
        AzKeyframeChannel channel = zChannel;

        if (channel == null)
            zChannel = channel = bake(zKeyframes);

        return channel;
    }

    public double getLastKeyframeTime() {
        return Math.max(xChannel().totalLength(), Math.max(yChannel().totalLength(), zChannel().totalLength()));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AzKeyframeStack)) {
            return false;
        }

        return Objects.equals(xKeyframes, ((AzKeyframeStack<?>) obj).xKeyframes) && Objects.equals(
            yKeyframes,
            ((AzKeyframeStack<?>) obj).yKeyframes
        )
            && Objects.equals(zKeyframes, ((AzKeyframeStack<?>) obj).zKeyframes);
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
