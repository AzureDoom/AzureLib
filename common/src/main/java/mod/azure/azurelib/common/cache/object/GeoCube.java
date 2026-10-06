/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.cache.object;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import mod.azure.azurelib.common.model.AzBone;

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
        Matrix4fc pose,
        Matrix3fc normal,
        boolean identity
    ) {

        public static final Transform IDENTITY = new Transform(new Matrix4f(), new Matrix3f(), true);

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

            var pose = new Matrix4f().translate(pivotX, pivotY, pivotZ)
                .rotateZ(rotZ)
                .rotateY(rotY)
                .rotateX(rotX)
                .translate(-pivotX, -pivotY, -pivotZ);
            var normal = new Matrix3f().rotateZ(rotZ).rotateY(rotY).rotateX(rotX);

            return new Transform(pose, normal, false);
        }
    }
}
