package mod.azure.azurelib.animation.blend;

import mod.azure.azurelib.animation.controller.AzBlendMode;
import net.minecraft.util.Mth;

import java.util.Arrays;

/**
 * The weight math behind blend spaces, usable on its own: how much each sample contributes for a given input, and how
 * those weights map onto layered controller weights.
 */
public final class AzBlendWeights {

    private AzBlendWeights() {}

    /**
     * 1D weights: the input is clamped to the samples' range and split linearly between the two samples around it.
     *
     * @param positions sample positions, ascending
     * @param input     the input value
     * @param dest      receives one weight per sample, summing to 1
     */
    public static void linear(double[] positions, double input, double[] dest) {
        int n = positions.length;
        Arrays.fill(dest, 0, n, 0);

        if (n == 0) {
            return;
        }

        if (n == 1 || input <= positions[0]) {
            dest[0] = 1;
            return;
        }

        if (input >= positions[n - 1]) {
            dest[n - 1] = 1;
            return;
        }

        for (int i = 0; i < n - 1; i++) {
            double from = positions[i];
            double to = positions[i + 1];

            if (input <= to) {
                double span = to - from;
                double t = span <= 0 ? 1 : (input - from) / span;
                dest[i] = 1 - t;
                dest[i + 1] = t;
                return;
            }
        }

        dest[n - 1] = 1;
    }

    /**
     * 2D weights by gradient band interpolation (Freeform Cartesian): every sample's influence falls off linearly
     * towards each other sample, so the input landing on a sample plays exactly that sample, and samples can be placed
     * anywhere. Outside the samples the nearest ones take over.
     *
     * @param xs   sample X positions
     * @param ys   sample Y positions
     * @param x    input X
     * @param y    input Y
     * @param dest receives one weight per sample, summing to 1
     */
    public static void gradientBand(double[] xs, double[] ys, double x, double y, double[] dest) {
        int n = xs.length;

        if (n == 0) {
            return;
        }

        double total = 0.0;

        for (int i = 0; i < n; i++) {
            double weight = 1.0;
            double px = x - xs[i];
            double py = y - ys[i];

            for (int j = 0; j < n && weight > 0; j++) {
                if (i == j) {
                    continue;
                }

                double ex = xs[j] - xs[i];
                double ey = ys[j] - ys[i];
                double lengthSq = ex * ex + ey * ey;

                if (lengthSq <= 0) {
                    continue;
                }

                double h = 1 - (px * ex + py * ey) / lengthSq;
                weight = Mth.clamp(Math.min(1, h), 0, weight);
            }

            dest[i] = weight;
            total += weight;
        }

        if (total > 0) {
            for (int i = 0; i < n; i++) {
                dest[i] /= total;
            }

            return;
        }

        // Can't happen with distinct samples, but fall back to the nearest one rather than to nothing.
        int nearest = 0;
        double best = Double.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            double dx = x - xs[i];
            double dy = y - ys[i];
            double d = dx * dx + dy * dy;
            dest[i] = 0;

            if (d < best) {
                best = d;
                nearest = i;
            }
        }

        dest[nearest] = 1;
    }

    /**
     * Turns sample weights into weights for controllers layered in sample order with {@link AzBlendMode#OVERRIDE
     * OVERRIDE}, so that together they produce
     * {@code (1 - overall) * underlying + overall * sum(weights[i] * sample[i])}. Each controller blends towards its
     * sample by its share of everything applied so far.
     *
     * @param weights sample weights, summing to 1
     * @param overall how strongly the whole blend applies over what is underneath, from 0 to 1
     * @param dest    receives one controller weight per sample
     */
    public static void layered(double[] weights, double overall, double[] dest) {
        double accumulated = 1 - overall;

        for (int i = 0; i < weights.length; i++) {
            double w = overall * weights[i];

            if (w <= 0) {
                dest[i] = 0;
                continue;
            }

            accumulated += w;
            dest[i] = w / accumulated;
        }
    }
}
