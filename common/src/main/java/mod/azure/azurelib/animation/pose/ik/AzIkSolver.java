package mod.azure.azurelib.animation.pose.ik;

import com.mojang.math.Vector3f;

import java.util.List;

import mod.azure.azurelib.animation.pose.AzBoneChain;
import mod.azure.azurelib.animation.pose.AzBoneConstraint;

/**
 * Turns the bones of a chain so its effector reaches a target. Solvers work on the chain's working rotations only; the
 * caller applies the result. See {@link AzIkSolvers} for the built-in solvers.
 */
@FunctionalInterface
public interface AzIkSolver {

    /**
     * @param chain       the chain to solve, already loaded with the current pose
     * @param effector    the point on the chain's last bone that should reach the target
     * @param target      the model-space target, in blocks
     * @param constraints constraints to apply to chain bones after each step
     */
    void solve(AzBoneChain chain, AzIkEffector effector, Vector3f target, List<AzBoneConstraint> constraints);
}
