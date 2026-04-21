package mod.azure.azurelib.animation.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
import mod.azure.azurelib.animation.primitive.AzQueuedAnimation;
import mod.azure.azurelib.animation.property.AzAnimationProperties;

/**
 * The actual controller that handles the playing and usage of animations, including their various keyframes and
 * instruction markers. Each controller can only play a single animation at a time - for example, you may have one
 * controller to animate walking, one to control attacks, one is to control size, etc.
 */
public class AzAnimationController<T> extends AzAbstractAnimationController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AzAnimationController.class);

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

        var stateHolder = new AzAnimationControllerStateMachine.StateHolder<T>(
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
            return List.of();
        }
        var stages = sequence.stages();
        var animations = new ArrayList<AzQueuedAnimation>();

        for (var stage : stages) {
            var animation = animator.getAnimation(animatable, stage.name());

            if (animation == null) {
                LOGGER.warn(
                    "Unable to find animation: {} for {}",
                    stage.name(),
                    animatable.getClass().getSimpleName()
                );
                return List.of();
            } else {
                animations.add(new AzQueuedAnimation(animation, stage.properties().playBehavior()));
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
        // Run state machine updates.
        stateMachine.update();
        // Update bone animation queue cache.
        boneAnimationQueueCache.update(animationProperties.easingType());
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
    public void run(AzDispatchSide originSide, @NotNull AzAnimationSequence sequence) {
        if (currentSequenceOrigin == AzDispatchSide.SERVER && originSide == AzDispatchSide.CLIENT) {
            if (!hasAnimationFinished()) {
                // If we're playing a server-side sequence, ignore client-side sequences.
                return;
            }
        }

        this.currentSequenceOrigin = originSide;

        if (stateMachine.isStopped()) {
            stateMachine.transition();
        }

        if (currentSequence == null || !currentSequence.equals(sequence)) {
            this.currentAnimation = null;
        }

        var animatable = animator.context().animatable();

        if (sequence.stages().isEmpty()) {
            stateMachine.stop();
            return;
        }

        if (!sequence.equals(currentSequence)) {
            var animations = tryCreateAnimationQueue(animatable, sequence);

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

        if (currentAnimation == null) {
            this.currentSequence = null;
            this.currentSequenceOrigin = null;
        }
    }
}
