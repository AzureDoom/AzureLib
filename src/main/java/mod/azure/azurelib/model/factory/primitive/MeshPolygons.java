package mod.azure.azurelib.model.factory.primitive;

import java.util.Arrays;

import mod.azure.azurelib.loading.json.raw.PolyMesh;
import mod.azure.azurelib.loading.json.raw.PolysUnion;

/**
 * Turns Bedrock {@link PolyMesh} data into flat, render-ready quad arrays.
 * <p>
 * Every polygon becomes one or more quads: a quad stays as-is, a triangle repeats its last corner, and anything larger
 * is ear-clipped into triangles first. Positions are converted from Bedrock space (pixels, X mirrored) to model space
 * (blocks), normals are mirrored to match, and UVs are normalized to 0-1. Corner order is preserved, which after
 * un-mirroring is the counter-clockwise order Blockbench draws.
 * </p>
 * <p>
 * Instances are immutable and shared between copies of a model. The arrays are copied on construction and the array
 * accessors return copies; per-frame code should use the indexed getters ({@link #position}, {@link #cornerPosition},
 * ...), which don't allocate.
 * </p>
 */
public final class MeshPolygons {

    private static final int[][] TRIANGLE = { { 0, 1, 2, 2 } };

    private static final int[][] QUAD = { { 0, 1, 2, 3 } };

    private static final int[][] QUAD_ROTATED = { { 1, 2, 3, 0 } };

    private static final float EPSILON = 1.0e-9f;

    private final int quadCount;

    private final float[] positions;

    private final int[] cornerPositions;

    private final float[] cornerNormals;

    private final float[] cornerUvs;

    private final float[] uvBounds;

    /**
     * @param quadCount       number of quads
     * @param positions       unique positions in model space, {@code xyz} per position
     * @param cornerPositions position index of each quad corner ({@code quadCount * 4})
     * @param cornerNormals   normal of each quad corner, {@code xyz} per corner
     * @param cornerUvs       UV of each quad corner, {@code uv} per corner
     * @param uvBounds        UV bounds of each quad ({@code minU, minV, maxU, maxV}), for quad filters
     */
    public MeshPolygons(
        int quadCount,
        float[] positions,
        int[] cornerPositions,
        float[] cornerNormals,
        float[] cornerUvs,
        float[] uvBounds
    ) {
        this.quadCount = quadCount;
        this.positions = positions.clone();
        this.cornerPositions = cornerPositions.clone();
        this.cornerNormals = cornerNormals.clone();
        this.cornerUvs = cornerUvs.clone();
        this.uvBounds = uvBounds.clone();
    }

    public int quadCount() {
        return quadCount;
    }

    public int positionCount() {
        return positions.length / 3;
    }

    /** A copy of the unique positions. Use {@link #position} on hot paths. */
    public float[] positions() {
        return positions.clone();
    }

    /** A copy of the corner position indices. Use {@link #cornerPosition} on hot paths. */
    public int[] cornerPositions() {
        return cornerPositions.clone();
    }

    /** A copy of the corner normals. Use {@link #cornerNormal} on hot paths. */
    public float[] cornerNormals() {
        return cornerNormals.clone();
    }

    /** A copy of the corner UVs. Use {@link #cornerUv} on hot paths. */
    public float[] cornerUvs() {
        return cornerUvs.clone();
    }

    /** A copy of the quad UV bounds. Use {@link #uvBound} on hot paths. */
    public float[] uvBounds() {
        return uvBounds.clone();
    }

    /** Component {@code i} of the flat position array ({@code position * 3 + axis}). */
    public float position(int i) {
        return positions[i];
    }

    /** Position index used by quad corner {@code corner}. */
    public int cornerPosition(int corner) {
        return cornerPositions[corner];
    }

    /** Component {@code i} of the flat corner normal array ({@code corner * 3 + axis}). */
    public float cornerNormal(int i) {
        return cornerNormals[i];
    }

    /** Component {@code i} of the flat corner UV array ({@code corner * 2 + axis}). */
    public float cornerUv(int i) {
        return cornerUvs[i];
    }

    /** Component {@code i} of the flat UV bounds array ({@code quad * 4 + 0..3}). */
    public float uvBound(int i) {
        return uvBounds[i];
    }

    public static MeshPolygons bake(PolyMesh mesh, float textureWidth, float textureHeight) {
        float[] positions = convertPositions(mesh.positions());
        IntBuffer cornerPositions = new IntBuffer();
        FloatBuffer cornerNormals = new FloatBuffer();
        FloatBuffer cornerUvs = new FloatBuffer();
        FloatBuffer uvBounds = new FloatBuffer();
        boolean normalized = mesh.usesNormalizedUVs();
        float uScale = normalized || textureWidth <= 0 ? 1f : 1f / textureWidth;
        float vScale = normalized || textureHeight <= 0 ? 1f : 1f / textureHeight;
        PolysUnion polys = mesh.polysUnion();
        int positionCount = positions.length / 3;

        if (polys == null || positionCount == 0) {
            return new MeshPolygons(0, positions, new int[0], new float[0], new float[0], new float[0]);
        }

        int[][][] polygons;

        if (polys.type() != null) {
            // List shorthand: positions, normals and uvs are parallel arrays consumed in fixed-size groups.
            int size = polys.type().cornerCount();
            polygons = new int[positionCount / size][size][];

            for (int p = 0; p < polygons.length; p++) {
                for (int c = 0; c < size; c++) {
                    int index = p * size + c;
                    polygons[p][c] = new int[] { index, index, index };
                }
            }
        } else {
            polygons = polys.union();
        }

        float[] faceNormal = new float[3];

        for (int[][] polygon : polygons) {
            if (polygon == null || polygon.length < 3 || !validPositions(polygon, positionCount)) {
                continue;
            }

            computeFaceNormal(positions, polygon, faceNormal);

            for (int[] quad : quadsFor(positions, polygon, faceNormal)) {
                float minU = Float.POSITIVE_INFINITY, minV = Float.POSITIVE_INFINITY;
                float maxU = Float.NEGATIVE_INFINITY, maxV = Float.NEGATIVE_INFINITY;

                for (int cornerIndex : quad) {
                    int[] corner = polygon[cornerIndex];
                    cornerPositions.add(corner[0]);

                    double[] normal = vectorOrNull(mesh.normals(), corner[1], 3);
                    boolean usedNormal = false;

                    if (normal != null) {
                        // Normalize in double: a component near Float.MAX_VALUE would overflow when squared as a float.
                        double nx = -normal[0];
                        double ny = normal[1];
                        double nz = normal[2];
                        double length = Math.sqrt(nx * nx + ny * ny + nz * nz);

                        if (length > 1.0e-6 && !Double.isInfinite(length) && !Double.isNaN(length)) {
                            cornerNormals.add((float) (nx / length));
                            cornerNormals.add((float) (ny / length));
                            cornerNormals.add((float) (nz / length));
                            usedNormal = true;
                        }
                    }

                    if (!usedNormal) {
                        cornerNormals.add(faceNormal[0]);
                        cornerNormals.add(faceNormal[1]);
                        cornerNormals.add(faceNormal[2]);
                    }

                    double[] uv = vectorOrNull(mesh.uvs(), corner[2], 2);
                    float u = uv != null ? (float) uv[0] * uScale : 0f;
                    float v = uv != null ? (float) uv[1] * vScale : 0f;
                    cornerUvs.add(u);
                    cornerUvs.add(v);
                    minU = Math.min(minU, u);
                    minV = Math.min(minV, v);
                    maxU = Math.max(maxU, u);
                    maxV = Math.max(maxV, v);
                }

                uvBounds.add(minU);
                uvBounds.add(minV);
                uvBounds.add(maxU);
                uvBounds.add(maxV);
            }
        }

        return new MeshPolygons(
            cornerPositions.size() / 4,
            positions,
            cornerPositions.toArray(),
            cornerNormals.toArray(),
            cornerUvs.toArray(),
            uvBounds.toArray()
        );
    }

    /**
     * Splits a polygon into quads, as indices into its corners.
     * <p>
     * A quad is drawn as triangles {@code (0, 1, 2)} and {@code (2, 3, 0)}, splitting it along the 0-2 diagonal. For a
     * concave quad that diagonal lies outside the face when the reflex corner is 1 or 3, so those quads are rotated to
     * split along 1-3 instead. Larger polygons are ear-clipped into triangles (each a quad with its last corner
     * repeated), which stays inside concave outlines where a fan would not.
     * </p>
     */
    static int[][] quadsFor(float[] positions, int[][] polygon, float[] normal) {
        int corners = polygon.length;

        if (corners == 3) {
            return TRIANGLE;
        }

        if (corners == 4) {
            return isReflex(positions, polygon, 1, normal) || isReflex(positions, polygon, 3, normal)
                ? QUAD_ROTATED
                : QUAD;
        }

        return earClip(positions, polygon, normal);
    }

    /** Whether corner {@code index} turns against the polygon's winding, i.e. is the inner corner of a concavity. */
    private static boolean isReflex(float[] positions, int[][] polygon, int index, float[] normal) {
        int n = polygon.length;
        return turn(
            positions,
            polygon[(index + n - 1) % n][0],
            polygon[index][0],
            polygon[(index + 1) % n][0],
            normal
        ) < -EPSILON;
    }

    /** Signed turn at {@code b} for the path a -> b -> c, positive when it follows the winding given by {@code normal}. */
    private static float turn(float[] p, int a, int b, int c, float[] normal) {
        a *= 3;
        b *= 3;
        c *= 3;
        float ux = p[b] - p[a], uy = p[b + 1] - p[a + 1], uz = p[b + 2] - p[a + 2];
        float vx = p[c] - p[b], vy = p[c + 1] - p[b + 1], vz = p[c + 2] - p[b + 2];
        return (uy * vz - uz * vy) * normal[0] + (uz * vx - ux * vz) * normal[1] + (ux * vy - uy * vx) * normal[2];
    }

    /** True if position {@code q} lies inside or on triangle a, b, c (counter-clockwise around {@code normal}). */
    private static boolean inTriangle(float[] p, int a, int b, int c, int q, float[] normal) {
        return turn(p, a, b, q, normal) >= -EPSILON
            && turn(p, b, c, q, normal) >= -EPSILON
            && turn(p, c, a, q, normal) >= -EPSILON;
    }

    /**
     * Ear clipping: repeatedly cut off a convex corner whose triangle contains no other corner. Falls back to a fan for
     * whatever is left if no ear can be found, which only happens for self-intersecting or degenerate outlines.
     */
    private static int[][] earClip(float[] positions, int[][] polygon, float[] normal) {
        IntBuffer remaining = new IntBuffer();

        for (int i = 0; i < polygon.length; i++) {
            remaining.add(i);
        }

        int[][] quads = new int[polygon.length - 2][];
        int count = 0;
        int guard = polygon.length * polygon.length;

        while (remaining.size() > 3 && guard-- > 0) {
            int size = remaining.size();
            boolean clipped = false;

            for (int i = 0; i < size; i++) {
                int prev = remaining.get((i + size - 1) % size);
                int curr = remaining.get(i);
                int next = remaining.get((i + 1) % size);
                int a = polygon[prev][0], b = polygon[curr][0], c = polygon[next][0];

                if (turn(positions, a, b, c, normal) <= EPSILON) {
                    continue; // reflex or collinear: not an ear
                }

                boolean blocked = false;

                for (int k = 0; k < size && !blocked; k++) {
                    int other = remaining.get(k);

                    if (other == prev || other == curr || other == next) {
                        continue;
                    }

                    int q = polygon[other][0];
                    blocked = q != a && q != b && q != c && inTriangle(positions, a, b, c, q, normal);
                }

                if (!blocked) {
                    quads[count++] = new int[] { prev, curr, next, next };
                    remaining.remove(i);
                    clipped = true;
                    break;
                }
            }

            if (!clipped) {
                break;
            }
        }

        // The final triangle, or a fan over anything ear clipping couldn't resolve.
        for (int i = 1; i + 1 < remaining.size(); i++) {
            quads[count++] = new int[] {
                remaining.get(0),
                remaining.get(i),
                remaining.get(i + 1),
                remaining.get(i + 1) };
        }

        return count == quads.length ? quads : Arrays.copyOf(quads, count);
    }

    private static float[] convertPositions(double[][] source) {
        float[] positions = new float[source.length * 3];

        for (int i = 0; i < source.length; i++) {
            double[] position = source[i];
            double x = position.length > 0 ? position[0] : 0;
            double y = position.length > 1 ? position[1] : 0;
            double z = position.length > 2 ? position[2] : 0;
            positions[i * 3] = (float) (-x / 16d);
            positions[i * 3 + 1] = (float) (y / 16d);
            positions[i * 3 + 2] = (float) (z / 16d);
        }

        return positions;
    }

    private static boolean validPositions(int[][] polygon, int positionCount) {
        for (int[] corner : polygon) {
            if (corner == null || corner[0] < 0 || corner[0] >= positionCount) {
                return false;
            }
        }

        return true;
    }

    private static double[] vectorOrNull(double[][] vectors, int index, int size) {
        if (index < 0 || index >= vectors.length) {
            return null;
        }

        double[] vector = vectors[index];
        return vector != null && vector.length >= size ? vector : null;
    }

    /** Newell's method, so concave and non-planar polygons still get a stable normal. */
    private static void computeFaceNormal(float[] positions, int[][] polygon, float[] out) {
        float nx = 0, ny = 0, nz = 0;

        for (int i = 0; i < polygon.length; i++) {
            int a = polygon[i][0] * 3;
            int b = polygon[(i + 1) % polygon.length][0] * 3;
            nx += (positions[a + 1] - positions[b + 1]) * (positions[a + 2] + positions[b + 2]);
            ny += (positions[a + 2] - positions[b + 2]) * (positions[a] + positions[b]);
            nz += (positions[a] - positions[b]) * (positions[a + 1] + positions[b + 1]);
        }

        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);

        if (length < 1.0e-12f) {
            out[0] = 0;
            out[1] = 1;
            out[2] = 0;
            return;
        }

        out[0] = nx / length;
        out[1] = ny / length;
        out[2] = nz / length;
    }

    /** Minimal growable int list, so this class needs nothing beyond the JDK. */
    private static final class IntBuffer {

        private int[] values = new int[16];

        private int size;

        void add(int value) {
            if (size == values.length) {
                values = Arrays.copyOf(values, size * 2);
            }

            values[size++] = value;
        }

        int get(int index) {
            return values[index];
        }

        void remove(int index) {
            System.arraycopy(values, index + 1, values, index, size - index - 1);
            size--;
        }

        int size() {
            return size;
        }

        int[] toArray() {
            return Arrays.copyOf(values, size);
        }
    }

    /** Minimal growable float list. */
    private static final class FloatBuffer {

        private float[] values = new float[16];

        private int size;

        void add(float value) {
            if (size == values.length) {
                values = Arrays.copyOf(values, size * 2);
            }

            values[size++] = value;
        }

        int size() {
            return size;
        }

        float[] toArray() {
            return Arrays.copyOf(values, size);
        }
    }
}
