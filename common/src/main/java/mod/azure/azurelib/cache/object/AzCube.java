/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.object;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import mod.azure.azurelib.model.AzBone;

/**
 * Baked cuboid for a {@link AzBone}
 */
public record AzCube(
    AzQuad[] quads,
    Vec3 pivot,
    Vec3 rotation,
    Vec3 size,
    double inflate,
    boolean mirror,
    Transform transform
) {

    public AzCube(AzQuad[] quads, Vec3 pivot, Vec3 rotation, Vec3 size, double inflate, boolean mirror) {
        this(quads, pivot, rotation, size, inflate, mirror, Transform.of(pivot, rotation));
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
