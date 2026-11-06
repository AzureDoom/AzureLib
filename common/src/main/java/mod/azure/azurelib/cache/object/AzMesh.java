package mod.azure.azurelib.cache.object;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.factory.primitive.MeshPolygons;

/**
 * Baked rigid polygon mesh for an {@link AzBone}, from a Bedrock {@code poly_mesh}.
 * <p>
 * Stored as flat arrays of quad corners in the bone's model space, the same space cube vertices use, so it renders
 * under the bone's pose exactly like a cube. Every polygon is already a quad (see {@link MeshPolygons}).
 * </p>
 * <p>
 * Immutable and shared between copies of a model: arrays are copied on construction and the array accessors return
 * copies. Per-frame code should use the indexed getters, which don't allocate.
 * </p>
 */
public record AzMesh(
    int quadCount,
    float[] positions,
    float[] normals,
    float[] uvs,
    float[] uvBounds
) {

    /**
     * @param quadCount number of quads
     * @param positions corner positions, {@code xyz} per corner ({@code quadCount * 12} floats)
     * @param normals   corner normals, {@code xyz} per corner
     * @param uvs       corner UVs (0-1), {@code uv} per corner
     * @param uvBounds  per quad {@code minU, minV, maxU, maxV}
     */
    public AzMesh(int quadCount, float[] positions, float[] normals, float[] uvs, float[] uvBounds) {
        this.quadCount = quadCount;
        this.positions = positions.clone();
        this.normals = normals.clone();
        this.uvs = uvs.clone();
        this.uvBounds = uvBounds.clone();
    }

    @Override
    public float[] positions() {
        return positions.clone();
    }

    @Override
    public float[] normals() {
        return normals.clone();
    }

    @Override
    public float[] uvs() {
        return uvs.clone();
    }

    @Override
    public float[] uvBounds() {
        return uvBounds.clone();
    }

    public int positionComponentCount() {
        return positions.length;
    }

    public float position(int i) {
        return positions[i];
    }

    public float normal(int i) {
        return normals[i];
    }

    public float uv(int i) {
        return uvs[i];
    }

    public float uvBound(int i) {
        return uvBounds[i];
    }

    public static AzMesh of(MeshPolygons polygons) {
        int corners = polygons.quadCount() * 4;
        float[] positions = new float[corners * 3];

        for (int i = 0; i < corners; i++) {
            int source = polygons.cornerPosition(i) * 3;
            positions[i * 3] = polygons.position(source);
            positions[i * 3 + 1] = polygons.position(source + 1);
            positions[i * 3 + 2] = polygons.position(source + 2);
        }

        return new AzMesh(
            polygons.quadCount(),
            positions,
            polygons.cornerNormals(),
            polygons.cornerUvs(),
            polygons.uvBounds()
        );
    }

    public boolean isEmpty() {
        return quadCount == 0;
    }
}
