package mod.azure.azurelib.animation.controller;

/**
 * How a controller's animation combines with what earlier controllers wrote to the same bone this frame (or with the
 * bind pose if nothing did). Controllers are applied in the order they were added, and the result is scaled by the
 * controller's weight.
 */
public enum AzBlendMode {

    /**
     * Blends toward this controller's pose: at weight 1 the pose replaces what is underneath, at 0.5 it lands halfway.
     * The default, and the only behaviour before blending existed.
     */
    OVERRIDE,

    /**
     * Adds this controller's movement on top of what is underneath, e.g. breathing or recoil over a walk cycle.
     * Rotation and position add their offset from the bind pose; scale multiplies by its ratio to the bind scale.
     * Weight scales the added amount.
     */
    ADDITIVE
}
