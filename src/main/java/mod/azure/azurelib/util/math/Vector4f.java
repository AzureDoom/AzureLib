package mod.azure.azurelib.util.math;

/**
 * Mutable float 4-vector, mirroring the 1.18 {@code com.mojang.math.Vector4f} API.
 */
public final class Vector4f {

    public float x;

    public float y;

    public float z;

    public float w;

    public Vector4f() {}

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4f(Vector3f vector) {
        this(vector.x(), vector.y(), vector.z(), 1.0F);
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

    public float w() {
        return this.w;
    }

    public void mul(Vector3f vector) {
        this.x *= vector.x();
        this.y *= vector.y();
        this.z *= vector.z();
    }

    public void set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public float dot(Vector4f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w;
    }

    public boolean normalize() {
        float lengthSq = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
        if (lengthSq < 1.0E-5F) {
            return false;
        }
        float inv = Mth.fastInvSqrt(lengthSq);
        this.x *= inv;
        this.y *= inv;
        this.z *= inv;
        this.w *= inv;
        return true;
    }

    /** this = matrix * this */
    public void transform(Matrix4f matrix) {
        float ox = this.x, oy = this.y, oz = this.z, ow = this.w;
        this.x = matrix.m00 * ox + matrix.m01 * oy + matrix.m02 * oz + matrix.m03 * ow;
        this.y = matrix.m10 * ox + matrix.m11 * oy + matrix.m12 * oz + matrix.m13 * ow;
        this.z = matrix.m20 * ox + matrix.m21 * oy + matrix.m22 * oz + matrix.m23 * ow;
        this.w = matrix.m30 * ox + matrix.m31 * oy + matrix.m32 * oz + matrix.m33 * ow;
    }

    public void transform(Quaternion quaternion) {
        Vector3f v = new Vector3f(this.x, this.y, this.z);
        v.transform(quaternion);
        this.set(v.x(), v.y(), v.z(), this.w);
    }

    public void perspectiveDivide() {
        this.x /= this.w;
        this.y /= this.w;
        this.z /= this.w;
        this.w = 1.0F;
    }

    public void lerp(Vector4f target, float delta) {
        float f = 1.0F - delta;
        this.x = this.x * f + target.x * delta;
        this.y = this.y * f + target.y * delta;
        this.z = this.z * f + target.z * delta;
        this.w = this.w * f + target.w * delta;
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + "]";
    }
}
