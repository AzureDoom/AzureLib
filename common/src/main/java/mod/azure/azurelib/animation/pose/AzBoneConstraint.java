package mod.azure.azurelib.animation.pose;

import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * A rule a bone's local rotation must obey, such as a knee that only bends one way. Constraints are applied by IK
 * solvers after every step, and can also run on their own as a pose modifier through
 * {@link AzBoneConstraints#modifier}.
 *
 * @see AzBoneConstraints
 */
public interface AzBoneConstraint {

    String boneName();

    /**
     * Adjusts {@code rotation} in place so it obeys this constraint.
     *
     * @param rotation     the bone's local rotation in radians, in AzBone's convention
     * @param bindRotation the bone's bind rotation, in the same convention
     */
    void constrain(Vector3f rotation, Vector3fc bindRotation);
}
