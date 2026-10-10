/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.object;

import mod.azure.azurelib.loading.json.raw.FaceUV;
import mod.azure.azurelib.util.math.Direction;
import mod.azure.azurelib.util.math.Vector3f;

/**
 * Quad data holder
 */
public final class GeoQuad {

    private final GeoVertex[] vertices;

    private final Vector3f normal;

    private final Direction direction;

    public GeoQuad(GeoVertex[] vertices, Vector3f normal, Direction direction) {
        this.vertices = vertices;
        this.normal = normal;
        this.direction = direction;
    }

    public GeoVertex[] vertices() {
        return this.vertices;
    }

    public Vector3f normal() {
        return this.normal;
    }

    public Direction direction() {
        return this.direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GeoQuad))
            return false;
        GeoQuad other = (GeoQuad) o;
        return java.util.Objects.equals(this.vertices, other.vertices)
            && java.util.Objects.equals(this.normal, other.normal)
            && java.util.Objects.equals(this.direction, other.direction);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.vertices);
        result = 31 * result + java.util.Objects.hashCode(this.normal);
        result = 31 * result + java.util.Objects.hashCode(this.direction);
        return result;
    }

    @Override
    public String toString() {
        return "GeoQuad[vertices=" + this.vertices + ", normal=" + this.normal + ", direction=" + this.direction + "]";
    }

    public static GeoQuad build(
        GeoVertex[] vertices,
        double[] uvCoords,
        double[] uvSize,
        FaceUV.Rotation uvRotation,
        float texWidth,
        float texHeight,
        boolean mirror,
        Direction direction
    ) {
        return build(
            vertices,
            (float) uvCoords[0],
            (float) uvCoords[1],
            (float) uvSize[0],
            (float) uvSize[1],
            uvRotation,
            texWidth,
            texHeight,
            mirror,
            direction
        );
    }

    public static GeoQuad build(
        GeoVertex[] vertices,
        float u,
        float v,
        float uSize,
        float vSize,
        FaceUV.Rotation uvRotation,
        float texWidth,
        float texHeight,
        boolean mirror,
        Direction direction
    ) {
        float uWidth = (u + uSize) / texWidth;
        float vHeight = (v + vSize) / texHeight;
        u /= texWidth;
        v /= texHeight;
        Vector3f normal = new Vector3f(
            direction.getStepX(),
            direction.getStepY(),
            direction.getStepZ()
        );

        if (!mirror) {
            float tempWidth = uWidth;
            uWidth = u;
            u = tempWidth;
        } else {
            normal.mul(-1, 1, 1);
        }

        float[] uvs = uvRotation.rotateUvs(u, v, uWidth, vHeight);
        vertices[0] = vertices[0].withUVs(uvs[0], uvs[1]);
        vertices[1] = vertices[1].withUVs(uvs[2], uvs[3]);
        vertices[2] = vertices[2].withUVs(uvs[4], uvs[5]);
        vertices[3] = vertices[3].withUVs(uvs[6], uvs[7]);

        return new GeoQuad(vertices, normal, direction);
    }
}
