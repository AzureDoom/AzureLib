package mod.azure.azurelib.animation.pose.ik;

import mod.azure.azurelib.animation.pose.AzBoneChain;
import org.joml.Vector3f;

/**
 * The point of a chain that IK moves onto the target: a fixed offset, in Blockbench pixels, from the pivot of the
 * chain's last bone. For a leg chain ending in the foot bone, {@code (0, 0, 0)} is the ankle and {@code (0, -1, 0)} a
 * point just below it.
 */
public record AzIkEffector(
    float x,
    float y,
    float z
) {

    public static final AzIkEffector PIVOT = new AzIkEffector(0, 0, 0);

    public Vector3f position(AzBoneChain chain, Vector3f dest) {
        return chain.pointOnBone(chain.size() - 1, x, y, z, dest);
    }
}
