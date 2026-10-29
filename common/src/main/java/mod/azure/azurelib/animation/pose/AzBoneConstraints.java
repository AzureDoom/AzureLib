package mod.azure.azurelib.animation.pose;

import com.mojang.math.Vector3f;

import java.util.List;
import java.util.Objects;

/**
 * Ready-made {@link AzBoneConstraint}s.
 * <p>
 * Angles are in degrees, relative to the bone's bind pose and in the same convention as Blockbench's rotation fields
 * and your keyframes: a limit of {@code 0..120} on X allows exactly the rotations a keyframe of {@code [0..120, 0, 0]}
 * would produce. (AzureLib stores X and Y negated internally; the constraints convert.)
 * </p>
 */
public final class AzBoneConstraints {

    private static final float DEG_TO_RAD = (float) (Math.PI / 180);

    private AzBoneConstraints() {}

    public static AzBoneConstraint limit(
        String boneName,
        float minX,
        float maxX,
        float minY,
        float maxY,
        float minZ,
        float maxZ
    ) {
        Objects.requireNonNull(boneName, "boneName");
        // Internal X and Y are negated, so the Blockbench range [min, max] becomes [-max, -min].
        return new Limit(
            boneName,
            -maxX * DEG_TO_RAD,
            -minX * DEG_TO_RAD,
            -maxY * DEG_TO_RAD,
            -minY * DEG_TO_RAD,
            minZ * DEG_TO_RAD,
            maxZ * DEG_TO_RAD
        );
    }

    /** A hinge around X (e.g. a knee or elbow): X clamped to the range, Y and Z held at the bind pose. */
    public static AzBoneConstraint hingeX(String boneName, float minDegrees, float maxDegrees) {
        return limit(boneName, minDegrees, maxDegrees, 0, 0, 0, 0);
    }

    /** A hinge around Y: Y clamped to the range, X and Z held at the bind pose. */
    public static AzBoneConstraint hingeY(String boneName, float minDegrees, float maxDegrees) {
        return limit(boneName, 0, 0, minDegrees, maxDegrees, 0, 0);
    }

    /** A hinge around Z: Z clamped to the range, X and Y held at the bind pose. */
    public static AzBoneConstraint hingeZ(String boneName, float minDegrees, float maxDegrees) {
        return limit(boneName, 0, 0, 0, 0, minDegrees, maxDegrees);
    }

    /**
     * Runs constraints as a pose modifier, clamping whatever the animation and earlier modifiers produced. Weighted
     * like any modifier: at 0.5 the bone lands halfway between its pose and the clamped one.
     */
    public static <T> AzPoseModifier<T> modifier(AzBoneConstraint... constraints) {
        List<AzBoneConstraint> list = List.of(constraints);
        Vector3f scratch = new Vector3f();

        return pose -> {
            for (AzBoneConstraint constraint : list) {
                AzPoseBone bone = pose.bone(constraint.boneName());

                if (!bone.isWritable()) {
                    continue;
                }

                scratch.load(bone.currentRotation());
                constraint.constrain(scratch, bone.bindRotation());
                bone.setRotation(scratch.x, scratch.y, scratch.z);
            }
        };
    }

    private static final class Limit implements AzBoneConstraint {

        private final String boneName;

        private final float minX;

        private final float maxX;

        private final float minY;

        private final float maxY;

        private final float minZ;

        private final float maxZ;

        private Limit(
            String boneName,
            float minX,
            float maxX,
            float minY,
            float maxY,
            float minZ,
            float maxZ
        ) {
            this.boneName = boneName;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }

        public String boneName() {
            return boneName;
        }

        public float minX() {
            return minX;
        }

        public float maxX() {
            return maxX;
        }

        public float minY() {
            return minY;
        }

        public float maxY() {
            return maxY;
        }

        public float minZ() {
            return minZ;
        }

        public float maxZ() {
            return maxZ;
        }

        @Override
        public void constrain(Vector3f rotation, Vector3f bind) {
            rotation.set(
                clamp(rotation.x - bind.x(), minX, maxX) + bind.x(),
                clamp(rotation.y - bind.y(), minY, maxY) + bind.y(),
                clamp(rotation.z - bind.z(), minZ, maxZ) + bind.z()
            );
        }

        private static float clamp(float value, float min, float max) {
            return value < min ? min : Math.min(value, max);
        }
    }
}
