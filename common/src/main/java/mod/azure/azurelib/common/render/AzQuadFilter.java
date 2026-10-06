package mod.azure.azurelib.common.render;

/**
 * Picks which quads of a model to draw, from the bounds of each quad's UVs (normalized texture coordinates). Used by
 * the auto-glowing layer to skip quads whose UV area has no glowing pixels.
 */
@FunctionalInterface
public interface AzQuadFilter {

    boolean test(float minU, float minV, float maxU, float maxV);
}
