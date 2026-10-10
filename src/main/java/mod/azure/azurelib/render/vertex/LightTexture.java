package mod.azure.azurelib.render.vertex;

/**
 * Packed lightmap coordinate helpers, mirroring the 1.18 {@code net.minecraft.client.renderer.LightTexture}. The
 * packing (block light in the low 16 bits, sky light in the high 16 bits, each shifted left by 4) is identical to the
 * value returned by 1.12.2's {@code Entity#getBrightnessForRender()} and {@code World#getCombinedLight}.
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
}
