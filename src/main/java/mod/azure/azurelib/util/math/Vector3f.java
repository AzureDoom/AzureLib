package mod.azure.azurelib.util.math;

/**
 * Mutable float 3-vector. A Java 8 / 1.7.10 re-implementation of the 1.18 {@code com.mojang.math.Vector3f} API that
 * AzureLib's renderer is written against.
 */
public final class Vector3f {

    public static final Vector3f XN = new Vector3f(-1.0F, 0.0F, 0.0F);

    public static final Vector3f XP = new Vector3f(1.0F, 0.0F, 0.0F);

    public static final Vector3f YN = new Vector3f(0.0F, -1.0F, 0.0F);

    public static final Vector3f YP = new Vector3f(0.0F, 1.0F, 0.0F);

    public static final Vector3f ZN = new Vector3f(0.0F, 0.0F, -1.0F);

    public static final Vector3f ZP = new Vector3f(0.0F, 0.0F, 1.0F);

    public static final Vector3f ZERO = new Vector3f(0.0F, 0.0F, 0.0F);

    public float x;

    public float y;

    public float z;

    public Vector3f() {}

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f(Vector4f vector) {
        this(vector.x(), vector.y(), vector.z());
    }

    public Vector3f(Vec3 vec) {
        this((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float z() {
        return this.z;
    }

    public void mul(float multiplier) {
        this.x *= multiplier;
        this.y *= multiplier;
        this.z *= multiplier;
    }

    public void mul(float mx, float my, float mz) {
        this.x *= mx;
        this.y *= my;
        this.z *= mz;
    }

    public void clamp(float min, float max) {
        this.x = Mth.clamp(this.x, min, max);
        this.y = Mth.clamp(this.y, min, max);
        this.z = Mth.clamp(this.z, min, max);
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void load(Vector3f other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public void add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public void add(Vector3f other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
    }

    public void sub(Vector3f other) {
        this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
    }

    public float dot(Vector3f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public boolean normalize() {
        float lengthSq = this.x * this.x + this.y * this.y + this.z * this.z;
        if (lengthSq < 1.0E-5F) {
            return false;
        }
        float inv = Mth.fastInvSqrt(lengthSq);
        this.x *= inv;
        this.y *= inv;
        this.z *= inv;
        return true;
    }

    public void cross(Vector3f other) {
        float ax = this.x, ay = this.y, az = this.z;
        this.x = ay * other.z - az * other.y;
        this.y = az * other.x - ax * other.z;
        this.z = ax * other.y - ay * other.x;
    }

    /** this = matrix * this */
    public void transform(Matrix3f matrix) {
        float ox = this.x, oy = this.y, oz = this.z;
        this.x = matrix.m00 * ox + matrix.m01 * oy + matrix.m02 * oz;
        this.y = matrix.m10 * ox + matrix.m11 * oy + matrix.m12 * oz;
        this.z = matrix.m20 * ox + matrix.m21 * oy + matrix.m22 * oz;
    }

    public void transform(Quaternion quaternion) {
        Quaternion q = new Quaternion(quaternion);
        q.mul(new Quaternion(this.x, this.y, this.z, 0.0F));
        Quaternion conj = new Quaternion(quaternion);
        conj.conj();
        q.mul(conj);
        this.set(q.i(), q.j(), q.k());
    }

    public void lerp(Vector3f target, float delta) {
        float f = 1.0F - delta;
        this.x = this.x * f + target.x * delta;
        this.y = this.y * f + target.y * delta;
        this.z = this.z * f + target.z * delta;
    }

    public Quaternion rotation(float radians) {
        return new Quaternion(this, radians, false);
    }

    public Quaternion rotationDegrees(float degrees) {
        return new Quaternion(this, degrees, true);
    }

    public Vector3f copy() {
        return new Vector3f(this.x, this.y, this.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Vector3f))
            return false;
        Vector3f other = (Vector3f) o;
        return Float.compare(other.x, this.x) == 0 && Float.compare(other.y, this.y) == 0 && Float.compare(
            other.z,
            this.z
        ) == 0;
    }

    @Override
    public int hashCode() {
        int i = Float.floatToIntBits(this.x);
        i = 31 * i + Float.floatToIntBits(this.y);
        return 31 * i + Float.floatToIntBits(this.z);
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }
}
