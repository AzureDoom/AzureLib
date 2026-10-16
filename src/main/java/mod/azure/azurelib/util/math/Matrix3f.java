package mod.azure.azurelib.util.math;

/**
 * Mutable row-major 3x3 float matrix, mirroring the 1.18 {@code com.mojang.math.Matrix3f} API. Like the original, the
 * no-arg constructor produces a zero matrix; call {@link #setIdentity()} for an identity matrix.
 */
public final class Matrix3f {

    public float m00;

    public float m01;

    public float m02;

    public float m10;

    public float m11;

    public float m12;

    public float m20;

    public float m21;

    public float m22;

    public Matrix3f() {}

    public Matrix3f(Quaternion q) {
        float i = q.i(), j = q.j(), k = q.k(), r = q.r();
        float ii = 2.0F * i * i;
        float jj = 2.0F * j * j;
        float kk = 2.0F * k * k;
        this.m00 = 1.0F - jj - kk;
        this.m11 = 1.0F - kk - ii;
        this.m22 = 1.0F - ii - jj;
        float ij = i * j, jk = j * k, ki = k * i, ir = i * r, jr = j * r, kr = k * r;
        this.m10 = 2.0F * (ij + kr);
        this.m01 = 2.0F * (ij - kr);
        this.m20 = 2.0F * (ki - jr);
        this.m02 = 2.0F * (ki + jr);
        this.m21 = 2.0F * (jk + ir);
        this.m12 = 2.0F * (jk - ir);
    }

    public Matrix3f(Matrix4f matrix) {
        this.m00 = matrix.m00;
        this.m01 = matrix.m01;
        this.m02 = matrix.m02;
        this.m10 = matrix.m10;
        this.m11 = matrix.m11;
        this.m12 = matrix.m12;
        this.m20 = matrix.m20;
        this.m21 = matrix.m21;
        this.m22 = matrix.m22;
    }

    public Matrix3f(Matrix3f other) {
        this.load(other);
    }

    public static Matrix3f createScaleMatrix(float x, float y, float z) {
        Matrix3f m = new Matrix3f();
        m.m00 = x;
        m.m11 = y;
        m.m22 = z;
        return m;
    }

    public void load(Matrix3f other) {
        this.m00 = other.m00;
        this.m01 = other.m01;
        this.m02 = other.m02;
        this.m10 = other.m10;
        this.m11 = other.m11;
        this.m12 = other.m12;
        this.m20 = other.m20;
        this.m21 = other.m21;
        this.m22 = other.m22;
    }

    public void setIdentity() {
        this.m00 = 1.0F;
        this.m01 = 0.0F;
        this.m02 = 0.0F;
        this.m10 = 0.0F;
        this.m11 = 1.0F;
        this.m12 = 0.0F;
        this.m20 = 0.0F;
        this.m21 = 0.0F;
        this.m22 = 1.0F;
    }

    public void transpose() {
        float t = this.m10;
        this.m10 = this.m01;
        this.m01 = t;
        t = this.m20;
        this.m20 = this.m02;
        this.m02 = t;
        t = this.m21;
        this.m21 = this.m12;
        this.m12 = t;
    }

    public float determinant() {
        return this.m00 * (this.m11 * this.m22 - this.m12 * this.m21) - this.m01 * (this.m10 * this.m22 - this.m12
            * this.m20) + this.m02 * (this.m10 * this.m21 - this.m11 * this.m20);
    }

    public boolean invert() {
        float det = determinant();
        if (Math.abs(det) <= 1.0E-6F) {
            return false;
        }
        float inv = 1.0F / det;
        float a00 = (this.m11 * this.m22 - this.m12 * this.m21) * inv;
        float a01 = (this.m02 * this.m21 - this.m01 * this.m22) * inv;
        float a02 = (this.m01 * this.m12 - this.m02 * this.m11) * inv;
        float a10 = (this.m12 * this.m20 - this.m10 * this.m22) * inv;
        float a11 = (this.m00 * this.m22 - this.m02 * this.m20) * inv;
        float a12 = (this.m02 * this.m10 - this.m00 * this.m12) * inv;
        float a20 = (this.m10 * this.m21 - this.m11 * this.m20) * inv;
        float a21 = (this.m01 * this.m20 - this.m00 * this.m21) * inv;
        float a22 = (this.m00 * this.m11 - this.m01 * this.m10) * inv;
        this.m00 = a00;
        this.m01 = a01;
        this.m02 = a02;
        this.m10 = a10;
        this.m11 = a11;
        this.m12 = a12;
        this.m20 = a20;
        this.m21 = a21;
        this.m22 = a22;
        return true;
    }

    /** this = this * other */
    public void mul(Matrix3f other) {
        float a00 = this.m00 * other.m00 + this.m01 * other.m10 + this.m02 * other.m20;
        float a01 = this.m00 * other.m01 + this.m01 * other.m11 + this.m02 * other.m21;
        float a02 = this.m00 * other.m02 + this.m01 * other.m12 + this.m02 * other.m22;
        float a10 = this.m10 * other.m00 + this.m11 * other.m10 + this.m12 * other.m20;
        float a11 = this.m10 * other.m01 + this.m11 * other.m11 + this.m12 * other.m21;
        float a12 = this.m10 * other.m02 + this.m11 * other.m12 + this.m12 * other.m22;
        float a20 = this.m20 * other.m00 + this.m21 * other.m10 + this.m22 * other.m20;
        float a21 = this.m20 * other.m01 + this.m21 * other.m11 + this.m22 * other.m21;
        float a22 = this.m20 * other.m02 + this.m21 * other.m12 + this.m22 * other.m22;
        this.m00 = a00;
        this.m01 = a01;
        this.m02 = a02;
        this.m10 = a10;
        this.m11 = a11;
        this.m12 = a12;
        this.m20 = a20;
        this.m21 = a21;
        this.m22 = a22;
    }

    /** this = this * rotation(quaternion), in place (no allocation). */
    public void mul(Quaternion quaternion) {
        float i = quaternion.i(), j = quaternion.j(), k = quaternion.k(), r = quaternion.r();
        float ii = 2.0F * i * i, jj = 2.0F * j * j, kk = 2.0F * k * k;
        float ij = i * j, jk = j * k, ki = k * i, ir = i * r, jr = j * r, kr = k * r;
        this.mulRotation(
            1.0F - jj - kk,
            2.0F * (ij - kr),
            2.0F * (ki + jr),
            2.0F * (ij + kr),
            1.0F - kk - ii,
            2.0F * (jk - ir),
            2.0F * (ki - jr),
            2.0F * (jk + ir),
            1.0F - ii - jj
        );
    }

    /** this = this * R, with R given row-major. */
    public void mulRotation(
        float r00,
        float r01,
        float r02,
        float r10,
        float r11,
        float r12,
        float r20,
        float r21,
        float r22
    ) {
        float a0 = this.m00, a1 = this.m01, a2 = this.m02;
        this.m00 = a0 * r00 + a1 * r10 + a2 * r20;
        this.m01 = a0 * r01 + a1 * r11 + a2 * r21;
        this.m02 = a0 * r02 + a1 * r12 + a2 * r22;
        a0 = this.m10;
        a1 = this.m11;
        a2 = this.m12;
        this.m10 = a0 * r00 + a1 * r10 + a2 * r20;
        this.m11 = a0 * r01 + a1 * r11 + a2 * r21;
        this.m12 = a0 * r02 + a1 * r12 + a2 * r22;
        a0 = this.m20;
        a1 = this.m21;
        a2 = this.m22;
        this.m20 = a0 * r00 + a1 * r10 + a2 * r20;
        this.m21 = a0 * r01 + a1 * r11 + a2 * r21;
        this.m22 = a0 * r02 + a1 * r12 + a2 * r22;
    }

    /** this = this * scale(x, y, z), in place: scales each column. */
    public void mulScale(float x, float y, float z) {
        this.m00 *= x;
        this.m10 *= x;
        this.m20 *= x;
        this.m01 *= y;
        this.m11 *= y;
        this.m21 *= y;
        this.m02 *= z;
        this.m12 *= z;
        this.m22 *= z;
    }

    public void mul(float multiplier) {
        this.m00 *= multiplier;
        this.m01 *= multiplier;
        this.m02 *= multiplier;
        this.m10 *= multiplier;
        this.m11 *= multiplier;
        this.m12 *= multiplier;
        this.m20 *= multiplier;
        this.m21 *= multiplier;
        this.m22 *= multiplier;
    }

    public void set(int row, int col, float value) {
        switch (row * 3 + col) {
            case 0:
                this.m00 = value;
                break;
            case 1:
                this.m01 = value;
                break;
            case 2:
                this.m02 = value;
                break;
            case 3:
                this.m10 = value;
                break;
            case 4:
                this.m11 = value;
                break;
            case 5:
                this.m12 = value;
                break;
            case 6:
                this.m20 = value;
                break;
            case 7:
                this.m21 = value;
                break;
            case 8:
                this.m22 = value;
                break;
            default:
                throw new IndexOutOfBoundsException(row + "," + col);
        }
    }

    public Matrix3f copy() {
        return new Matrix3f(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Matrix3f))
            return false;
        Matrix3f m = (Matrix3f) o;
        return Float.compare(m.m00, this.m00) == 0 && Float.compare(m.m01, this.m01) == 0 && Float.compare(
            m.m02,
            this.m02
        ) == 0 && Float.compare(m.m10, this.m10) == 0 && Float.compare(m.m11, this.m11) == 0 && Float.compare(
            m.m12,
            this.m12
        ) == 0 && Float.compare(m.m20, this.m20) == 0 && Float.compare(m.m21, this.m21) == 0 && Float.compare(
            m.m22,
            this.m22
        ) == 0;
    }

    @Override
    public int hashCode() {
        int h = Float.floatToIntBits(this.m00);
        h = 31 * h + Float.floatToIntBits(this.m01);
        h = 31 * h + Float.floatToIntBits(this.m02);
        h = 31 * h + Float.floatToIntBits(this.m10);
        h = 31 * h + Float.floatToIntBits(this.m11);
        h = 31 * h + Float.floatToIntBits(this.m12);
        h = 31 * h + Float.floatToIntBits(this.m20);
        h = 31 * h + Float.floatToIntBits(this.m21);
        return 31 * h + Float.floatToIntBits(this.m22);
    }
}
