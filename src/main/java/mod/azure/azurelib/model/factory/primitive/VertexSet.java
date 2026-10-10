package mod.azure.azurelib.model.factory.primitive;

import net.minecraft.util.EnumFacing;

import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.util.math.Vec3;

/**
 * Holder class to make it easier to store and refer to vertices for a given cube
 */
public final class VertexSet {

    private final GeoVertex bottomLeftBack;

    private final GeoVertex bottomRightBack;

    private final GeoVertex topLeftBack;

    private final GeoVertex topRightBack;

    private final GeoVertex topLeftFront;

    private final GeoVertex topRightFront;

    private final GeoVertex bottomLeftFront;

    private final GeoVertex bottomRightFront;

    public VertexSet(
        GeoVertex bottomLeftBack,
        GeoVertex bottomRightBack,
        GeoVertex topLeftBack,
        GeoVertex topRightBack,
        GeoVertex topLeftFront,
        GeoVertex topRightFront,
        GeoVertex bottomLeftFront,
        GeoVertex bottomRightFront
    ) {
        this.bottomLeftBack = bottomLeftBack;
        this.bottomRightBack = bottomRightBack;
        this.topLeftBack = topLeftBack;
        this.topRightBack = topRightBack;
        this.topLeftFront = topLeftFront;
        this.topRightFront = topRightFront;
        this.bottomLeftFront = bottomLeftFront;
        this.bottomRightFront = bottomRightFront;
    }

    public GeoVertex bottomLeftBack() {
        return this.bottomLeftBack;
    }

    public GeoVertex bottomRightBack() {
        return this.bottomRightBack;
    }

    public GeoVertex topLeftBack() {
        return this.topLeftBack;
    }

    public GeoVertex topRightBack() {
        return this.topRightBack;
    }

    public GeoVertex topLeftFront() {
        return this.topLeftFront;
    }

    public GeoVertex topRightFront() {
        return this.topRightFront;
    }

    public GeoVertex bottomLeftFront() {
        return this.bottomLeftFront;
    }

    public GeoVertex bottomRightFront() {
        return this.bottomRightFront;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof VertexSet))
            return false;
        VertexSet other = (VertexSet) o;
        return java.util.Objects.equals(this.bottomLeftBack, other.bottomLeftBack)
            && java.util.Objects.equals(this.bottomRightBack, other.bottomRightBack)
            && java.util.Objects.equals(this.topLeftBack, other.topLeftBack)
            && java.util.Objects.equals(this.topRightBack, other.topRightBack)
            && java.util.Objects.equals(this.topLeftFront, other.topLeftFront)
            && java.util.Objects.equals(this.topRightFront, other.topRightFront)
            && java.util.Objects.equals(this.bottomLeftFront, other.bottomLeftFront)
            && java.util.Objects.equals(this.bottomRightFront, other.bottomRightFront);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.bottomLeftBack);
        result = 31 * result + java.util.Objects.hashCode(this.bottomRightBack);
        result = 31 * result + java.util.Objects.hashCode(this.topLeftBack);
        result = 31 * result + java.util.Objects.hashCode(this.topRightBack);
        result = 31 * result + java.util.Objects.hashCode(this.topLeftFront);
        result = 31 * result + java.util.Objects.hashCode(this.topRightFront);
        result = 31 * result + java.util.Objects.hashCode(this.bottomLeftFront);
        result = 31 * result + java.util.Objects.hashCode(this.bottomRightFront);
        return result;
    }

    @Override
    public String toString() {
        return "VertexSet[bottomLeftBack=" + this.bottomLeftBack + ", bottomRightBack=" + this.bottomRightBack
            + ", topLeftBack=" + this.topLeftBack + ", topRightBack=" + this.topRightBack + ", topLeftFront="
            + this.topLeftFront + ", topRightFront=" + this.topRightFront + ", bottomLeftFront=" + this.bottomLeftFront
            + ", bottomRightFront=" + this.bottomRightFront + "]";
    }

    public VertexSet(Vec3 origin, Vec3 vertexSize, double inflation) {
        this(
            new GeoVertex(origin.x - inflation, origin.y - inflation, origin.z - inflation),
            new GeoVertex(origin.x - inflation, origin.y - inflation, origin.z + vertexSize.z + inflation),
            new GeoVertex(origin.x - inflation, origin.y + vertexSize.y + inflation, origin.z - inflation),
            new GeoVertex(
                origin.x - inflation,
                origin.y + vertexSize.y + inflation,
                origin.z + vertexSize.z + inflation
            ),
            new GeoVertex(
                origin.x + vertexSize.x + inflation,
                origin.y + vertexSize.y + inflation,
                origin.z - inflation
            ),
            new GeoVertex(
                origin.x + vertexSize.x + inflation,
                origin.y + vertexSize.y + inflation,
                origin.z + vertexSize.z + inflation
            ),
            new GeoVertex(origin.x + vertexSize.x + inflation, origin.y - inflation, origin.z - inflation),
            new GeoVertex(
                origin.x + vertexSize.x + inflation,
                origin.y - inflation,
                origin.z + vertexSize.z + inflation
            )
        );
    }

    /**
     * Returns the normal vertex array for a west-facing quad
     */
    public GeoVertex[] quadWest() {
        return new GeoVertex[] { this.topRightBack, this.topLeftBack, this.bottomLeftBack, this.bottomRightBack };
    }

    /**
     * Returns the normal vertex array for an east-facing quad
     */
    public GeoVertex[] quadEast() {
        return new GeoVertex[] {
            this.topLeftFront,
            this.topRightFront,
            this.bottomRightFront,
            this.bottomLeftFront
        };
    }

    /**
     * Returns the normal vertex array for a north-facing quad
     */
    public GeoVertex[] quadNorth() {
        return new GeoVertex[] { this.topLeftBack, this.topLeftFront, this.bottomLeftFront, this.bottomLeftBack };
    }

    /**
     * Returns the normal vertex array for a south-facing quad
     */
    public GeoVertex[] quadSouth() {
        return new GeoVertex[] {
            this.topRightFront,
            this.topRightBack,
            this.bottomRightBack,
            this.bottomRightFront
        };
    }

    /**
     * Returns the normal vertex array for a top-facing quad
     */
    public GeoVertex[] quadUp() {
        return new GeoVertex[] { this.topRightBack, this.topRightFront, this.topLeftFront, this.topLeftBack };
    }

    /**
     * Returns the normal vertex array for a bottom-facing quad
     */
    public GeoVertex[] quadDown() {
        return new GeoVertex[] {
            this.bottomLeftBack,
            this.bottomLeftFront,
            this.bottomRightFront,
            this.bottomRightBack
        };
    }

    /**
     * Return the vertex array relevant to the quad being built, taking into account mirroring and quad type
     */
    public GeoVertex[] verticesForQuad(EnumFacing direction, boolean boxUv, boolean mirror) {
        switch ((direction)) {
            case WEST:
                return mirror ? quadEast() : quadWest();
            case EAST:
                return mirror ? quadWest() : quadEast();
            case NORTH:
                return quadNorth();
            case SOUTH:
                return quadSouth();
            case UP:
                return mirror && !boxUv ? quadDown() : quadUp();
            case DOWN:
                return mirror && !boxUv ? quadUp() : quadDown();
            default:
                throw new IllegalStateException();
        }
    }
}
