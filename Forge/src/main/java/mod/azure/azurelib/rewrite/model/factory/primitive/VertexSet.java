package mod.azure.azurelib.rewrite.model.factory.primitive;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

import mod.azure.azurelib.cache.object.GeoVertex;

/**
 * Holder class to make it easier to store and refer to vertices for a given cube.
 */
public class VertexSet {

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

    public VertexSet(Vector3d origin, Vector3d vertexSize, double inflation) {
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

    public GeoVertex getBottomLeftBack() {
        return bottomLeftBack;
    }

    public GeoVertex getBottomRightBack() {
        return bottomRightBack;
    }

    public GeoVertex getTopLeftBack() {
        return topLeftBack;
    }

    public GeoVertex getTopRightBack() {
        return topRightBack;
    }

    public GeoVertex getTopLeftFront() {
        return topLeftFront;
    }

    public GeoVertex getTopRightFront() {
        return topRightFront;
    }

    public GeoVertex getBottomLeftFront() {
        return bottomLeftFront;
    }

    public GeoVertex getBottomRightFront() {
        return bottomRightFront;
    }

    public GeoVertex[] quadWest() {
        return new GeoVertex[] { topRightBack, topLeftBack, bottomLeftBack, bottomRightBack };
    }

    public GeoVertex[] quadEast() {
        return new GeoVertex[] { topLeftFront, topRightFront, bottomRightFront, bottomLeftFront };
    }

    public GeoVertex[] quadNorth() {
        return new GeoVertex[] { topLeftBack, topLeftFront, bottomLeftFront, bottomLeftBack };
    }

    public GeoVertex[] quadSouth() {
        return new GeoVertex[] { topRightFront, topRightBack, bottomRightBack, bottomRightFront };
    }

    public GeoVertex[] quadUp() {
        return new GeoVertex[] { topRightBack, topRightFront, topLeftFront, topLeftBack };
    }

    public GeoVertex[] quadDown() {
        return new GeoVertex[] { bottomLeftBack, bottomLeftFront, bottomRightFront, bottomRightBack };
    }

    public GeoVertex[] verticesForQuad(Direction direction, boolean boxUv, boolean mirror) {
        switch (direction) {
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
                throw new IllegalArgumentException("Unsupported direction: " + direction);
        }
    }
}
