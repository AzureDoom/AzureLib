package mod.azure.azurelib.render;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayDeque;
import java.util.ArrayList;

import mod.azure.azurelib.AzureLib;

/**
 * Frame-scoped pool for the float and int arrays that {@link AzBufferSource} records vertices into.
 * <p>
 * Recordings are written during extraction and read back during the same frame's render pass, after which nothing
 * references them. So instead of allocating fresh arrays for every animatable every frame, arrays are handed out from
 * per-size free lists and all of them are returned in one go by {@link #endFrame()}, which runs once the frame has been
 * fully rendered (see {@code GameRendererMixin_AzFrameArena}). Arrays come in power-of-two sizes and are not cleared,
 * since callers only read what they wrote.
 * <p>
 * Safety valves: only the render thread uses the pool (other threads get plain arrays), a single frame can hold at most
 * {@value #MAX_TRACKED_BYTES} bytes of pooled arrays before further requests fall back to plain allocation, and if that
 * limit is ever reached before the first {@link #endFrame()} call, the frame-end hook is assumed missing and pooling is
 * switched off for good.
 */
public final class AzFrameArena {

    /** Smallest pooled array: 2^9 = 512 elements. */
    private static final int MIN_CLASS = 9;

    /** Largest pooled array: 2^24 = 16M elements. Larger requests are allocated directly. */
    private static final int MAX_CLASS = 24;

    private static final int CLASSES = MAX_CLASS + 1;

    private static final long MAX_TRACKED_BYTES = 256L * 1024 * 1024;

    /** Frames between steps of decay for each size class's retained-array peak. */
    private static final int DECAY_INTERVAL = 120;

    private static final float[] EMPTY_FLOATS = new float[0];

    private static final int[] EMPTY_INTS = new int[0];

    @SuppressWarnings("unchecked")
    private static final ArrayDeque<float[]>[] FREE_FLOATS = new ArrayDeque[CLASSES];

    @SuppressWarnings("unchecked")
    private static final ArrayDeque<int[]>[] FREE_INTS = new ArrayDeque[CLASSES];

    private static final int[] USED_FLOATS = new int[CLASSES];

    private static final int[] USED_INTS = new int[CLASSES];

    private static final int[] PEAK_FLOATS = new int[CLASSES];

    private static final int[] PEAK_INTS = new int[CLASSES];

    private static final ArrayList<float[]> IN_USE_FLOATS = new ArrayList<>();

    private static final ArrayList<int[]> IN_USE_INTS = new ArrayList<>();

    private static long trackedBytes;

    private static boolean frameEndSeen;

    private static boolean disabled;

    private static long frames;

    static {
        for (int i = 0; i < CLASSES; i++) {
            FREE_FLOATS[i] = new ArrayDeque<>();
            FREE_INTS[i] = new ArrayDeque<>();
        }
    }

    private AzFrameArena() {}

    /** A float array of at least {@code minLength} elements, valid until the end of the current frame. */
    public static float[] floats(int minLength) {
        if (minLength <= 0) {
            return EMPTY_FLOATS;
        }

        var sizeClass = sizeClass(minLength);

        if (sizeClass < 0 || !canPool(4L << sizeClass)) {
            return new float[minLength];
        }

        var array = FREE_FLOATS[sizeClass].pollLast();

        if (array == null) {
            array = new float[1 << sizeClass];
        }

        IN_USE_FLOATS.add(array);
        USED_FLOATS[sizeClass]++;
        trackedBytes += 4L << sizeClass;
        return array;
    }

    /** An int array of at least {@code minLength} elements, valid until the end of the current frame. */
    public static int[] ints(int minLength) {
        if (minLength <= 0) {
            return EMPTY_INTS;
        }

        var sizeClass = sizeClass(minLength);

        if (sizeClass < 0 || !canPool(4L << sizeClass)) {
            return new int[minLength];
        }

        var array = FREE_INTS[sizeClass].pollLast();

        if (array == null) {
            array = new int[1 << sizeClass];
        }

        IN_USE_INTS.add(array);
        USED_INTS[sizeClass]++;
        trackedBytes += 4L << sizeClass;
        return array;
    }

    /**
     * Returns every array handed out this frame to the pool. Must only run once nothing will read this frame's
     * recordings again, i.e. after the frame's render pass.
     */
    public static void endFrame() {
        if (!RenderSystem.isOnRenderThread()) {
            return;
        }

        frameEndSeen = true;
        frames++;

        for (var array : IN_USE_FLOATS) {
            FREE_FLOATS[Integer.numberOfTrailingZeros(array.length)].addLast(array);
        }

        for (var array : IN_USE_INTS) {
            FREE_INTS[Integer.numberOfTrailingZeros(array.length)].addLast(array);
        }

        IN_USE_FLOATS.clear();
        IN_USE_INTS.clear();
        trackedBytes = 0;

        var decay = frames % DECAY_INTERVAL == 0;

        for (int sizeClass = MIN_CLASS; sizeClass < CLASSES; sizeClass++) {
            PEAK_FLOATS[sizeClass] = retain(
                FREE_FLOATS[sizeClass],
                USED_FLOATS[sizeClass],
                PEAK_FLOATS[sizeClass],
                decay
            );
            PEAK_INTS[sizeClass] = retain(FREE_INTS[sizeClass], USED_INTS[sizeClass], PEAK_INTS[sizeClass], decay);
            USED_FLOATS[sizeClass] = 0;
            USED_INTS[sizeClass] = 0;
        }
    }

    /**
     * Keeps as many free arrays as the recent peak usage of their size class and lets the rest be collected, so a
     * one-off busy frame doesn't pin its memory forever. The peak decays by an eighth every {@value #DECAY_INTERVAL}
     * frames.
     */
    private static int retain(ArrayDeque<?> free, int used, int peak, boolean decay) {
        if (decay) {
            peak -= Math.max(1, peak >> 3);
        }

        peak = Math.max(peak, used);

        while (free.size() > peak) {
            free.pollFirst();
        }

        return peak;
    }

    private static boolean canPool(long bytes) {
        if (disabled || !RenderSystem.isOnRenderThread()) {
            return false;
        }

        if (trackedBytes + bytes > MAX_TRACKED_BYTES) {
            if (!frameEndSeen) {
                disabled = true;
                IN_USE_FLOATS.clear();
                IN_USE_INTS.clear();
                trackedBytes = 0;
                AzureLib.LOGGER.warn(
                    "AzFrameArena never saw a frame end; vertex recording pooling is disabled for this session."
                );
            }

            return false;
        }

        return true;
    }

    private static int sizeClass(int minLength) {
        var sizeClass = Math.max(MIN_CLASS, 32 - Integer.numberOfLeadingZeros(minLength - 1));
        return sizeClass > MAX_CLASS ? -1 : sizeClass;
    }
}
