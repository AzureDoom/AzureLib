package mod.azure.azurelib.animation.pose.secondary;

import java.util.Objects;

/**
 * A shape attached to a bone that {@link AzSpringBone} chains can't pass through, such as a capsule along each thigh so
 * a tail swings around the legs instead of through them. Colliders follow their bone's animated pose.
 * <p>
 * Positions are in Blockbench pixels from the bone's pivot, along the bone's own axes, and the radius is in pixels, the
 * same conventions as socket and effector offsets. One collider can be shared by any number of spring chains.
 * </p>
 */
public final class AzSpringCollider {

    public enum Shape {
        SPHERE,
        CAPSULE
    }

    private final String bone;

    private final Shape shape;

    private final float startX, startY, startZ;

    private final float endX, endY, endZ;

    private final float radius;

    private AzSpringCollider(
        String bone,
        Shape shape,
        float startX,
        float startY,
        float startZ,
        float endX,
        float endY,
        float endZ,
        float radius
    ) {
        this.bone = Objects.requireNonNull(bone, "bone");
        this.shape = shape;
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;

        if (!(radius > 0)) {
            throw new IllegalArgumentException("A spring collider needs a positive radius");
        }

        this.radius = radius;
    }

    /** A sphere centred {@code (x, y, z)} pixels from the bone's pivot. */
    public static AzSpringCollider sphere(String bone, float x, float y, float z, float radius) {
        return new AzSpringCollider(bone, Shape.SPHERE, x, y, z, x, y, z, radius);
    }

    /**
     * A capsule: every point within {@code radius} of the line from {@code (x1, y1, z1)} to {@code (x2, y2, z2)}, both
     * in pixels from the bone's pivot. Good for limbs and bodies.
     */
    public static AzSpringCollider capsule(
        String bone,
        float x1,
        float y1,
        float z1,
        float x2,
        float y2,
        float z2,
        float radius
    ) {
        return new AzSpringCollider(bone, Shape.CAPSULE, x1, y1, z1, x2, y2, z2, radius);
    }

    public String bone() {
        return bone;
    }

    public Shape shape() {
        return shape;
    }

    public float startX() {
        return startX;
    }

    public float startY() {
        return startY;
    }

    public float startZ() {
        return startZ;
    }

    public float endX() {
        return endX;
    }

    public float endY() {
        return endY;
    }

    public float endZ() {
        return endZ;
    }

    public float radius() {
        return radius;
    }
}
