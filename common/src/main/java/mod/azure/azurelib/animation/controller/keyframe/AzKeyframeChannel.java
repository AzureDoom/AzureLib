package mod.azure.azurelib.animation.controller.keyframe;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import mod.azure.azurelib.AzureLib;

/**
 * A baked, immutable view of a single axis of keyframes (e.g. the X rotation keyframes of one bone).
 * <p>
 * Keyframes only store their own {@link AzKeyframe#length() length}, so finding the keyframe for a given tick used to
 * mean summing lengths from the start of the list every frame. A channel does that once: it copies the keyframes into
 * an array and precomputes the time each keyframe ends at, so a lookup is either a cached-index check
 * ({@link #locate(double, int)}) or a binary search ({@link #search(double)}), with no allocation in either case.
 * </p>
 * <p>
 * Each keyframe's easing curve is also resolved here when it can be known ahead of time (see
 * {@link mod.azure.azurelib.animation.easing.AzEasingType#resolveTransformer}), so playback doesn't have to look it up.
 * </p>
 * <p>
 * A channel is a snapshot of the keyframes it was built from. Changing that list afterward does not change the channel.
 * </p>
 */
@SuppressWarnings("unused")
public final class AzKeyframeChannel {

    /** A channel with no keyframes. */
    public static final AzKeyframeChannel EMPTY = new AzKeyframeChannel(List.of());

    /**
     * How many keyframes a cached lookup walks before giving up and binary searching instead. Normal playback moves at
     * most one keyframe per frame, so this only matters for jumps (looping back to the start, seeking).
     */
    private static final int SCAN_LIMIT = 4;

    private final AzKeyframe<?>[] frames;

    /** {@code endTimes[i]} is the tick keyframe {@code i} ends at; it starts at {@code endTimes[i - 1]} (or 0). */
    private final double[] endTimes;

    /** {@code transformers[i]} is keyframe {@code i}'s baked easing curve, or {@code null} if resolved at playback. */
    private final Double2DoubleFunction[] transformers;

    public AzKeyframeChannel(List<? extends AzKeyframe<?>> keyframes) {
        var size = keyframes.size();

        this.frames = new AzKeyframe<?>[size];
        this.endTimes = new double[size];
        this.transformers = new Double2DoubleFunction[size];

        var total = 0D;

        for (var i = 0; i < size; i++) {
            var frame = keyframes.get(i);
            total += frame.length();
            this.frames[i] = frame;
            this.endTimes[i] = total;
            this.transformers[i] = resolveTransformer(frame);
        }
    }

    @Nullable
    private static Double2DoubleFunction resolveTransformer(AzKeyframe<?> frame) {
        var easingType = frame.easingType();

        if (easingType == null)
            return null;

        try {
            return easingType.resolveTransformer(frame);
        } catch (RuntimeException e) {
            // Leave it to playback, which reports the problem the same way it always has.
            AzureLib.LOGGER.debug("Couldn't bake easing curve for {}, resolving it at playback", easingType.name(), e);
            return null;
        }
    }

    public boolean isEmpty() {
        return frames.length == 0;
    }

    public int size() {
        return frames.length;
    }

    /**
     * @return the keyframe at {@code index}. Only valid for an index returned by {@link #locate} or {@link #search}.
     */
    public AzKeyframe<?> frame(int index) {
        return frames[index];
    }

    /**
     * @return the keyframe at {@code index}'s easing curve, resolved when the channel was built, or {@code null} if it
     *         has to be resolved at playback
     */
    @Nullable
    public Double2DoubleFunction transformer(int index) {
        return transformers[index];
    }

    /**
     * @return the tick the keyframe at {@code index} starts at
     */
    public double startTime(int index) {
        return index == 0 ? 0 : endTimes[index - 1];
    }

    /**
     * @return the tick the keyframe at {@code index} ends at
     */
    public double endTime(int index) {
        return endTimes[index];
    }

    /**
     * @return the combined length of every keyframe in this channel, in ticks
     */
    public double totalLength() {
        return frames.length == 0 ? 0 : endTimes[frames.length - 1];
    }

    /**
     * Finds the keyframe for {@code tick}, starting from {@code hint} (usually the index this channel returned last
     * frame). Animations sample nearby ticks frame to frame, so the hint is almost always already correct or one
     * keyframe away, in either direction, which makes forward, reversed and ping-pong playback effectively O(1). If the
     * hint is further off than a few keyframes, this falls back to {@link #search(double)}.
     * <p>
     * The result is the first keyframe that ends after {@code tick}, or the last keyframe if none do — the same rule
     * {@link #search(double)} uses, so the hint never changes which keyframe is returned.
     * </p>
     *
     * @param tick the tick being sampled
     * @param hint any index; out-of-range values are clamped
     * @return the index of the keyframe to sample, or {@code -1} if this channel is empty
     */
    public int locate(double tick, int hint) {
        var ends = endTimes;
        var last = ends.length - 1;

        if (last < 0) {
            return -1;
        }

        var index = hint < 0 ? 0 : Math.min(hint, last);

        if (index < last && ends[index] <= tick) {
            // The tick is past this keyframe; walk forward.
            for (var steps = 0; steps < SCAN_LIMIT; steps++) {
                index++;

                if (index == last || ends[index] > tick) {
                    return index;
                }
            }

            return search(tick);
        }

        if (index > 0 && ends[index - 1] > tick) {
            // The tick is before this keyframe; walk backward.
            for (var steps = 0; steps < SCAN_LIMIT; steps++) {
                index--;

                if (index == 0 || ends[index - 1] <= tick) {
                    return index;
                }
            }

            return search(tick);
        }

        return index;
    }

    /**
     * Binary searches for the keyframe for {@code tick}: the first keyframe that ends after {@code tick}, or the last
     * keyframe if none do.
     *
     * @return the index of the keyframe to sample, or {@code -1} if this channel is empty
     */
    public int search(double tick) {
        var ends = endTimes;
        var lo = 0;
        var hi = ends.length - 1;
        var result = hi;

        while (lo <= hi) {
            var mid = (lo + hi) >>> 1;

            if (ends[mid] > tick) {
                result = mid;
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }

        return result;
    }
}
