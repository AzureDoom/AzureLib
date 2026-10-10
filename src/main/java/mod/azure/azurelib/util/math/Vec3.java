package mod.azure.azurelib.util.math;

import net.minecraft.util.math.Vec3d;

/**
 * Immutable double 3-vector used for AzureLib's baked model data, mirroring the parts of the 1.18
 * {@code net.minecraft.world.phys.Vec3} API that AzureLib relies on. Use {@link #of(Vec3d)} / {@link #toVec3d()} to
 * convert to and from Minecraft 1.12.2's {@link Vec3d}.
 */
public final class Vec3 {

    public static final Vec3 ZERO = new Vec3(0.0D, 0.0D, 0.0D);

    public final double x;

    public final double y;

    public final double z;

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(Vector3f vector) {
        this(vector.x(), vector.y(), vector.z());
    }

    public static Vec3 of(Vec3d vec) {
        return new Vec3(vec.x, vec.y, vec.z);
    }

    public Vec3d toVec3d() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public double x() {
        return this.x;
    }

    public double y() {
        return this.y;
    }

    public double z() {
        return this.z;
    }

    public Vec3 add(double x, double y, double z) {
        return new Vec3(this.x + x, this.y + y, this.z + z);
    }

    public Vec3 add(Vec3 other) {
        return add(other.x, other.y, other.z);
    }

    public Vec3 subtract(Vec3 other) {
        return add(-other.x, -other.y, -other.z);
    }

    public Vec3 scale(double factor) {
        return new Vec3(this.x * factor, this.y * factor, this.z * factor);
    }

    public Vec3 multiply(double x, double y, double z) {
        return new Vec3(this.x * x, this.y * y, this.z * z);
    }

    public double dot(Vec3 other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public double lengthSqr() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double length() {
        return Math.sqrt(lengthSqr());
    }

    public double horizontalDistanceSqr() {
        return this.x * this.x + this.z * this.z;
    }

    public double distanceTo(Vec3 other) {
        return subtract(other).length();
    }

    public Vec3 normalize() {
        double length = length();
        return length < 1.0E-4D ? ZERO : new Vec3(this.x / length, this.y / length, this.z / length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Vec3))
            return false;
        Vec3 v = (Vec3) o;
        return Double.compare(v.x, this.x) == 0 && Double.compare(v.y, this.y) == 0 && Double.compare(v.z, this.z) == 0;
    }

    @Override
    public int hashCode() {
        long l = Double.doubleToLongBits(this.x);
        int h = (int) (l ^ l >>> 32);
        l = Double.doubleToLongBits(this.y);
        h = 31 * h + (int) (l ^ l >>> 32);
        l = Double.doubleToLongBits(this.z);
        return 31 * h + (int) (l ^ l >>> 32);
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }
}
