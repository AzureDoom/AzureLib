package mod.azure.azurelib.animation.controller;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.controller.keyframe.AzKeyframeCallbacks;
import mod.azure.azurelib.animation.controller.keyframe.AzKeyframeManager;
import mod.azure.azurelib.animation.controller.state.impl.AzAnimationPauseState;
import mod.azure.azurelib.animation.controller.state.impl.AzAnimationPlayState;
import mod.azure.azurelib.animation.controller.state.impl.AzAnimationStopState;
import mod.azure.azurelib.animation.controller.state.impl.AzAnimationTransitionState;
import mod.azure.azurelib.animation.controller.state.machine.AzAnimationControllerStateMachine;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequence;
import mod.azure.azurelib.animation.dispatch.command.stage.AzAnimationStage;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviorRegistry;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.primitive.AzAnimationDefaults;
import mod.azure.azurelib.animation.primitive.AzBakedAnimation;
import mod.azure.azurelib.animation.primitive.AzQueuedAnimation;
import mod.azure.azurelib.animation.property.AzAnimationProperties;
import mod.azure.azurelib.animation.property.AzAnimationStageProperties;
import mod.azure.azurelib.util.math.Mth;

/**
 * The actual controller that handles the playing and usage of animations, including their various keyframes and
 * instruction markers. Each controller can only play a single animation at a time - for example, you may have one
 * controller to animate walking, one to control attacks, one is to control size, etc.
 */
public class AzAnimationController<T> extends AzAbstractAnimationController {

    protected static final Logger LOGGER = LogManager.getLogger(AzAnimationController.class);

    public static <T> AzAnimationControllerBuilder<T> builder(AzAnimator<?, T> animator, String name) {
        return new AzAnimationControllerBuilder<>(animator, name);
    }

    private final AzAnimationControllerTimer<T> controllerTimer;

    private final AzAnimationQueue animationQueue;

    private final AzAnimationControllerStateMachine<T> stateMachine;

    private final AzAnimator<?, T> animator;

    private final AzBoneAnimationQueueCache<T> boneAnimationQueueCache;

    private final AzBoneSnapshotCache boneSnapshotCache;

    private final AzKeyframeManager<T> keyframeManager;

    protected AzQueuedAnimation currentAnimation;

    private AzAnimationProperties animationProperties;

    /**
     * How many times the current animation has finished and been replayed by
     * {@link mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors#REPEAT_X_TIMES}. Kept per controller because
     * play behaviors are shared singletons.
     */
    private int repeatCount;

    /**
     * Runtime flip of the current animation's direction, layered on top of the stage/controller direction. Toggled by
     * {@link mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors#PING_PONG} at each leg and by
     * {@link #setReversing(boolean)} when the direction changes mid-animation. Kept per controller because play
     * behaviors are shared singletons, and cleared whenever a new animation starts.
     */
    private boolean directionFlipped;

    /** How strongly this controller's animation is applied, from 0 (no effect) to 1 (full). See {@link #setWeight}. */
    private double weight = 1;

    private AzBlendMode blendMode = AzBlendMode.OVERRIDE;

    private AzBoneMask boneMask = AzBoneMask.ALL;

    private final AzWeightFade weightFade = new AzWeightFade();

    AzAnimationController(
        String name,
        AzAnimator<?, T> animator,
        AzAnimationProperties animationProperties,
        AzKeyframeCallbacks<T> keyframeCallbacks
    ) {
        super(name);

        this.animator = animator;
        this.controllerTimer = new AzAnimationControllerTimer<>(this);
        this.animationProperties = animationProperties;

        this.animationQueue = new AzAnimationQueue();
        this.boneAnimationQueueCache = new AzBoneAnimationQueueCache<>(animator.context().boneCache());
        this.boneSnapshotCache = new AzBoneSnapshotCache();
        this.keyframeManager = new AzKeyframeManager<>(
            this,
            boneAnimationQueueCache,
            boneSnapshotCache,
            keyframeCallbacks
        );

        AzAnimationControllerStateMachine.StateHolder<T> stateHolder =
            new AzAnimationControllerStateMachine.StateHolder<T>(
                new AzAnimationPlayState<>(),
                new AzAnimationPauseState<>(),
                new AzAnimationStopState<>(),
                new AzAnimationTransitionState<>()
            );

        this.stateMachine = new AzAnimationControllerStateMachine<>(stateHolder, this, animator.context());
    }

    /**
     * Determines if the animation process managed by this controller has fully completed. <br>
     * This method combines the parent class's condition for animation completion with an additional check to verify if
     * the state machine is in a stopped state. The state machine being stopped indicates that no subsequent animations
     * or transitions are active.
     *
     * @return true if the parent controller and state machine both indicate that the animation process has fully
     *         finished, false otherwise.
     */
    @Override
    public boolean hasAnimationFinished() {
        return super.hasAnimationFinished() && stateMachine.isStopped();
    }

    /**
     * Attempts to create a queue of animations from the provided animation sequence for the given animatable object.
     * This method processes each stage of the supplied animation sequence, retrieves the corresponding animation, and
     * adds it to the queue with its specified play behavior.
     * <p>
     * If any stage in the sequence references an animation that cannot be found, the method logs a warning and returns
     * an empty list, indicating that the animation queue could not be fully created.
     *
     * @param animatable The animatable object for which the animation queue is being created. The object determines the
     *                   context in which animations are retrieved and applied.
     * @param sequence   An {@link AzAnimationSequence} object representing a sequential list of animation stages, each
     *                   containing metadata necessary to retrieve and configure animations.
     * @return A list of {@link AzQueuedAnimation} objects representing the created animation queue. Returns an empty
     *         list if any stage references a non-existent animation.
     */
    public List<AzQueuedAnimation> tryCreateAnimationQueue(T animatable, AzAnimationSequence sequence) {
        if (animatable == null) {
            LOGGER.warn("Unable to create animation queue: animatable is null");
            return ImmutableList.of();
        }
        List<AzAnimationStage> stages = sequence.stages();
        ArrayList<AzQueuedAnimation> animations = new ArrayList<AzQueuedAnimation>();

        for (AzAnimationStage stage : stages) {
            AzBakedAnimation animation = animator.getAnimation(animatable, stage.name());

            if (animation == null) {
                LOGGER.warn(
                    "Unable to find animation: {} for {}",
                    stage.name(),
                    animatable.getClass().getSimpleName()
                );
                return ImmutableList.of();
            } else {
                AzAnimationStageProperties properties = stage.properties();
                Boolean reverseOverride = properties.hasReversing() ? properties.isReversing() : null;
                AzPlayBehavior playBehavior = resolvePlayBehavior(properties, animation);
                animations.add(new AzQueuedAnimation(animation, playBehavior, reverseOverride));
            }
        }

        return animations;
    }

    /**
     * This method is called every frame to populate the animation point queues, and process animation state logic.
     */
    public void update() {
        // Adjust the tick before making any updates.
        controllerTimer.update();
        // Prepares bone cache for animations on a frame.
        boneAnimationQueueCache.prepareFrame();
        // Run state machine updates.
        stateMachine.update();
        // Advance any weight fade before applying this frame's values.
        updateWeightFade();
        // Update bone animation queue cache.
        boneAnimationQueueCache.update(animationProperties.easingType(), weight, blendMode);
    }

    /**
     * Executes an animation sequence for a given dispatch side and updates the state machine accordingly. This method
     * determines if an animation sequence can be executed based on its origin side and whether the current animation
     * sequence has finished. It also transitions the state machine and handles the addition of new animations to the
     * animation queue.
     *
     * @param originSide The side (client or server) from which the animation sequence originates. This determines
     *                   whether the sequence can override a currently running sequence.
     * @param sequence   The {@link AzAnimationSequence} object representing the ordered list of animation stages to be
     *                   processed. Must not be null.
     */
    public void run(AzDispatchSide originSide, @Nonnull AzAnimationSequence sequence) {
        if (currentSequenceOrigin == AzDispatchSide.SERVER && originSide == AzDispatchSide.CLIENT) {
            if (!hasAnimationFinished()) {
                // If we're playing a server-side sequence, ignore client-side sequences.
                return;
            }
        }

        this.currentSequenceOrigin = originSide;

        // A finished sequence has consumed its queue. Dispatching it again should replay it from the first stage,
        // rather than being treated as "already playing" and only replaying the last stage.
        boolean wasStopped = stateMachine.isStopped();

        if (wasStopped) {
            stateMachine.transition();
        }

        if (currentSequence == null || !currentSequence.equals(sequence)) {
            this.currentAnimation = null;
        }

        T animatable = animator.context().animatable();

        if (sequence.stages().isEmpty()) {
            stateMachine.stop();
            return;
        }

        if (wasStopped || !sequence.equals(currentSequence)) {
            List<AzQueuedAnimation> animations = tryCreateAnimationQueue(animatable, sequence);

            if (!animations.isEmpty()) {
                animationQueue.clear();
                animationQueue.addAll(animations);
                this.currentSequence = sequence;
                stateMachine.transition();
                return;
            }

            animationQueue.clear();
            this.currentSequence = null;
            stateMachine.transition();
        }
    }

    public AzAnimationProperties animationProperties() {
        return animationProperties;
    }

    /**
     * Sets the animation properties for this controller. This method assigns the provided {@link AzAnimationProperties}
     * object to the controller to define various attributes for animation behavior, such as speed, easing type,
     * transition length, and start tick offset.
     *
     * @param animationProperties The {@link AzAnimationProperties} object containing the desired animation properties.
     *                            This parameter must not be null.
     */
    public void setAnimationProperties(AzAnimationProperties animationProperties) {
        this.animationProperties = animationProperties;
    }

    public AzAnimationQueue animationQueue() {
        return animationQueue;
    }

    /**
     * How strongly this controller's animation is applied to the bones it animates, from 0 to 1.
     */
    public double weight() {
        return weight;
    }

    /**
     * Sets how strongly this controller's animation is applied, clamped to 0..1. Controllers are layered in the order
     * they were added: each one blends its pose over whatever earlier controllers wrote to the same bone this frame, or
     * over the bind pose if none did. At 1 (the default) the pose fully replaces what is underneath, which is the same
     * result as before blending existed; at 0 the controller has no effect.
     */
    public void setWeight(double weight) {
        this.weightFade.cancel();
        this.weight = clampWeight(weight);
    }

    /**
     * Moves the weight to {@code targetWeight} over {@code lengthTicks} ticks, starting next frame, so layers can fade
     * in and out instead of popping. Fades follow the animator's clock: they run smoothly between game ticks, pause
     * with the game, and are not affected by animation speed. A length of 0 or less sets the weight immediately.
     * Calling {@link #setWeight} or starting another fade replaces the current one, starting from the current weight.
     */
    public void fadeWeight(double targetWeight, double lengthTicks) {
        if (!(lengthTicks > 0)) {
            setWeight(targetWeight);
            return;
        }

        this.weightFade.start(this.weight, clampWeight(targetWeight), lengthTicks);
    }

    /** Whether a {@link #fadeWeight} fade is still in progress. */
    public boolean isFadingWeight() {
        return weightFade.isActive();
    }

    private void updateWeightFade() {
        if (weightFade.isActive()) {
            weight = weightFade.update(animator.context().timer().getAnimTime());
        }
    }

    private static double clampWeight(double weight) {
        return Double.isNaN(weight) ? 0 : Math.max(0, Math.min(1, weight));
    }

    /** How this controller combines with earlier controllers on the same bone. */
    public AzBlendMode blendMode() {
        return blendMode;
    }

    /** Sets how this controller combines with earlier controllers on the same bone. See {@link AzBlendMode}. */
    public void setBlendMode(AzBlendMode blendMode) {
        this.blendMode = blendMode == null ? AzBlendMode.OVERRIDE : blendMode;
    }

    /** The bones this controller may animate. */
    public AzBoneMask boneMask() {
        return boneMask;
    }

    /** Limits which bones this controller animates. See {@link AzBoneMask}. */
    public void setBoneMask(AzBoneMask boneMask) {
        this.boneMask = boneMask == null ? AzBoneMask.ALL : boneMask;
    }

    public AzBoneAnimationQueueCache<T> boneAnimationQueueCache() {
        return boneAnimationQueueCache;
    }

    public AzBoneSnapshotCache boneSnapshotCache() {
        return boneSnapshotCache;
    }

    public AzAnimationControllerTimer<T> controllerTimer() {
        return controllerTimer;
    }

    public @Nullable AzQueuedAnimation currentAnimation() {
        return currentAnimation;
    }

    public AzKeyframeManager<T> keyframeManager() {
        return keyframeManager;
    }

    public AzAnimationControllerStateMachine<T> stateMachine() {
        return stateMachine;
    }

    /**
     * Sets the current animation to be played and updates the associated state variables. This method assigns the given
     * {@link AzQueuedAnimation} as the current animation and clears the current animation sequence and its origin if
     * the provided animation is null.
     *
     * @param currentAnimation The {@link AzQueuedAnimation} to be set as the current animation. May be null, in which
     *                         case the current sequence and sequence origin are cleared.
     */
    public void setCurrentAnimation(AzQueuedAnimation currentAnimation) {
        this.currentAnimation = currentAnimation;
        // A new (or canceled) animation must not inherit the previous animation's repeat progress.
        this.repeatCount = 0;
        // ...nor the previous animation's ping-pong leg or in-place reversal.
        this.directionFlipped = false;

        if (currentAnimation == null) {
            this.currentSequence = null;
            this.currentSequenceOrigin = null;
        }
    }

    /**
     * @return how many times the current animation has been repeated so far
     */
    public int repeatCount() {
        return repeatCount;
    }

    /**
     * Increments the repeat count of the current animation.
     *
     * @return the new repeat count
     */
    public int incrementRepeatCount() {
        return ++repeatCount;
    }

    /**
     * Resets the repeat count of the current animation to zero.
     */
    public void resetRepeatCount() {
        this.repeatCount = 0;
    }

    /**
     * @return {@code true} if the current animation is being sampled from its end towards its start. The base direction
     *         comes from the stage ({@link AzQueuedAnimation#reverseOverride()}) if it set one, otherwise from the
     *         controller's {@link AzAnimationProperties#isReversing()}; a runtime flip (ping-pong, in-place reversal)
     *         is then applied on top.
     */
    public boolean isPlayingReversed() {
        boolean base = currentAnimation != null && currentAnimation.reverseOverride() != null
            ? currentAnimation.reverseOverride()
            : animationProperties.isReversing();

        return base ^ directionFlipped;
    }

    /**
     * Maps playback progress to the tick that keyframes are sampled at. Progress
     * ({@link AzAnimationControllerTimer#getAdjustedTick()}) always counts up from 0 to the animation length no matter
     * the direction, so finish detection, play behaviors, freeze and repeat logic are direction-agnostic. Only sampling
     * (bones, Molang {@code query.anim_time}, keyframe callbacks) reads this mirrored value.
     *
     * @return a tick in {@code [0, length]} of the current animation, or the raw progress if nothing is playing
     */
    public double sampleTick() {
        double progress = controllerTimer.getAdjustedTick();

        if (currentAnimation == null) {
            return progress;
        }

        double length = currentAnimation.animation().length();
        double clamped = Mth.clamp(progress, 0D, length);

        return isPlayingReversed() ? length - clamped : clamped;
    }

    /**
     * Flips the direction of the current animation for its next leg. The caller is expected to restart progress (as
     * {@link mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors#PING_PONG} does at a leg boundary), so the
     * sampled pose stays continuous: the end of one leg is the start of the next.
     */
    public void flipDirection() {
        this.directionFlipped = !directionFlipped;
    }

    /**
     * Turns the current animation around from the pose it is in right now, without a jump: progress is remapped to
     * {@code length - progress}, so the sampled tick is unchanged on this frame and starts moving the other way on the
     * next. Keyframe callbacks are re-primed so events already "behind" the new direction don't fire immediately. Does
     * nothing if no animation is playing.
     */
    public void reverseInPlace() {
        if (currentAnimation == null) {
            return;
        }

        double length = currentAnimation.animation().length();
        double progress = Mth.clamp(controllerTimer.getAdjustedTick(), 0D, length);

        flipDirection();
        controllerTimer.seek(length - progress);
        keyframeManager.keyframeCallbackHandler().resync(sampleTick(), isPlayingReversed());
    }

    /**
     * Sets the controller-level direction. If that changes the direction of an animation that is already playing, the
     * animation turns around in place (see {@link #reverseInPlace()}) instead of snapping to the mirrored pose. Stages
     * with their own direction ignore the controller-level one.
     */
    public void setReversing(boolean reversing) {
        boolean wasReversed = isPlayingReversed();
        this.animationProperties = animationProperties.withShouldReverse(reversing);

        if (currentAnimation != null && isPlayingReversed() != wasReversed) {
            // The property change already flipped the effective direction; undo that with a runtime flip so the
            // remap below sees the old direction, then let reverseInPlace() flip it back with continuity.
            flipDirection();
            reverseInPlace();
        }
    }

    /**
     * Picks the behavior a stage plays with: the one the code chose, unless it chose nothing or
     * {@link AzPlayBehaviors#AS_AUTHORED}, in which case the animation file's authored {@code loop} decides.
     */
    private AzPlayBehavior resolvePlayBehavior(AzAnimationStageProperties properties, AzBakedAnimation animation) {
        if (properties.hasPlayBehavior() && properties.playBehavior() != AzPlayBehaviors.AS_AUTHORED) {
            return properties.playBehavior();
        }

        String authored = animation.defaults().playBehavior();

        if (authored == null) {
            return AzPlayBehaviors.PLAY_ONCE;
        }

        AzPlayBehavior behavior = AzPlayBehaviorRegistry.getOrNull(authored);

        if (behavior == null || behavior == AzPlayBehaviors.AS_AUTHORED) {
            LOGGER.warn(
                "Animation '{}' asks for unknown play behavior '{}', playing it once instead",
                animation.name(),
                authored
            );
            return AzPlayBehaviors.PLAY_ONCE;
        }

        return behavior;
    }

    /**
     * The repeat count {@link AzPlayBehaviors#REPEAT_X_TIMES} uses: a value set on the controller (e.g. via an
     * AzCommand) wins; otherwise the current animation's authored {@code repeat_times}.
     */
    public double effectiveRepeatXTimes() {
        double commanded = animationProperties.repeatXTimes();

        if (commanded > 1 || currentAnimation == null) {
            return commanded;
        }

        double authored = currentAnimation.animation().defaults().repeatTimes();
        return authored > 0 ? authored : commanded;
    }

    /**
     * The freeze point, in ticks, used by the controller timer and {@link AzPlayBehaviors#FREEZE_ON_FRAME}: a value set
     * on the controller wins; otherwise the current animation's authored {@code freeze_at}, but only while that
     * animation is actually playing with freeze_on_frame (so a file's freeze point can't stall a stage the code chose
     * to loop).
     */
    public double effectiveFreezeTickOffset() {
        double commanded = animationProperties.freezeTickOffset();

        if (commanded > 0 || currentAnimation == null) {
            return commanded;
        }

        AzAnimationDefaults defaults = currentAnimation.animation().defaults();

        if (currentAnimation.playBehavior() == AzPlayBehaviors.FREEZE_ON_FRAME && defaults.hasFreezeTick()) {
            return defaults.freezeTick();
        }

        return commanded;
    }
}
