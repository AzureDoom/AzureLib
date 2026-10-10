package mod.azure.azurelib.util.math;

/**
 * Mutable quaternion (i, j, k, r), mirroring the 1.18 {@code com.mojang.math.Quaternion} API.
 */
public final class Quaternion {

    public static final Quaternion ONE = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);

    private float i;

    private float j;

    private float k;

    private float r;

    public Quaternion(float i, float j, float k, float r) {
        this.i = i;
        this.j = j;
        this.k = k;
        this.r = r;
    }

    public Quaternion(Vector3f axis, float angle, boolean degrees) {
        if (degrees) {
            angle *= (float) (Math.PI / 180D);
        }
        float s = (float) Math.sin(angle / 2.0F);
        this.i = axis.x() * s;
        this.j = axis.y() * s;
        this.k = axis.z() * s;
        this.r = (float) Math.cos(angle / 2.0F);
    }

    public Quaternion(float x, float y, float z, boolean degrees) {
        if (degrees) {
            x *= (float) (Math.PI / 180D);
            y *= (float) (Math.PI / 180D);
            z *= (float) (Math.PI / 180D);
        }
        float sx = (float) Math.sin(0.5F * x);
        float cx = (float) Math.cos(0.5F * x);
        float sy = (float) Math.sin(0.5F * y);
        float cy = (float) Math.cos(0.5F * y);
        float sz = (float) Math.sin(0.5F * z);
        float cz = (float) Math.cos(0.5F * z);
        this.i = sx * cy * cz + cx * sy * sz;
        this.j = cx * sy * cz - sx * cy * sz;
        this.k = sx * sy * cz + cx * cy * sz;
        this.r = cx * cy * cz - sx * sy * sz;
    }

    public Quaternion(Quaternion other) {
        this(other.i, other.j, other.k, other.r);
    }

    public static Quaternion fromXYZ(float x, float y, float z) {
        Quaternion q = ONE.copy();
        q.mul(new Quaternion((float) Math.sin(x / 2.0F), 0.0F, 0.0F, (float) Math.cos(x / 2.0F)));
        q.mul(new Quaternion(0.0F, (float) Math.sin(y / 2.0F), 0.0F, (float) Math.cos(y / 2.0F)));
        q.mul(new Quaternion(0.0F, 0.0F, (float) Math.sin(z / 2.0F), (float) Math.cos(z / 2.0F)));
        return q;
    }

    public float i() {
        return this.i;
    }

    public float j() {
        return this.j;
    }

    public float k() {
        return this.k;
    }

    public float r() {
        return this.r;
    }

    /** this = this * other */
    public void mul(Quaternion other) {
        float ai = this.i, aj = this.j, ak = this.k, ar = this.r;
        float bi = other.i, bj = other.j, bk = other.k, br = other.r;
        this.i = ar * bi + ai * br + aj * bk - ak * bj;
        this.j = ar * bj - ai * bk + aj * br + ak * bi;
        this.k = ar * bk + ai * bj - aj * bi + ak * br;
        this.r = ar * br - ai * bi - aj * bj - ak * bk;
    }

    public void mul(float multiplier) {
        this.i *= multiplier;
        this.j *= multiplier;
        this.k *= multiplier;
        this.r *= multiplier;
    }

    public void conj() {
        this.i = -this.i;
        this.j = -this.j;
        this.k = -this.k;
    }

    public void set(float i, float j, float k, float r) {
        this.i = i;
        this.j = j;
        this.k = k;
        this.r = r;
    }

    public void normalize() {
        float lengthSq = this.i * this.i + this.j * this.j + this.k * this.k + this.r * this.r;
        if (lengthSq > 1.0E-6F) {
            float inv = Mth.fastInvSqrt(lengthSq);
            this.i *= inv;
            this.j *= inv;
            this.k *= inv;
            this.r *= inv;
        } else {
            this.i = 0.0F;
            this.j = 0.0F;
            this.k = 0.0F;
            this.r = 0.0F;
        }
    }

    public Quaternion copy() {
        return new Quaternion(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Quaternion))
            return false;
        Quaternion q = (Quaternion) o;
        return Float.compare(q.i, this.i) == 0 && Float.compare(q.j, this.j) == 0 && Float.compare(q.k, this.k) == 0
            && Float.compare(q.r, this.r) == 0;
    }

    @Override
    public int hashCode() {
        int h = Float.floatToIntBits(this.i);
        h = 31 * h + Float.floatToIntBits(this.j);
        h = 31 * h + Float.floatToIntBits(this.k);
        return 31 * h + Float.floatToIntBits(this.r);
    }

    @Override
    public String toString() {
        return "Quaternion[" + this.r + " + " + this.i + "i + " + this.j + "j + " + this.k + "k]";
    }
}
