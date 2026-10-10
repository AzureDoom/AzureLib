/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.object;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.util.math.Matrix3f;
import mod.azure.azurelib.util.math.Matrix4f;
import mod.azure.azurelib.util.math.Vec3;
import mod.azure.azurelib.util.math.Vector3f;

/**
 * Baked cuboid for a {@link AzBone}
 */
public final class GeoCube {

    private final GeoQuad[] quads;

    private final Vec3 pivot;

    private final Vec3 rotation;

    private final Vec3 size;

    private final double inflate;

    private final boolean mirror;

    private final Transform transform;

    private final int normalFlips;

    public GeoCube(
        GeoQuad[] quads,
        Vec3 pivot,
        Vec3 rotation,
        Vec3 size,
        double inflate,
        boolean mirror,
        Transform transform,
        int normalFlips
    ) {
        this.quads = quads;
        this.pivot = pivot;
        this.rotation = rotation;
        this.size = size;
        this.inflate = inflate;
        this.mirror = mirror;
        this.transform = transform;
        this.normalFlips = normalFlips;
    }

    public GeoQuad[] quads() {
        return this.quads;
    }

    public Vec3 pivot() {
        return this.pivot;
    }

    public Vec3 rotation() {
        return this.rotation;
    }

    public Vec3 size() {
        return this.size;
    }

    public double inflate() {
        return this.inflate;
    }

    public boolean mirror() {
        return this.mirror;
    }

    public Transform transform() {
        return this.transform;
    }

    public int normalFlips() {
        return this.normalFlips;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GeoCube))
            return false;
        GeoCube other = (GeoCube) o;
        return java.util.Objects.equals(this.quads, other.quads)
            && java.util.Objects.equals(this.pivot, other.pivot)
            && java.util.Objects.equals(this.rotation, other.rotation)
            && java.util.Objects.equals(this.size, other.size)
            && Double.compare(this.inflate, other.inflate) == 0
            && this.mirror == other.mirror
            && java.util.Objects.equals(this.transform, other.transform)
            && this.normalFlips == other.normalFlips;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.quads);
        result = 31 * result + java.util.Objects.hashCode(this.pivot);
        result = 31 * result + java.util.Objects.hashCode(this.rotation);
        result = 31 * result + java.util.Objects.hashCode(this.size);
        result = 31 * result + Double.hashCode(this.inflate);
        result = 31 * result + Boolean.hashCode(this.mirror);
        result = 31 * result + java.util.Objects.hashCode(this.transform);
        result = 31 * result + Integer.hashCode(this.normalFlips);
        return result;
    }

    @Override
    public String toString() {
        return "GeoCube[quads=" + this.quads + ", pivot=" + this.pivot + ", rotation=" + this.rotation + ", size="
            + this.size + ", inflate=" + this.inflate + ", mirror=" + this.mirror + ", transform=" + this.transform
            + ", normalFlips=" + this.normalFlips + "]";
    }

    /** {@link #normalFlips} bit: flip a negative X normal. */
    public static final int FLIP_X = 1;

    /** {@link #normalFlips} bit: flip a negative Y normal. */
    public static final int FLIP_Y = 2;

    /** {@link #normalFlips} bit: flip a negative Z normal. */
    public static final int FLIP_Z = 4;

    public GeoCube(GeoQuad[] quads, Vec3 pivot, Vec3 rotation, Vec3 size, double inflate, boolean mirror) {
        this(quads, pivot, rotation, size, inflate, mirror, Transform.of(pivot, rotation), normalFlipsFor(size));
    }

    /**
     * Which normal components {@code RenderUtils#fixInvertedFlatCube} may flip for a cube of this size, worked out once
     * instead of for every quad on every frame. Zero for any cube with volume, which is most of them.
     */
    public static int normalFlipsFor(Vec3 size) {
        boolean flatX = size.x() == 0;
        boolean flatY = size.y() == 0;
        boolean flatZ = size.z() == 0;
        int flips = 0;

        if (flatY || flatZ)
            flips |= FLIP_X;

        if (flatX || flatZ)
            flips |= FLIP_Y;

        if (flatX || flatY)
            flips |= FLIP_Z;

        return flips;
    }

    /**
     * A cube's rotation around its pivot, baked once at load since it never changes:
     * {@code pose = T(pivot) * Rz * Ry * Rx * T(-pivot)} and {@code normal = Rz * Ry * Rx}, matching the order the
     * renderer used to apply them to the pose stack for every cube on every frame.
     *
     * @param pose     the cube-local transform, in model units (pixels / 16)
     * @param normal   the matching normal transform
     * @param identity {@code true} for an unrotated cube, which needs no transform at all
     */
    public static final class Transform {

        private final Matrix4f pose;

        private final Matrix3f normal;

        private final boolean identity;

        public Transform(Matrix4f pose, Matrix3f normal, boolean identity) {
            this.pose = pose;
            this.normal = normal;
            this.identity = identity;
        }

        public Matrix4f pose() {
            return this.pose;
        }

        public Matrix3f normal() {
            return this.normal;
        }

        public boolean identity() {
            return this.identity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Transform))
                return false;
            Transform other = (Transform) o;
            return java.util.Objects.equals(this.pose, other.pose)
                && java.util.Objects.equals(this.normal, other.normal)
                && this.identity == other.identity;
        }

        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + java.util.Objects.hashCode(this.pose);
            result = 31 * result + java.util.Objects.hashCode(this.normal);
            result = 31 * result + Boolean.hashCode(this.identity);
            return result;
        }

        @Override
        public String toString() {
            return "Transform[pose=" + this.pose + ", normal=" + this.normal + ", identity=" + this.identity + "]";
        }

        public static final Transform IDENTITY = new Transform(identity4(), identity3(), true);

        public static Transform of(Vec3 pivot, Vec3 rotation) {
            float rotX = (float) rotation.x();
            float rotY = (float) rotation.y();
            float rotZ = (float) rotation.z();

            if (rotX == 0f && rotY == 0f && rotZ == 0f) {
                return IDENTITY;
            }

            float pivotX = (float) pivot.x() / 16f;
            float pivotY = (float) pivot.y() / 16f;
            float pivotZ = (float) pivot.z() / 16f;

            Matrix4f pose = Matrix4f.createTranslateMatrix(pivotX, pivotY, pivotZ);
            Matrix3f normal = identity3();

            if (rotZ != 0f) {
                pose.multiply(Vector3f.ZP.rotation(rotZ));
                normal.mul(Vector3f.ZP.rotation(rotZ));
            }

            if (rotY != 0f) {
                pose.multiply(Vector3f.YP.rotation(rotY));
                normal.mul(Vector3f.YP.rotation(rotY));
            }

            if (rotX != 0f) {
                pose.multiply(Vector3f.XP.rotation(rotX));
                normal.mul(Vector3f.XP.rotation(rotX));
            }

            pose.multiplyWithTranslation(-pivotX, -pivotY, -pivotZ);

            return new Transform(pose, normal, false);
        }

        private static Matrix4f identity4() {
            Matrix4f matrix = new Matrix4f();
            matrix.setIdentity();
            return matrix;
        }

        private static Matrix3f identity3() {
            Matrix3f matrix = new Matrix3f();
            matrix.setIdentity();
            return matrix;
        }
    }
}
