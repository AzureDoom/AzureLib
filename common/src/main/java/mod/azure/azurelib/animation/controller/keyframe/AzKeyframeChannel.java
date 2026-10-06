package mod.azure.azurelib.animation.controller.keyframe;

import java.util.List;

/**
 * A baked, array-backed view of a single axis of keyframes (e.g. the X rotation keyframes of one bone).
 * <p>
 * Keyframes only store their own {@link AzKeyframe#length() length}, so finding the keyframe for a given tick used to
 * mean summing lengths from the start of the list every frame. A channel does that once: it copies the keyframes into
 * an array and precomputes the time each keyframe ends at, so a lookup is either a cached-index check
 * ({@link #locate(double, int)}) or a binary search ({@link #search(double)}), with no allocation in either case.
 * </p>
 * <p>
 * Channels are created by {@link AzKeyframeStack} and are bound to the list they were baked from. If that list changes
 * size, the channel re-bakes itself on the next lookup. Changing a keyframe list in any other way after it has started
 * being animated is not supported.
 * </p>
 */
@SuppressWarnings("unused")
public final class AzKeyframeChannel {

    private static final AzKeyframe<?>[] NO_FRAMES = new AzKeyframe<?>[0];

    private static final double[] NO_TIMES = new double[0];

    private final List<? extends AzKeyframe<?>> source;

    private AzKeyframe<?>[] frames;

    private double[] endTimes;

    public AzKeyframeChannel(List<? extends AzKeyframe<?>> source) {
        this.source = source;
        bake();
    }

    private void bake() {
        var size = source.size();

        if (size == 0) {
            this.frames = NO_FRAMES;
            this.endTimes = NO_TIMES;
            return;
        }

        var bakedFrames = new AzKeyframe<?>[size];
        var bakedEndTimes = new double[size];
        var total = 0D;

        for (var i = 0; i < size; i++) {
            var frame = source.get(i);
            total += frame.length();
            bakedFrames[i] = frame;
            bakedEndTimes[i] = total;
        }

        this.frames = bakedFrames;
        this.endTimes = bakedEndTimes;
    }

    private void ensureBaked() {
        if (frames.length != source.size()) {
            bake();
        }
    }

    public boolean isEmpty() {
        return source.isEmpty();
    }

    public int size() {
        ensureBaked();
        return frames.length;
    }

    /**
     * @return the keyframe at {@code index}. Only valid for an index returned by {@link #locate} or {@link #search}.
     */
    public AzKeyframe<?> frame(int index) {
        return frames[index];
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
        ensureBaked();
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
        ensureBaked();

        var ends = endTimes;
        var last = ends.length - 1;

        if (last < 0) {
            return -1;
        }

        var index = hint < 0 ? 0 : Math.min(hint, last);

        if (index < last && ends[index] <= tick) {
            for (var steps = 0; steps < 4; steps++) {
                index++;

                if (index == last || ends[index] > tick) {
                    return index;
                }
            }

            return search(tick);
        }

        if (index > 0 && ends[index - 1] > tick) {
            for (var steps = 0; steps < 4; steps++) {
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
        ensureBaked();

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
