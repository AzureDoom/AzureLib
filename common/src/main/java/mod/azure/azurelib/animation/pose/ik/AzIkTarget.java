package mod.azure.azurelib.animation.pose.ik;

import com.mojang.math.Vector3f;

import mod.azure.azurelib.animation.pose.AzPoseContext;

/**
 * Where an IK chain or look-at should aim this frame, in model space. See {@link AzIkTargets} for common targets.
 *
 * @param <T> the animatable type
 */
@FunctionalInterface
public interface AzIkTarget<T> {

    /**
     * @param pose     the running modifier's context
     * @param effector the effector's current (animated) model-space position, e.g. to search for ground below a foot
     * @param dest     receives the model-space target, in blocks
     * @return {@code false} to skip solving this frame, leaving the bones as animated
     */
    boolean resolve(AzPoseContext<T> pose, Vector3f effector, Vector3f dest);
}
