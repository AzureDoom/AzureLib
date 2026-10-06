package mod.azure.azurelib.common.animation.controller.keyframe;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import mod.azure.azurelib.common.animation.easing.AzEasingType;
import mod.azure.azurelib.common.animation.easing.AzEasingUtil;
import mod.azure.azurelib.common.animation.primitive.AzBakedAnimation;
import mod.azure.azurelib.core.math.Constant;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.object.Axis;

/**
 * AzAbstractKeyframeExecutor is a base class designed to handle animations and transitions between keyframes in a
 * generic and reusable fashion. It provides the foundational logic for determining the current state of an animation
 * based on the tick time and computing the animation's required values.
 * <p>
 * Keyframes are sampled from each stack's baked {@link AzKeyframeChannel}s. Every executor keeps a keyframe cursor per
 * bone channel ({@code bone x transform x axis}, flattened into one {@code int[]}) holding the keyframe index it
 * sampled last time. Playback moves through keyframes a little at a time, so the next lookup usually starts on the
 * right keyframe or one away from it, in either direction. Cursors are only a starting point for the lookup: a stale or
 * shared cursor costs a binary search, never a wrong result.
 * </p>
 */
public class AzAbstractKeyframeExecutor {

    /** Channels per bone: three transforms (rotation, position, scale) times three axes. */
    protected static final int CHANNELS_PER_BONE = 9;

    protected static final int ROTATION = 0;

    protected static final int POSITION = 1;

    protected static final int SCALE = 2;

    private static final int[] NO_CURSORS = new int[0];

    /** Stand-in for a missing axis, matching what the previous implementation produced for an empty keyframe list. */
    private static final AzKeyframe<IValue> EMPTY_KEYFRAME = new AzKeyframe<>(0, () -> 0, () -> 0);

    private final AzKeyframeLocation<AzKeyframe<IValue>> scratchLocation = new AzKeyframeLocation<>(EMPTY_KEYFRAME, 0);

    /** Scratch point for {@link #sampleValue}; never handed out. */
    private final AzAnimationPoint scratchPoint = new AzAnimationPoint();

    private int[] keyframeCursors = NO_CURSORS;

    @Nullable
    private AzBakedAnimation cursorAnimation;

    protected AzAbstractKeyframeExecutor() {}

    /**
     * Returns the keyframe cursors for {@code animation}, laid out as {@code boneIndex * 9 + transform * 3 + axis}.
     * They carry over from frame to frame while the same animation keeps playing and are reset when a different one
     * starts.
     */
    protected final int[] prepareKeyframeCursors(AzBakedAnimation animation) {
        if (animation != cursorAnimation) {
            var size = animation.boneAnimations().length * CHANNELS_PER_BONE;

            if (keyframeCursors.length < size) {
                keyframeCursors = new int[size];
            } else {
                Arrays.fill(keyframeCursors, 0, size, 0);
            }

            cursorAnimation = animation;
        }

        return keyframeCursors;
    }

    /**
     * Forces the cursors to be reset the next time {@link #prepareKeyframeCursors} is called. Never needed for
     * correctness; only useful after a large jump in the timeline to skip the first lookup's binary search.
     */
    protected final void resetKeyframeCursors() {
        cursorAnimation = null;
    }

    protected static int cursorIndex(int boneIndex, int transform, int axis) {
        return boneIndex * CHANNELS_PER_BONE + transform * 3 + axis;
    }

    /**
     * Finds the keyframe for {@code tick} starting from the cursor at {@code cursorIndex}, and moves the cursor there.
     *
     * @return the keyframe index, or {@code -1} if the channel is empty
     */
    protected final int locateKeyframe(AzKeyframeChannel channel, double tick, int[] cursors, int cursorIndex) {
        var index = channel.locate(tick, cursors[cursorIndex]);

        if (index >= 0) {
            cursors[cursorIndex] = index;
        }

        return index;
    }

    /**
     * Samples one channel at {@code tick} and writes the result straight into {@code queue}'s slot for it, without
     * creating an {@link AzAnimationPoint}.
     *
     * @param transform {@link #ROTATION}, {@link #POSITION} or {@link #SCALE}
     */
    protected final void writeChannel(
        AzBoneAnimationQueue queue,
        int transform,
        Axis axis,
        AzKeyframeChannel channel,
        double tick,
        int[] cursors,
        int cursorIndex
    ) {
        var slot = transform * 3 + axis.ordinal();
        var index = locateKeyframe(channel, tick, cursors, cursorIndex);

        if (index < 0) {
            queue.write(slot, EMPTY_KEYFRAME, 0, 0, 0, 0);
            return;
        }

        var frame = channel.frame(index);
        var isRotation = transform == ROTATION;

        queue.write(
            slot,
            frame,
            tick - channel.startTime(index),
            frame.length(),
            readValue(frame.startValue(), isRotation, axis),
            readValue(frame.endValue(), isRotation, axis),
            channel.transformer(index)
        );
    }

    /**
     * Returns the value one channel has at {@code tick}, eased exactly as it would be when played, so a transition ends
     * on the same pose the animation then starts from.
     *
     * @param easingOverride the controller-wide easing override, or {@code null}
     */
    protected final double sampleValue(
        AzKeyframeChannel channel,
        int transform,
        Axis axis,
        double tick,
        int[] cursors,
        int cursorIndex,
        @Nullable AzEasingType easingOverride
    ) {
        var index = locateKeyframe(channel, tick, cursors, cursorIndex);

        if (index < 0) {
            return 0;
        }

        var frame = channel.frame(index);
        var isRotation = transform == ROTATION;

        scratchPoint.set(
            frame,
            tick - channel.startTime(index),
            frame.length(),
            readValue(frame.startValue(), isRotation, axis),
            readValue(frame.endValue(), isRotation, axis),
            channel.transformer(index)
        );

        return AzEasingUtil.lerpWithOverride(scratchPoint, easingOverride);
    }

    /**
     * Reads a keyframe value. Constant rotations are converted to radians (and flipped on X/Y) when the animation is
     * baked; anything else is converted here.
     */
    protected static double readValue(IValue value, boolean isRotation, Axis axis) {
        var result = value.get();

        if (isRotation && !(value instanceof Constant)) {
            result = Math.toRadians(result);

            if (axis == Axis.X || axis == Axis.Y) {
                result *= -1;
            }
        }

        return result;
    }

    /**
     * Convert a list of keyframes to an {@link AzAnimationPoint} at the given tick.
     *
     * @deprecated No longer used by AzureLib, which samples {@link AzKeyframeChannel}s through {@link #writeChannel} /
     *             {@link #sampleValue} instead. Kept for compatibility; overriding it no longer affects how animations
     *             are sampled.
     */
    @Deprecated
    protected AzAnimationPoint getAnimationPointAtTick(
        List<AzKeyframe<IValue>> frames,
        double tick,
        boolean isRotation,
        Axis axis
    ) {
        var location = getCurrentKeyframeLocation(frames, tick, scratchLocation);
        var currentFrame = location.keyframe();

        return new AzAnimationPoint(
            currentFrame,
            location.startTick(),
            currentFrame.length(),
            readValue(currentFrame.startValue(), isRotation, axis),
            readValue(currentFrame.endValue(), isRotation, axis)
        );
    }

    /**
     * Returns the {@link AzKeyframe} relevant to the current tick time, written into {@code scratch}. Its start tick is
     * the time elapsed since that keyframe started.
     *
     * @deprecated No longer used by AzureLib; see {@link AzKeyframeChannel#locate(double, int)}. Kept for
     *             compatibility, and now a single allocation-free pass over the list.
     */
    @Deprecated
    protected AzKeyframeLocation<AzKeyframe<IValue>> getCurrentKeyframeLocation(
        List<AzKeyframe<IValue>> frames,
        double ageInTicks,
        AzKeyframeLocation<AzKeyframe<IValue>> scratch
    ) {
        var size = frames.size();

        if (size == 0) {
            return scratch.set(EMPTY_KEYFRAME, 0);
        }

        var total = 0D;

        for (var i = 0; i < size; i++) {
            var frame = frames.get(i);
            var start = total;
            total += frame.length();

            if (total > ageInTicks || i == size - 1) {
                return scratch.set(frame, ageInTicks - start);
            }
        }

        return scratch;
    }

    /**
     * Legacy overload retained for any subclass overrides. Delegates to the scratch-based version.
     *
     * @deprecated See {@link #getCurrentKeyframeLocation(List, double, AzKeyframeLocation)}.
     */
    @Deprecated
    protected AzKeyframeLocation<AzKeyframe<IValue>> getCurrentKeyframeLocation(
        List<AzKeyframe<IValue>> frames,
        double ageInTicks
    ) {
        return getCurrentKeyframeLocation(frames, ageInTicks, scratchLocation);
    }
}
