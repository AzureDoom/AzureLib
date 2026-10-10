/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.object;

import mod.azure.azurelib.util.math.Vector3f;

/**
 * Vertex data holder
 *
 * @param position The position of the vertex
 * @param texU     The texture U coordinate
 * @param texV     The texture V coordinate
 */
public final class GeoVertex {

    private final Vector3f position;

    private final float texU;

    private final float texV;

    public GeoVertex(Vector3f position, float texU, float texV) {
        this.position = position;
        this.texU = texU;
        this.texV = texV;
    }

    public Vector3f position() {
        return this.position;
    }

    public float texU() {
        return this.texU;
    }

    public float texV() {
        return this.texV;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GeoVertex))
            return false;
        GeoVertex other = (GeoVertex) o;
        return java.util.Objects.equals(this.position, other.position)
            && Float.compare(this.texU, other.texU) == 0
            && Float.compare(this.texV, other.texV) == 0;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.position);
        result = 31 * result + Float.hashCode(this.texU);
        result = 31 * result + Float.hashCode(this.texV);
        return result;
    }

    @Override
    public String toString() {
        return "GeoVertex[position=" + this.position + ", texU=" + this.texU + ", texV=" + this.texV + "]";
    }

    public GeoVertex(double x, double y, double z) {
        this(new Vector3f((float) x, (float) y, (float) z), 0, 0);
    }

    public GeoVertex withUVs(float texU, float texV) {
        return new GeoVertex(this.position, texU, texV);
    }
}
