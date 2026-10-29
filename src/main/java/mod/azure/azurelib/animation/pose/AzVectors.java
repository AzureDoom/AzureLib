package mod.azure.azurelib.animation.pose;

import mod.azure.azurelib.util.math.Vector3f;

/**
 * Small vector helpers the pose, IK and socket code needs that {@link Vector3f} doesn't have: its mutators return
 * {@code void}, and it has no distance or length methods. None of these allocate.
 */
public final class AzVectors {

    private AzVectors() {}

    /** Sets {@code dest} and returns it, for the {@code return dest.set(...)} pattern. */
    public static Vector3f set(Vector3f dest, float x, float y, float z) {
        dest.set(x, y, z);
        return dest;
    }

    /** Copies {@code source} into {@code dest} and returns it. */
    public static Vector3f set(Vector3f dest, Vector3f source) {
        dest.load(source);
        return dest;
    }

    public static float lengthSquared(Vector3f v) {
        return v.x() * v.x() + v.y() * v.y() + v.z() * v.z();
    }

    public static float length(Vector3f v) {
        return (float) Math.sqrt(lengthSquared(v));
    }

    public static float distanceSquared(Vector3f a, Vector3f b) {
        float dx = a.x() - b.x();
        float dy = a.y() - b.y();
        float dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }

    public static float distance(Vector3f a, Vector3f b) {
        return (float) Math.sqrt(distanceSquared(a, b));
    }
}
