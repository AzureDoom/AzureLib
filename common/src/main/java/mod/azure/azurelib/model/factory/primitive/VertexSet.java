package mod.azure.azurelib.model.factory.primitive;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import mod.azure.azurelib.cache.object.AzVertex;

/**
 * Holder class to make it easier to store and refer to vertices for a given cube
 */
public record VertexSet(
    AzVertex bottomLeftBack,
    AzVertex bottomRightBack,
    AzVertex topLeftBack,
    AzVertex topRightBack,
    AzVertex topLeftFront,
    AzVertex topRightFront,
    AzVertex bottomLeftFront,
    AzVertex bottomRightFront
) {

    public VertexSet(Vec3 origin, Vec3 vertexSize, double inflation) {
        this(
            new AzVertex(origin.x - inflation, origin.y - inflation, origin.z - inflation),
            new AzVertex(origin.x - inflation, origin.y - inflation, origin.z + vertexSize.z + inflation),
            new AzVertex(origin.x - inflation, origin.y + vertexSize.y + inflation, origin.z - inflation),
            new AzVertex(
                origin.x - inflation,
                origin.y + vertexSize.y + inflation,
                origin.z + vertexSize.z + inflation
            ),
            new AzVertex(
                origin.x + vertexSize.x + inflation,
                origin.y + vertexSize.y + inflation,
                origin.z - inflation
            ),
            new AzVertex(
                origin.x + vertexSize.x + inflation,
                origin.y + vertexSize.y + inflation,
                origin.z + vertexSize.z + inflation
            ),
            new AzVertex(origin.x + vertexSize.x + inflation, origin.y - inflation, origin.z - inflation),
            new AzVertex(
                origin.x + vertexSize.x + inflation,
                origin.y - inflation,
                origin.z + vertexSize.z + inflation
            )
        );
    }

    /**
     * Returns the normal vertex array for a west-facing quad
     */
    public AzVertex[] quadWest() {
        return new AzVertex[] { this.topRightBack, this.topLeftBack, this.bottomLeftBack, this.bottomRightBack };
    }

    /**
     * Returns the normal vertex array for an east-facing quad
     */
    public AzVertex[] quadEast() {
        return new AzVertex[] {
            this.topLeftFront,
            this.topRightFront,
            this.bottomRightFront,
            this.bottomLeftFront
        };
    }

    /**
     * Returns the normal vertex array for a north-facing quad
     */
    public AzVertex[] quadNorth() {
        return new AzVertex[] { this.topLeftBack, this.topLeftFront, this.bottomLeftFront, this.bottomLeftBack };
    }

    /**
     * Returns the normal vertex array for a south-facing quad
     */
    public AzVertex[] quadSouth() {
        return new AzVertex[] {
            this.topRightFront,
            this.topRightBack,
            this.bottomRightBack,
            this.bottomRightFront
        };
    }

    /**
     * Returns the normal vertex array for a top-facing quad
     */
    public AzVertex[] quadUp() {
        return new AzVertex[] { this.topRightBack, this.topRightFront, this.topLeftFront, this.topLeftBack };
    }

    /**
     * Returns the normal vertex array for a bottom-facing quad
     */
    public AzVertex[] quadDown() {
        return new AzVertex[] {
            this.bottomLeftBack,
            this.bottomLeftFront,
            this.bottomRightFront,
            this.bottomRightBack
        };
    }

    /**
     * Return the vertex array relevant to the quad being built, taking into account mirroring and quad type
     */
    public AzVertex[] verticesForQuad(Direction direction, boolean boxUv, boolean mirror) {
        return switch (direction) {
            case WEST -> mirror ? quadEast() : quadWest();
            case EAST -> mirror ? quadWest() : quadEast();
            case NORTH -> quadNorth();
            case SOUTH -> quadSouth();
            case UP -> mirror && !boxUv ? quadDown() : quadUp();
            case DOWN -> mirror && !boxUv ? quadUp() : quadDown();
        };
    }
}
