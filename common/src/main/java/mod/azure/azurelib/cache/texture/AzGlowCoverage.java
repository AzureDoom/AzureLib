package mod.azure.azurelib.cache.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mod.azure.azurelib.render.AzBufferSource;

/**
 * Which parts of a glowmask texture actually glow, so the glow layer can skip quads whose UV area is fully transparent
 * in the mask. Those quads draw nothing, but would otherwise still be replayed, depth-sorted and uploaded every frame.
 * <p>
 * Built once when the glowmask loads, as a summed-area table over the mask's non-transparent pixels, so testing a
 * quad's UV rectangle is four array reads. Large textures are reduced to a grid of at most {@value #MAX_CELLS} cells
 * per side first; a cell counts as glowing if any pixel in it does, so the reduction can keep extra quads but never
 * drops one that glows.
 * <p>
 * Animated textures get no coverage (their frames would all have to be considered), and the glow layer then mirrors the
 * whole model as before.
 */
public final class AzGlowCoverage implements AzBufferSource.QuadFilter {

    private static final int MAX_CELLS = 512;

    private static final float EDGE_EPSILON = 1.0e-3f;

    private static final Map<Identifier, AzGlowCoverage> BY_TEXTURE = new ConcurrentHashMap<>();

    public static final AzGlowCoverage NOTHING = new AzGlowCoverage(1, 1, 0, 1, new int[4]);

    private final int width;

    private final int height;

    private final int shift;

    private final int cols;

    private final int[] table;

    private AzGlowCoverage(int width, int height, int shift, int cols, int[] table) {
        this.width = width;
        this.height = height;
        this.shift = shift;
        this.cols = cols;
        this.table = table;
    }

    /** Coverage for a loaded glowmask texture, or {@code null} if none is known (not loaded yet, or animated). */
    public static @Nullable AzGlowCoverage get(Identifier glowTexture) {
        return BY_TEXTURE.get(glowTexture);
    }

    static void register(Identifier glowTexture, NativeImage mask) {
        BY_TEXTURE.put(glowTexture, fromMask(mask));
    }

    static void registerNothing(Identifier glowTexture) {
        BY_TEXTURE.put(glowTexture, NOTHING);
    }

    static void unregister(Identifier glowTexture) {
        BY_TEXTURE.remove(glowTexture);
    }

    static AzGlowCoverage fromMask(NativeImage mask) {
        var width = mask.getWidth();
        var height = mask.getHeight();
        var shift = 0;

        while ((ceilShift(width, shift) > MAX_CELLS) || (ceilShift(height, shift) > MAX_CELLS)) {
            shift++;
        }

        var cols = ceilShift(width, shift);
        var rows = ceilShift(height, shift);
        var glowing = new boolean[cols * rows];
        var any = false;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((mask.getPixel(x, y) >>> 24) != 0) {
                    glowing[(y >> shift) * cols + (x >> shift)] = true;
                    any = true;
                }
            }
        }

        if (!any) {
            return NOTHING;
        }

        var stride = cols + 1;
        var table = new int[stride * (rows + 1)];

        for (int row = 0; row < rows; row++) {
            var rowSum = 0;

            for (int col = 0; col < cols; col++) {
                rowSum += glowing[row * cols + col] ? 1 : 0;
                table[(row + 1) * stride + col + 1] = table[row * stride + col + 1] + rowSum;
            }
        }

        return new AzGlowCoverage(width, height, shift, cols, table);
    }

    private static int ceilShift(int value, int shift) {
        return (value + (1 << shift) - 1) >> shift;
    }

    public boolean isNothing() {
        return this == NOTHING;
    }

    /**
     * Whether any glowing pixel lies inside the given UV rectangle (normalized texture coordinates). UVs outside [0, 1]
     * wrap around the texture, so those quads are always kept.
     */
    @Override
    public boolean test(float minU, float minV, float maxU, float maxV) {
        if (this == NOTHING) {
            return false;
        }

        if (minU < 0f || minV < 0f || maxU > 1f || maxV > 1f) {
            return true;
        }

        var x0 = (int) Math.floor(minU * width + EDGE_EPSILON);
        var x1 = (int) Math.ceil(maxU * width - EDGE_EPSILON);
        var y0 = (int) Math.floor(minV * height + EDGE_EPSILON);
        var y1 = (int) Math.ceil(maxV * height - EDGE_EPSILON);

        x0 = Math.clamp(x0, 0, width - 1);
        y0 = Math.clamp(y0, 0, height - 1);
        x1 = Math.clamp(x1, x0 + 1, width);
        y1 = Math.clamp(y1, y0 + 1, height);

        var col0 = x0 >> shift;
        var col1 = ((x1 - 1) >> shift) + 1;
        var row0 = y0 >> shift;
        var row1 = ((y1 - 1) >> shift) + 1;
        var stride = cols + 1;

        var sum = table[row1 * stride + col1] - table[row0 * stride + col1] - table[row1 * stride + col0]
            + table[row0 * stride + col0];
        return sum > 0;
    }
}
