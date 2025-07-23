package mod.azure.azurelib.rewrite.animation.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.controller.keyframe.AzKeyframeCallbacks;
import mod.azure.azurelib.rewrite.animation.controller.keyframe.AzKeyframeManager;
import mod.azure.azurelib.rewrite.animation.controller.state.impl.AzAnimationPauseState;
import mod.azure.azurelib.rewrite.animation.controller.state.impl.AzAnimationPlayState;
import mod.azure.azurelib.rewrite.animation.controller.state.impl.AzAnimationStopState;
import mod.azure.azurelib.rewrite.animation.controller.state.impl.AzAnimationTransitionState;
import mod.azure.azurelib.rewrite.animation.controller.state.machine.AzAnimationControllerStateMachine;
import mod.azure.azurelib.rewrite.animation.controller.state.machine.StateHolder;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.sequence.AzAnimationSequence;
import mod.azure.azurelib.rewrite.animation.dispatch.command.stage.AzAnimationStage;
import mod.azure.azurelib.rewrite.animation.primitive.AzBakedAnimation;
import mod.azure.azurelib.rewrite.animation.primitive.AzQueuedAnimation;
import mod.azure.azurelib.rewrite.animation.property.AzAnimationProperties;

/**
 * The actual controller that handles the playing and usage of animations, including their various keyframes and
 * instruction markers. Each controller can only play a single animation at a time - for example, you may have one
 * controller to animate walking, one to control attacks, one is to control size, etc.
 */
public class AzAnimationController<T> extends AzAbstractAnimationController {

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

        StateHolder<T> stateHolder = new StateHolder<T>(
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
        List<AzAnimationStage> stages = sequence.stages();
        ArrayList<AzQueuedAnimation> animations = new ArrayList<AzQueuedAnimation>();

        for (AzAnimationStage stage : stages) {
            AzBakedAnimation animation = animator.getAnimation(animatable, stage.name());

            if (animation == null) {
                AzureLib.LOGGER.warn(
                    "Unable to find animation: {} for {}",
                    stage.name(),
                    animatable.getClass().getSimpleName()
                );
                return new ArrayList<>();
            } else {
                animations.add(new AzQueuedAnimation(animation, stage.properties().playBehavior()));
            }
        }

        return animations;
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

        T animatable = animator.context().animatable();

        if (sequence.stages().isEmpty()) {
            stateMachine.stop();
            return;
        }

        if (!sequence.equals(currentSequence)) {
            List<AzQueuedAnimation> animations = tryCreateAnimationQueue(animatable, sequence);

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
