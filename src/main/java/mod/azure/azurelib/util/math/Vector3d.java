package mod.azure.azurelib.util.math;

/**
 * Mutable double 3-vector, mirroring the 1.18 {@code com.mojang.math.Vector3d} API.
 */
public class Vector3d {

    public double x;

    public double y;

    public double z;

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vector3d other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public void add(Vector3d other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
    }

    public void scale(double multiplier) {
        this.x *= multiplier;
        this.y *= multiplier;
        this.z *= multiplier;
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }
}
