package mod.azure.azurelib.core2.animation.controller;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import mod.azure.azurelib.core2.animation.AzAnimator;
import mod.azure.azurelib.core2.animation.controller.keyframe.AzKeyframeCallbacks;
import mod.azure.azurelib.core2.animation.controller.keyframe.AzKeyframeManager;
import mod.azure.azurelib.core2.animation.controller.state.impl.AzAnimationPauseState;
import mod.azure.azurelib.core2.animation.controller.state.impl.AzAnimationPlayState;
import mod.azure.azurelib.core2.animation.controller.state.impl.AzAnimationStopState;
import mod.azure.azurelib.core2.animation.controller.state.impl.AzAnimationTransitionState;
import mod.azure.azurelib.core2.animation.controller.state.machine.AzAnimationControllerStateMachine;
import mod.azure.azurelib.core2.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.core2.animation.dispatch.command.sequence.AzAnimationSequence;
import mod.azure.azurelib.core2.animation.primitive.AzQueuedAnimation;
import mod.azure.azurelib.core2.animation.property.AzAnimationProperties;

/**
 * The actual controller that handles the playing and usage of animations, including their various keyframes and
 * instruction markers. Each controller can only play a single animation at a time - for example you may have one
 * controller to animate walking, one to control attacks, one to control size, etc.
 */
public class AzAnimationController<T> extends AzAbstractAnimationController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AzAnimationController.class);

    public static <T> AzAnimationControllerBuilder<T> builder(AzAnimator<T> animator, String name) {
        return new AzAnimationControllerBuilder<>(animator, name);
    }

    private final AzAnimationControllerTimer<T> controllerTimer;

    private final AzAnimationQueue animationQueue;

    private final AzAnimationControllerStateMachine<T> stateMachine;

    private final AzAnimator<T> animator;

    private final AzBoneAnimationQueueCache<T> boneAnimationQueueCache;

    private final AzBoneSnapshotCache boneSnapshotCache;

    private final AzKeyframeManager<T> keyframeManager;

    protected AzQueuedAnimation currentAnimation;

    private AzAnimationProperties animationProperties;

    AzAnimationController(
        String name,
        AzAnimator<T> animator,
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

    @Override
    public boolean hasAnimationFinished() {
        return super.hasAnimationFinished() && stateMachine.isStopped();
    }

    public List<AzQueuedAnimation> tryCreateAnimationQueue(T animatable, AzAnimationSequence sequence) {
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

    public void setAnimation(T animatable, AzAnimationSequence sequence) {
        if (sequence == null || sequence.stages().isEmpty()) {
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

            stateMachine.stop();
        }
    }

    /**
     * This method is called every frame in order to populate the animation point queues, and process animation state
     * logic.
     */
    public void update() {
        // Adjust the tick before making any updates.
        controllerTimer.update();
        // Run state machine updates.
        stateMachine.update();
        // Update bone animation queue cache.
        boneAnimationQueueCache.update(animationProperties.easingType());
    }

    public void run(AzDispatchSide originSide, AzAnimationSequence sequence) {
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

        if (sequence != null) {
            if (currentSequence == null || !currentSequence.equals(sequence)) {
                this.currentAnimation = null;
            }

            var animatable = animator.context().animatable();
            setAnimation(animatable, sequence);
        }
    }

    public AzAnimationProperties animationProperties() {
        return animationProperties;
    }

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

    public void setCurrentAnimation(AzQueuedAnimation currentAnimation) {
        this.currentAnimation = currentAnimation;

        if (currentAnimation == null) {
            this.currentSequence = null;
            this.currentSequenceOrigin = null;
        }
    }
}
