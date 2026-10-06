/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.object;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.world.phys.Vec3;

import mod.azure.azurelib.model.AzBone;

/**
 * Baked cuboid for a {@link AzBone}
 */
public record GeoCube(
    GeoQuad[] quads,
    Vec3 pivot,
    Vec3 rotation,
    Vec3 size,
    double inflate,
    boolean mirror,
    Transform transform,
    int normalFlips
) {

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
        var flatX = size.x() == 0;
        var flatY = size.y() == 0;
        var flatZ = size.z() == 0;
        var flips = 0;

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
    public record Transform(
        Matrix4f pose,
        Matrix3f normal,
        boolean identity
    ) {

        public static final Transform IDENTITY = new Transform(identity4(), identity3(), true);

        public static Transform of(Vec3 pivot, Vec3 rotation) {
            var rotX = (float) rotation.x();
            var rotY = (float) rotation.y();
            var rotZ = (float) rotation.z();

            if (rotX == 0f && rotY == 0f && rotZ == 0f) {
                return IDENTITY;
            }

            var pivotX = (float) pivot.x() / 16f;
            var pivotY = (float) pivot.y() / 16f;
            var pivotZ = (float) pivot.z() / 16f;

            var pose = Matrix4f.createTranslateMatrix(pivotX, pivotY, pivotZ);
            var normal = identity3();

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
            var matrix = new Matrix4f();
            matrix.setIdentity();
            return matrix;
        }

        private static Matrix3f identity3() {
            var matrix = new Matrix3f();
            matrix.setIdentity();
            return matrix;
        }
    }
}
