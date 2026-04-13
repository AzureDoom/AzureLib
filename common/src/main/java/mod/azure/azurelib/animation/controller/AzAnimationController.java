package mod.azure.azurelib.animation.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimationContext;
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

    public static <T> AzAnimationControllerBuilder<T> builder(AzAnimator<?, T> animator, String name) {
        return new AzAnimationControllerBuilder<>(animator, name);
    }

    private final AzAnimator<?, T> animator;

    private final AzBoneAnimationQueueCache<T> boneAnimationQueueCache;

    private final AzBoneSnapshotCache boneSnapshotCache;

    private final AzKeyframeManager<T> keyframeManager;

    private AzAnimationProperties animationProperties;

    AzAnimationController(
        String name,
        AzAnimator<?, T> animator,
        AzAnimationProperties animationProperties,
        AzKeyframeCallbacks<T> keyframeCallbacks
    ) {
        super(name);

        this.animator = animator;
        this.animationProperties = animationProperties;

        this.boneAnimationQueueCache = new AzBoneAnimationQueueCache<>();
        this.boneSnapshotCache = new AzBoneSnapshotCache();
        this.keyframeManager = new AzKeyframeManager<>(
            this,
            boneAnimationQueueCache,
            boneSnapshotCache,
            keyframeCallbacks
        );
    }

    private AzAnimationContext<T>.ControllerState state() {
        var context = context();

        if (context == null) {
            throw new IllegalStateException("No active animation context for controller " + name());
        }

        return context.getControllerState(this);
    }

    private AzAnimationControllerTimer<T> timer(AzAnimationContext<T> context) {
        var state = context.getControllerState(this);

        if (state.timer == null) {
            state.timer = new AzAnimationControllerTimer<>(this);
        }

        return state.timer;
    }

    private AzAnimationControllerStateMachine<T> stateMachine(AzAnimationContext<T> context) {
        var state = context.getControllerState(this);

        if (state.stateMachine == null) {
            var stateHolder = new AzAnimationControllerStateMachine.StateHolder<T>(
                new AzAnimationPlayState<>(),
                new AzAnimationPauseState<>(),
                new AzAnimationStopState<>(),
                new AzAnimationTransitionState<>()
            );

            state.stateMachine = new AzAnimationControllerStateMachine<>(stateHolder, this);
        }

        return state.stateMachine;
    }

    /**
     * Determines if the animation process managed by this controller has fully completed.
     */
    @Override
    public boolean hasAnimationFinished() {
        var context = context();

        if (context == null) {
            return false;
        }

        return stateMachine(context).isStopped();
    }

    /**
     * Attempts to create a queue of animations from the provided animation sequence for the given animatable object.
     */
    public List<AzQueuedAnimation> tryCreateAnimationQueue(T animatable, AzAnimationSequence sequence) {
        var stages = sequence.stages();
        var animations = new ArrayList<AzQueuedAnimation>();

        for (var stage : stages) {
            var animation = animator.getAnimation(animatable, stage.name());

            if (animation == null) {
                AzureLib.LOGGER.warn(
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
    public void update(AzAnimationContext<T> context) {
        var state = context.getControllerState(this);
        var controllerTimer = timer(context);
        var stateMachine = stateMachine(context);

        controllerTimer.update(context);
        boneAnimationQueueCache.bind(context.boneCache());

        // Make sure the state machine is using the active context for this frame
        stateMachine.update(context);

        // Lazily build queued animations once we have a live context
        if (state.currentSequence != null && state.animationQueue.isEmpty() && state.currentAnimation == null) {
            var animatable = context.animatable();
            var animations = tryCreateAnimationQueue(animatable, state.currentSequence);

            if (!animations.isEmpty()) {
                state.animationQueue.clear();
                state.animationQueue.addAll(animations);

                if (stateMachine.isStopped()) {
                    stateMachine.transition();
                }
            }
        }

        boneAnimationQueueCache.update(animationProperties.easingType());
    }

    /**
     * Executes an animation sequence for a given dispatch side. With a context-backed state, this method should only
     * queue intent/state changes. Actual playback startup happens in update(context).
     */
    public void run(AzDispatchSide originSide, @NotNull AzAnimationSequence sequence) {
        var context = context();

        if (context == null) {
            AzureLib.LOGGER.warn("run() with null context on controller {}", name());
            return;
        }

        var state = context.getControllerState(this);
        var stateMachine = stateMachine(context);

        if (state.currentSequenceOrigin == AzDispatchSide.SERVER && originSide == AzDispatchSide.CLIENT) {
            if (!stateMachine.isStopped()) {
                return;
            }
        }

        state.currentSequenceOrigin = originSide;

        if (sequence.stages().isEmpty()) {
            state.currentSequence = null;
            state.currentAnimation = null;
            state.animationQueue.clear();
            stateMachine.stop();
            return;
        }

        if (
            !sequence.equals(state.currentSequence)
                || (state.currentAnimation == null && state.animationQueue.isEmpty())
        ) {
            state.currentSequence = sequence;
            state.currentAnimation = null;
            state.animationQueue.clear();
        }
    }

    public AzAnimationProperties animationProperties() {
        return animationProperties;
    }

    public void setAnimationProperties(AzAnimationProperties animationProperties) {
        this.animationProperties = animationProperties;
    }

    public AzAnimationQueue animationQueue() {
        return state().animationQueue;
    }

    public AzBoneAnimationQueueCache<T> boneAnimationQueueCache() {
        return boneAnimationQueueCache;
    }

    public AzBoneSnapshotCache boneSnapshotCache() {
        return boneSnapshotCache;
    }

    public AzAnimationControllerTimer<T> controllerTimer() {
        var context = context();

        if (context == null) {
            throw new IllegalStateException("No active animation context for controller " + name());
        }

        return timer(context);
    }

    public @Nullable AzQueuedAnimation currentAnimation() {
        return state().currentAnimation;
    }

    public AzKeyframeManager<T> keyframeManager() {
        return keyframeManager;
    }

    public AzAnimationControllerStateMachine<T> stateMachine() {
        var context = context();

        if (context == null) {
            throw new IllegalStateException("No active animation context for controller " + name());
        }

        return stateMachine(context);
    }

    public AzAnimationContext<T> context() {
        return animator.context();
    }

    public void setCurrentAnimation(AzQueuedAnimation currentAnimation) {
        var state = state();
        state.currentAnimation = currentAnimation;

        if (currentAnimation == null) {
            state.currentSequence = null;
            state.currentSequenceOrigin = null;
        }
    }
}
