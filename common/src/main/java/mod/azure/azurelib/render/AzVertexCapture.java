package mod.azure.azurelib.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.Arrays;

/**
 * A {@link VertexConsumer} that transparently forwards every call to a {@code delegate} (so rendering happens as normal
 * on a cache miss) while also recording each vertex's position, UV, and normal into a flat float array.
 * <p>
 * Used by {@code AzEntityModelRenderer}/{@code AzBlockEntityModelRenderer} to populate
 * {@code AzEntityGeometryCache}/{@code AzBlockEntityGeometryCache}: color, overlay, and light are deliberately not
 * captured, since those are re-applied fresh from the specific animatable being rendered when a cached snapshot is
 * replayed (letting, for example, a swarm of identically-posed entities with different lighting/overlay share one
 * recorded pose).
 */
public final class AzVertexCapture implements VertexConsumer {

    private static final int FLOAT_STRIDE = 8;

    private static final int INITIAL_VERTICES = 512;

    private VertexConsumer delegate;

    private float[] floats = new float[INITIAL_VERTICES * FLOAT_STRIDE];

    private int vertexCount;

    private float vX, vY, vZ, vU, vV;

    public AzVertexCapture(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    /** Resets the recorded vertex count to zero and installs a new delegate for a fresh capture pass. */
    public void reset(VertexConsumer delegate) {
        this.delegate = delegate;
        this.vertexCount = 0;
    }

    /** Swaps the forwarding target without discarding anything already recorded this pass. */
    public void updateDelegate(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    public VertexConsumer delegate() {
        return delegate;
    }

    public float[] getCapturedData() {
        return Arrays.copyOf(floats, vertexCount * FLOAT_STRIDE);
    }

    public int getVertexCount() {
        return vertexCount;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        vX = x;
        vY = y;
        vZ = z;
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        delegate.setColor(r, g, b, a);
        return this;
    }

    @Override
    public VertexConsumer setColor(int argb) {
        delegate.setColor(argb);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        vU = u;
        vV = v;
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float nx, float ny, float nz) {
        ensureCapacity();

        var f = vertexCount * FLOAT_STRIDE;
        floats[f] = vX;
        floats[f + 1] = vY;
        floats[f + 2] = vZ;
        floats[f + 3] = vU;
        floats[f + 4] = vV;
        floats[f + 5] = nx;
        floats[f + 6] = ny;
        floats[f + 7] = nz;
        vertexCount++;

        delegate.setNormal(nx, ny, nz);
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        delegate.setLineWidth(width);
        return this;
    }

    private void ensureCapacity() {
        if ((vertexCount + 1) * FLOAT_STRIDE > floats.length) {
            floats = Arrays.copyOf(floats, floats.length * 2);
        }
    }
}
