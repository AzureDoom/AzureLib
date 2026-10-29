package mod.azure.azurelib.animation.pose;

/**
 * One step of procedural posing, run every frame after the animation controllers and the bone reset pass. A modifier
 * reads and writes bones through the {@link AzPoseContext} it is given rather than through {@code AzBone} directly, so
 * its changes are weighted, masked, layered with other modifiers, and undone before the next frame's controllers run.
 * Nothing it writes ever feeds back into the animated pose, so additive writes do not accumulate across frames.
 * <p>
 * Register modifiers in {@code AzAnimator#registerPoseModifiers}:
 * </p>
 *
 * <pre>
 * {@code
 *
 * protected void registerPoseModifiers(AzPoseModifierRegistry<MyEntity> modifiers) {
 *     modifiers.add("look", AzPoseModifiers.headLook("neck", "head"));
 *     modifiers.add("tail_jiggle", AzPoseModifiers.spring("tail")).priority(100);
 *     modifiers.add("charge_glow", pose -> {
 *         float charge = pose.animatable().getCharge(pose.partialTick());
 *         pose.bone("core").scale(1 + charge * 0.2f, 1 + charge * 0.2f, 1 + charge * 0.2f);
 *     });
 * }
 * }
 * </pre>
 *
 * @param <T> the animatable type
 */
@FunctionalInterface
public interface AzPoseModifier<T> {

    void apply(AzPoseContext<T> pose);
}
