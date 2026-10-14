package mod.azure.azurelib.render.vertex;

/**
 * Packed lightmap coordinate helpers, mirroring the 1.18 {@code net.minecraft.client.renderer.LightTexture}. The
 * packing (block light in the low 16 bits, sky light in the high 16 bits, each shifted left by 4) is identical to the
 * value 1.18 uses. Beta has no lightmap, so {@link #fromBrightness(float)} packs Beta's 0..1 brightness into the same
 * format and {@link AzBufferSource} folds it back into the vertex colour.
 */
public final class LightTexture {

    public static final int FULL_BRIGHT = 0xF000F0;

    public static final int FULL_SKY = 0xF00000;

    public static final int FULL_BLOCK = 0xF0;

    private LightTexture() {
        throw new UnsupportedOperationException();
    }

    public static int pack(int blockLight, int skyLight) {
        return blockLight << 4 | skyLight << 20;
    }

    public static int block(int packedLight) {
        return packedLight >> 4 & 0xFFFF;
    }

    public static int sky(int packedLight) {
        return packedLight >> 20 & 0xFFFF;
    }

    /**
     * Packs a Beta brightness value (0..1, e.g. {@code Entity#getBrightnessAtEyes} or {@code World#getBrightness}) into
     * a packed light value with equal block and sky levels.
     */
    public static int fromBrightness(float brightness) {
        int level = Math.round(Math.max(0.0F, Math.min(1.0F, brightness)) * 15.0F);
        return pack(level, level);
    }
}
