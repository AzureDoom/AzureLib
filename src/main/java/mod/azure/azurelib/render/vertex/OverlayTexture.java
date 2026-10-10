package mod.azure.azurelib.render.vertex;

/**
 * Packed "overlay" coordinates, mirroring the 1.18 {@code net.minecraft.client.renderer.texture.OverlayTexture}
 * helpers. Minecraft 1.12.2 has no overlay texture; {@link AzBufferSource} instead applies the equivalent red (hurt)
 * and white (flash) tint to the vertex colour.
 */
public final class OverlayTexture {

    public static final int NO_WHITE_U = 0;

    public static final int RED_OVERLAY_V = 3;

    public static final int WHITE_OVERLAY_V = 10;

    public static final int NO_OVERLAY = pack(NO_WHITE_U, WHITE_OVERLAY_V);

    private OverlayTexture() {
        throw new UnsupportedOperationException();
    }

    public static int u(float whiteOverlayProgress) {
        return (int) (whiteOverlayProgress * 15.0F);
    }

    public static int v(boolean hurt) {
        return hurt ? RED_OVERLAY_V : WHITE_OVERLAY_V;
    }

    public static int pack(int u, int v) {
        return u | v << 16;
    }

    public static int pack(float u, boolean hurt) {
        return pack(u(u), v(hurt));
    }
}
