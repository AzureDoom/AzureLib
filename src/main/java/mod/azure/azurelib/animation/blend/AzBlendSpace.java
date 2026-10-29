package mod.azure.azurelib.animation.blend;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorUpdateHook;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerBuilder;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.controller.AzWeightFade;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequence;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequenceBuilder;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.primitive.AzQueuedAnimation;

/**
 * Blends looping animations placed at points in an input space, driven by per-frame inputs. Each sample plays on its
 * own controller; every frame, before the controllers update, the blend space works out how much each sample counts for
 * the current input and sets the controllers' weights so their layered result is exactly that weighted average. With
 * phase sync on (the default) all samples share one normalized cycle, advancing at the weighted average of their own
 * rates, so a walk and a run blended halfway keep their feet in step at an in-between pace.
 * <p>
 * Add the blend space to the controller container in {@code registerControllers}, where its controllers are layered
 * like any others: controllers added after it (an attack on the upper body, say) blend on top of the blend.
 * </p>
 * <p>
 * Samples should animate the same bones. A sample that doesn't animate a bone contributes that bone's pose from below
 * (the bind pose, or earlier controllers) instead.
 * </p>
 *
 * @param <T> the animatable type
 */
public abstract class AzBlendSpace<T> implements AzAnimatorUpdateHook<T> {

    private final AzAnimator<?, T> animator;

    private final String name;

    private final List<Sample<T>> samples;

    private final boolean syncPhase;

    private final double smoothingTicks;

    private final double[] weights;

    private final double[] controllerWeights;

    private final AzWeightFade fade = new AzWeightFade();

    private double overallWeight = 1;

    private double phase;

    private double lastAnimTime = Double.NaN;

    private boolean added;

    protected AzBlendSpace(AzAnimator<?, T> animator, String name, Settings<T> settings, List<String> animations) {
        this.animator = Objects.requireNonNull(animator, "animator");
        this.name = Objects.requireNonNull(name, "name");
        this.syncPhase = settings.syncPhase;
        this.smoothingTicks = settings.smoothingTicks;

        if (animations.isEmpty()) {
            throw new IllegalArgumentException("Blend space '" + name + "' has no samples");
        }

        HashSet<String> used = new HashSet<>();
        ArrayList<Sample<T>> list = new ArrayList<>(animations.size());

        for (int i = 0; i < animations.size(); i++) {
            String animation = animations.get(i);
            String controllerName = name + "/" + animation;

            if (!used.add(controllerName)) {
                controllerName = controllerName + "#" + i;
                used.add(controllerName);
            }

            AzAnimationControllerBuilder<T> builder = settings.controller.apply(
                AzAnimationController.builder(animator, controllerName)
            );
            AzAnimationSequence sequence = new AzAnimationSequenceBuilder()
                .queue(animation, properties -> properties.withPlayBehavior(AzPlayBehaviors.LOOP))
                .build();
            list.add(new Sample<>(animation, builder.build(), sequence));
        }

        this.samples = Collections.unmodifiableList(new ArrayList<>(list));
        this.weights = new double[samples.size()];
        this.controllerWeights = new double[samples.size()];
    }

    /**
     * Adds the sample controllers to {@code container}, in sample order, and starts updating them. Call it once, from
     * {@code registerControllers}, at the point in the layering where the blend should go.
     */
    public void addTo(AzAnimationControllerContainer<T> container) {
        if (added) {
            throw new IllegalStateException("Blend space '" + name + "' was already added");
        }

        added = true;

        for (Sample<T> sample : samples) {
            container.add(sample.controller);
        }

        animator.addUpdateHook(this);
    }

    public String name() {
        return name;
    }

    public List<AzAnimationController<T>> controllers() {
        return samples.stream().map(Sample::controller).collect(Collectors.toList());
    }

    public double[] weights() {
        return weights;
    }

    public double phase() {
        return phase;
    }

    public void setWeight(double weight) {
        fade.cancel();
        overallWeight = clamp01(weight);
    }

    public void fadeWeight(double target, double lengthTicks) {
        if (lengthTicks <= 0) {
            setWeight(target);
            return;
        }

        fade.start(overallWeight, clamp01(target), lengthTicks);
    }

    public double weight() {
        return overallWeight;
    }

    protected abstract void computeWeights(T animatable, float partialTick, double smoothing, double[] dest);

    protected static double approach(double current, double target, double smoothing) {
        return Double.isNaN(current) ? target : current + (target - current) * smoothing;
    }

    @Override
    public void beforeControllers(T animatable, float partialTicks) {
        double now = animator.context().timer().getAnimTime();
        double dt = Double.isNaN(lastAnimTime) ? 0 : Math.max(0, now - lastAnimTime);
        lastAnimTime = now;

        if (fade.isActive()) {
            overallWeight = fade.update(now);
        }

        double smoothing = smoothingTicks <= 0 ? 1 : 1 - Math.exp(-dt / smoothingTicks);
        computeWeights(animatable, partialTicks, smoothing, weights);
        AzBlendWeights.layered(weights, overallWeight, controllerWeights);

        for (int i = 0; i < samples.size(); i++) {
            Sample<T> sample = samples.get(i);
            AzAnimationController<T> controller = sample.controller;
            controller.setWeight(controllerWeights[i]);

            if (controller.stateMachine().isStopped() || controller.currentSequence() != sample.sequence) {
                controller.run(AzDispatchSide.CLIENT, sample.sequence);
            }
        }

        if (syncPhase) {
            syncPhase(dt);
        }
    }

    private void syncPhase(double dt) {
        double rate = 0.0;

        for (int i = 0; i < samples.size(); i++) {
            AzAnimationController<T> controller = samples.get(i).controller;
            AzQueuedAnimation current = controller.currentAnimation();

            if (weights[i] <= 0 || current == null || current.animation().length() <= 0) {
                continue;
            }

            rate += weights[i] * controller.effectiveProperties().animationSpeed() / current.animation().length();
        }

        phase += dt * rate;
        boolean wrapped = phase >= 1;

        if (wrapped) {
            phase -= Math.floor(phase);
        }

        for (Sample<T> sample : samples) {
            AzAnimationController<T> controller = sample.controller;
            AzQueuedAnimation current = controller.currentAnimation();

            if (current == null || !controller.stateMachine().isPlaying()) {
                continue;
            }

            controller.controllerTimer().seek(phase * current.animation().length());

            if (wrapped) {
                // A new cycle: let sound, particle and custom keyframes fire again.
                controller.keyframeManager().keyframeCallbackHandler().reset();
            }
        }
    }

    private static double clamp01(double value) {
        return Double.isNaN(value) ? 0 : Math.max(0, Math.min(1, value));
    }

    private static final class Sample<T> {

        private final String animation;

        private final AzAnimationController<T> controller;

        private final AzAnimationSequence sequence;

        private Sample(
            String animation,
            AzAnimationController<T> controller,
            AzAnimationSequence sequence
        ) {
            this.animation = animation;
            this.controller = controller;
            this.sequence = sequence;
        }

        public String animation() {
            return animation;
        }

        public AzAnimationController<T> controller() {
            return controller;
        }

        public AzAnimationSequence sequence() {
            return sequence;
        }
    }

    protected static final class Settings<T> {

        boolean syncPhase = true;

        double smoothingTicks;

        UnaryOperator<AzAnimationControllerBuilder<T>> controller = UnaryOperator.identity();
    }

    @SuppressWarnings("unchecked")
    protected abstract static class Builder<T, B extends Builder<T, B>> {

        protected final AzAnimator<?, T> animator;

        protected final String name;

        protected final Settings<T> settings = new Settings<>();

        protected Builder(AzAnimator<?, T> animator, String name) {
            this.animator = Objects.requireNonNull(animator, "animator");
            this.name = Objects.requireNonNull(name, "name");
        }

        /**
         * Keeps all samples on one shared, normalized cycle (default {@code true}). Turn it off for samples that are
         * not cycles of the same motion, such as aim poses.
         */
        public B syncPhase(boolean syncPhase) {
            settings.syncPhase = syncPhase;
            return (B) this;
        }

        /** Eases inputs towards their new values with this time constant, in animation ticks. 0 (default) is off. */
        public B smoothing(double ticks) {
            settings.smoothingTicks = Math.max(0, ticks);
            return (B) this;
        }

        /**
         * Configures the sample controllers, e.g. their transition length, easing or bone mask. Applied to each one.
         */
        public B controller(UnaryOperator<AzAnimationControllerBuilder<T>> configure) {
            settings.controller = Objects.requireNonNull(configure, "configure");
            return (B) this;
        }
    }
}
