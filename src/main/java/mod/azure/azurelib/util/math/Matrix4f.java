package mod.azure.azurelib.util.math;

import java.nio.FloatBuffer;

/**
 * Mutable row-major 4x4 float matrix, mirroring the 1.18 {@code com.mojang.math.Matrix4f} API. Like the original, the
 * no-arg constructor produces a zero matrix; call {@link #setIdentity()} for an identity matrix.
 */
public final class Matrix4f {

    public float m00;

    public float m01;

    public float m02;

    public float m03;

    public float m10;

    public float m11;

    public float m12;

    public float m13;

    public float m20;

    public float m21;

    public float m22;

    public float m23;

    public float m30;

    public float m31;

    public float m32;

    public float m33;

    public Matrix4f() {}

    public Matrix4f(Matrix4f other) {
        this.load(other);
    }

    public Matrix4f(Quaternion q) {
        float i = q.i(), j = q.j(), k = q.k(), r = q.r();
        float ii = 2.0F * i * i;
        float jj = 2.0F * j * j;
        float kk = 2.0F * k * k;
        this.m00 = 1.0F - jj - kk;
        this.m11 = 1.0F - kk - ii;
        this.m22 = 1.0F - ii - jj;
        this.m33 = 1.0F;
        float ij = i * j, jk = j * k, ki = k * i, ir = i * r, jr = j * r, kr = k * r;
        this.m10 = 2.0F * (ij + kr);
        this.m01 = 2.0F * (ij - kr);
        this.m20 = 2.0F * (ki - jr);
        this.m02 = 2.0F * (ki + jr);
        this.m21 = 2.0F * (jk + ir);
        this.m12 = 2.0F * (jk - ir);
    }

    public static Matrix4f createScaleMatrix(float x, float y, float z) {
        Matrix4f m = new Matrix4f();
        m.m00 = x;
        m.m11 = y;
        m.m22 = z;
        m.m33 = 1.0F;
        return m;
    }

    public static Matrix4f createTranslateMatrix(float x, float y, float z) {
        Matrix4f m = new Matrix4f();
        m.m00 = 1.0F;
        m.m11 = 1.0F;
        m.m22 = 1.0F;
        m.m33 = 1.0F;
        m.m03 = x;
        m.m13 = y;
        m.m23 = z;
        return m;
    }

    public void load(Matrix4f o) {
        this.m00 = o.m00;
        this.m01 = o.m01;
        this.m02 = o.m02;
        this.m03 = o.m03;
        this.m10 = o.m10;
        this.m11 = o.m11;
        this.m12 = o.m12;
        this.m13 = o.m13;
        this.m20 = o.m20;
        this.m21 = o.m21;
        this.m22 = o.m22;
        this.m23 = o.m23;
        this.m30 = o.m30;
        this.m31 = o.m31;
        this.m32 = o.m32;
        this.m33 = o.m33;
    }

    public void setIdentity() {
        this.m00 = 1.0F;
        this.m01 = 0.0F;
        this.m02 = 0.0F;
        this.m03 = 0.0F;
        this.m10 = 0.0F;
        this.m11 = 1.0F;
        this.m12 = 0.0F;
        this.m13 = 0.0F;
        this.m20 = 0.0F;
        this.m21 = 0.0F;
        this.m22 = 1.0F;
        this.m23 = 0.0F;
        this.m30 = 0.0F;
        this.m31 = 0.0F;
        this.m32 = 0.0F;
        this.m33 = 1.0F;
    }

    public float determinant() {
        float b00 = this.m00 * this.m11 - this.m01 * this.m10;
        float b01 = this.m00 * this.m12 - this.m02 * this.m10;
        float b02 = this.m00 * this.m13 - this.m03 * this.m10;
        float b03 = this.m01 * this.m12 - this.m02 * this.m11;
        float b04 = this.m01 * this.m13 - this.m03 * this.m11;
        float b05 = this.m02 * this.m13 - this.m03 * this.m12;
        float b06 = this.m20 * this.m31 - this.m21 * this.m30;
        float b07 = this.m20 * this.m32 - this.m22 * this.m30;
        float b08 = this.m20 * this.m33 - this.m23 * this.m30;
        float b09 = this.m21 * this.m32 - this.m22 * this.m31;
        float b10 = this.m21 * this.m33 - this.m23 * this.m31;
        float b11 = this.m22 * this.m33 - this.m23 * this.m32;
        return b00 * b11 - b01 * b10 + b02 * b09 + b03 * b08 - b04 * b07 + b05 * b06;
    }

    public boolean invert() {
        float b00 = this.m00 * this.m11 - this.m01 * this.m10;
        float b01 = this.m00 * this.m12 - this.m02 * this.m10;
        float b02 = this.m00 * this.m13 - this.m03 * this.m10;
        float b03 = this.m01 * this.m12 - this.m02 * this.m11;
        float b04 = this.m01 * this.m13 - this.m03 * this.m11;
        float b05 = this.m02 * this.m13 - this.m03 * this.m12;
        float b06 = this.m20 * this.m31 - this.m21 * this.m30;
        float b07 = this.m20 * this.m32 - this.m22 * this.m30;
        float b08 = this.m20 * this.m33 - this.m23 * this.m30;
        float b09 = this.m21 * this.m32 - this.m22 * this.m31;
        float b10 = this.m21 * this.m33 - this.m23 * this.m31;
        float b11 = this.m22 * this.m33 - this.m23 * this.m32;
        float det = b00 * b11 - b01 * b10 + b02 * b09 + b03 * b08 - b04 * b07 + b05 * b06;
        if (Math.abs(det) <= 1.0E-6F) {
            return false;
        }
        float inv = 1.0F / det;
        float a00 = (this.m11 * b11 - this.m12 * b10 + this.m13 * b09) * inv;
        float a01 = (-this.m01 * b11 + this.m02 * b10 - this.m03 * b09) * inv;
        float a02 = (this.m31 * b05 - this.m32 * b04 + this.m33 * b03) * inv;
        float a03 = (-this.m21 * b05 + this.m22 * b04 - this.m23 * b03) * inv;
        float a10 = (-this.m10 * b11 + this.m12 * b08 - this.m13 * b07) * inv;
        float a11 = (this.m00 * b11 - this.m02 * b08 + this.m03 * b07) * inv;
        float a12 = (-this.m30 * b05 + this.m32 * b02 - this.m33 * b01) * inv;
        float a13 = (this.m20 * b05 - this.m22 * b02 + this.m23 * b01) * inv;
        float a20 = (this.m10 * b10 - this.m11 * b08 + this.m13 * b06) * inv;
        float a21 = (-this.m00 * b10 + this.m01 * b08 - this.m03 * b06) * inv;
        float a22 = (this.m30 * b04 - this.m31 * b02 + this.m33 * b00) * inv;
        float a23 = (-this.m20 * b04 + this.m21 * b02 - this.m23 * b00) * inv;
        float a30 = (-this.m10 * b09 + this.m11 * b07 - this.m12 * b06) * inv;
        float a31 = (this.m00 * b09 - this.m01 * b07 + this.m02 * b06) * inv;
        float a32 = (-this.m30 * b03 + this.m31 * b01 - this.m32 * b00) * inv;
        float a33 = (this.m20 * b03 - this.m21 * b01 + this.m22 * b00) * inv;
        this.m00 = a00;
        this.m01 = a01;
        this.m02 = a02;
        this.m03 = a03;
        this.m10 = a10;
        this.m11 = a11;
        this.m12 = a12;
        this.m13 = a13;
        this.m20 = a20;
        this.m21 = a21;
        this.m22 = a22;
        this.m23 = a23;
        this.m30 = a30;
        this.m31 = a31;
        this.m32 = a32;
        this.m33 = a33;
        return true;
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
        t = this.m30;
        this.m30 = this.m03;
        this.m03 = t;
        t = this.m31;
        this.m31 = this.m13;
        this.m13 = t;
        t = this.m32;
        this.m32 = this.m23;
        this.m23 = t;
    }

    /** this = this * other */
    public void multiply(Matrix4f o) {
        float a00 = this.m00 * o.m00 + this.m01 * o.m10 + this.m02 * o.m20 + this.m03 * o.m30;
        float a01 = this.m00 * o.m01 + this.m01 * o.m11 + this.m02 * o.m21 + this.m03 * o.m31;
        float a02 = this.m00 * o.m02 + this.m01 * o.m12 + this.m02 * o.m22 + this.m03 * o.m32;
        float a03 = this.m00 * o.m03 + this.m01 * o.m13 + this.m02 * o.m23 + this.m03 * o.m33;
        float a10 = this.m10 * o.m00 + this.m11 * o.m10 + this.m12 * o.m20 + this.m13 * o.m30;
        float a11 = this.m10 * o.m01 + this.m11 * o.m11 + this.m12 * o.m21 + this.m13 * o.m31;
        float a12 = this.m10 * o.m02 + this.m11 * o.m12 + this.m12 * o.m22 + this.m13 * o.m32;
        float a13 = this.m10 * o.m03 + this.m11 * o.m13 + this.m12 * o.m23 + this.m13 * o.m33;
        float a20 = this.m20 * o.m00 + this.m21 * o.m10 + this.m22 * o.m20 + this.m23 * o.m30;
        float a21 = this.m20 * o.m01 + this.m21 * o.m11 + this.m22 * o.m21 + this.m23 * o.m31;
        float a22 = this.m20 * o.m02 + this.m21 * o.m12 + this.m22 * o.m22 + this.m23 * o.m32;
        float a23 = this.m20 * o.m03 + this.m21 * o.m13 + this.m22 * o.m23 + this.m23 * o.m33;
        float a30 = this.m30 * o.m00 + this.m31 * o.m10 + this.m32 * o.m20 + this.m33 * o.m30;
        float a31 = this.m30 * o.m01 + this.m31 * o.m11 + this.m32 * o.m21 + this.m33 * o.m31;
        float a32 = this.m30 * o.m02 + this.m31 * o.m12 + this.m32 * o.m22 + this.m33 * o.m32;
        float a33 = this.m30 * o.m03 + this.m31 * o.m13 + this.m32 * o.m23 + this.m33 * o.m33;
        this.m00 = a00;
        this.m01 = a01;
        this.m02 = a02;
        this.m03 = a03;
        this.m10 = a10;
        this.m11 = a11;
        this.m12 = a12;
        this.m13 = a13;
        this.m20 = a20;
        this.m21 = a21;
        this.m22 = a22;
        this.m23 = a23;
        this.m30 = a30;
        this.m31 = a31;
        this.m32 = a32;
        this.m33 = a33;
    }

    public void multiply(Quaternion quaternion) {
        this.multiply(new Matrix4f(quaternion));
    }

    public void multiply(float multiplier) {
        this.m00 *= multiplier;
        this.m01 *= multiplier;
        this.m02 *= multiplier;
        this.m03 *= multiplier;
        this.m10 *= multiplier;
        this.m11 *= multiplier;
        this.m12 *= multiplier;
        this.m13 *= multiplier;
        this.m20 *= multiplier;
        this.m21 *= multiplier;
        this.m22 *= multiplier;
        this.m23 *= multiplier;
        this.m30 *= multiplier;
        this.m31 *= multiplier;
        this.m32 *= multiplier;
        this.m33 *= multiplier;
    }

    /** this = this * translate(x, y, z) */
    public void multiplyWithTranslation(float x, float y, float z) {
        this.m03 = this.m00 * x + this.m01 * y + this.m02 * z + this.m03;
        this.m13 = this.m10 * x + this.m11 * y + this.m12 * z + this.m13;
        this.m23 = this.m20 * x + this.m21 * y + this.m22 * z + this.m23;
        this.m33 = this.m30 * x + this.m31 * y + this.m32 * z + this.m33;
    }

    /** Adds the vector directly to the translation column (same semantics as the 1.18 original). */
    public void translate(Vector3f vector) {
        this.m03 += vector.x();
        this.m13 += vector.y();
        this.m23 += vector.z();
    }

    public float m03() {
        return this.m03;
    }

    public float m13() {
        return this.m13;
    }

    public float m23() {
        return this.m23;
    }

    public void setTranslation(float x, float y, float z) {
        this.m03 = x;
        this.m13 = y;
        this.m23 = z;
    }

    public float get(int row, int col) {
        switch (row * 4 + col) {
            case 0:
                return this.m00;
            case 1:
                return this.m01;
            case 2:
                return this.m02;
            case 3:
                return this.m03;
            case 4:
                return this.m10;
            case 5:
                return this.m11;
            case 6:
                return this.m12;
            case 7:
                return this.m13;
            case 8:
                return this.m20;
            case 9:
                return this.m21;
            case 10:
                return this.m22;
            case 11:
                return this.m23;
            case 12:
                return this.m30;
            case 13:
                return this.m31;
            case 14:
                return this.m32;
            case 15:
                return this.m33;
            default:
                throw new IndexOutOfBoundsException(row + "," + col);
        }
    }

    /**
     * Writes this matrix into the buffer in OpenGL's column-major order, suitable for
     * {@code GlStateManager.multMatrix}.
     */
    public void store(FloatBuffer buffer) {
        buffer.put(this.m00);
        buffer.put(this.m10);
        buffer.put(this.m20);
        buffer.put(this.m30);
        buffer.put(this.m01);
        buffer.put(this.m11);
        buffer.put(this.m21);
        buffer.put(this.m31);
        buffer.put(this.m02);
        buffer.put(this.m12);
        buffer.put(this.m22);
        buffer.put(this.m32);
        buffer.put(this.m03);
        buffer.put(this.m13);
        buffer.put(this.m23);
        buffer.put(this.m33);
    }

    public Matrix4f copy() {
        return new Matrix4f(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Matrix4f))
            return false;
        Matrix4f m = (Matrix4f) o;
        for (int i = 0; i < 16; i++) {
            if (Float.compare(m.get(i >> 2, i & 3), this.get(i >> 2, i & 3)) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < 16; i++) {
            h = 31 * h + Float.floatToIntBits(this.get(i >> 2, i & 3));
        }
        return h;
    }

    @Override
    public String toString() {
        return "Matrix4f:\n" + this.m00 + " " + this.m01 + " " + this.m02 + " " + this.m03 + "\n" + this.m10 + " "
            + this.m11 + " " + this.m12 + " " + this.m13 + "\n" + this.m20 + " " + this.m21 + " " + this.m22 + " "
            + this.m23 + "\n" + this.m30 + " " + this.m31 + " " + this.m32 + " " + this.m33 + "\n";
    }
}
