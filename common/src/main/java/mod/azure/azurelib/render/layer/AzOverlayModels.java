package mod.azure.azurelib.render.layer;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import mod.azure.azurelib.animation.AzAnimationContext;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequence;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;

/**
 * Per-animatable copies of overlay models used by {@link AzRenderLayer#reRenderWithBakedModel} and
 * {@link AzRenderLayer#reRenderWithAnimatedBakedModel}.
 * <p>
 * An overlay (saddle, armor, glow layer, ...) is posed by copying the animated pose of the base model's bones onto the
 * overlay's bones of the same name. The base model's bone cache is never touched, so its animation state, snapshots and
 * matrix tracking (used by hitboxes, bone-attached effects, etc.) survive the layer pass.
 * <p>
 * Copies are stored in the base animator's {@link AzAnimationContext#attachments()} (added alongside this class), so
 * they are owned by that context and need no weak references. If the base animator or its context is replaced (e.g.
 * {@code AzProvider} recreating the animator after a model change), the copies go with it and are rebuilt on the next
 * render.
 */
final class AzOverlayModels {

    private static final Object ATTACHMENT_KEY = new Object();

    private AzOverlayModels() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns this animatable's private copy of {@code overlayTemplate}, with every shared bone posed to match
     * {@code baseModel}. Bones that only exist in the overlay keep their bind pose relative to their parent.
     */
    static AzBakedModel posedOverlay(
        @Nullable AzAnimationContext<?> baseContext,
        AzBakedModel baseModel,
        AzBakedModel overlayTemplate
    ) {
        if (baseContext == null) {
            // Nothing animates the base model, so the overlay's bind pose already matches it.
            return overlayTemplate;
        }

        var binding = bindings(baseContext).computeIfAbsent(
            overlayTemplate,
            template -> new Binding(template.deepCopy(), null, null)
        );

        binding.bindTo(baseModel);
        binding.syncShared();
        return binding.overlay;
    }

    /**
     * Like {@link #posedOverlay}, but the overlay also has its own animator, so bones that only exist in the overlay
     * are animated too. Before it animates, every overlay controller is aligned with the base controller of the same
     * name (sequence, current stage, repeat count, direction and progress), so overlay-only bones stay frame-exact with
     * the base animation. Shared bones are still copied from the base model afterward, so they match it exactly.
     */
    static <T> AzBakedModel animatedOverlay(
        @Nullable AzAnimator<?, T> baseAnimator,
        T animatable,
        AzBakedModel baseModel,
        AzBakedModel overlayTemplate,
        Supplier<? extends AzAnimator<?, T>> overlayAnimatorFactory,
        float partialTick
    ) {
        var baseContext = baseAnimator != null ? baseAnimator.context() : null;

        if (baseContext == null) {
            return overlayTemplate;
        }

        var binding = bindings(baseContext).get(overlayTemplate);

        if (binding == null || binding.animator == null) {
            binding = createAnimatedBinding(baseAnimator, animatable, overlayTemplate, overlayAnimatorFactory);

            if (binding == null) {
                return posedOverlay(baseContext, baseModel, overlayTemplate);
            }

            bindings(baseContext).put(overlayTemplate, binding);
        }

        binding.bindTo(baseModel);
        binding.animate(baseAnimator, animatable, partialTick);
        binding.syncShared();
        return binding.overlay;
    }

    @SuppressWarnings("unchecked")
    private static Map<AzBakedModel, Binding> bindings(AzAnimationContext<?> baseContext) {
        return (Map<AzBakedModel, Binding>) baseContext.attachments()
            .computeIfAbsent(ATTACHMENT_KEY, ignored -> new IdentityHashMap<AzBakedModel, Binding>());
    }

    @Nullable
    private static <T> Binding createAnimatedBinding(
        AzAnimator<?, T> baseAnimator,
        T animatable,
        AzBakedModel overlayTemplate,
        Supplier<? extends AzAnimator<?, T>> overlayAnimatorFactory
    ) {
        AzAnimator<?, T> overlayAnimator = overlayAnimatorFactory.get();

        if (overlayAnimator == null) {
            return null;
        }

        /*
         * AzAnimator keeps its contexts in a WeakHashMap keyed by K. The overlay animator only ever has this one
         * context, so any key works as long as something holds it strongly: the binding does.
         */
        var contextKey = new Object();
        var context = createContext(overlayAnimator, contextKey, animatable);

        context.boneCache().setActiveModel(overlayTemplate);
        overlayAnimator.registerControllers(overlayAnimator.getAnimationControllerContainer());

        baseAnimator.context().linkedAnimators().add(overlayAnimator);

        return new Binding(context.boneCache().getBakedModel(), overlayAnimator, contextKey);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T> AzAnimationContext<T> createContext(AzAnimator<?, T> animator, Object key, T animatable) {
        return ((AzAnimator) animator).getOrCreateContext(key, animatable);
    }

    private static final class Binding {

        private final AzBakedModel overlay;

        @Nullable
        private final AzAnimator<?, ?> animator;

        // Strong reference to the overlay animator's context key; see createAnimatedBinding.
        @SuppressWarnings("unused")
        @Nullable
        private final Object contextKey;

        @Nullable
        private AzBakedModel boundBase;

        private AzBone[] sources = new AzBone[0];

        private AzBone[] targets = new AzBone[0];

        /*
         * Base sequences the overlay animator couldn't play (e.g. an animation missing from its file), by controller
         * name, so they are attempted once rather than every frame.
         */
        private final Map<String, AzAnimationSequence> unplayable = new HashMap<>();

        private Binding(AzBakedModel overlay, @Nullable AzAnimator<?, ?> animator, @Nullable Object contextKey) {
            this.overlay = overlay;
            this.animator = animator;
            this.contextKey = contextKey;
        }

        /** Pairs overlay bones with the base model's bones by name; only redone when the base model changes. */
        private void bindTo(AzBakedModel baseModel) {
            if (boundBase == baseModel) {
                return;
            }

            var sourceList = new ArrayList<AzBone>();
            var targetList = new ArrayList<AzBone>();

            for (var entry : overlay.getBonesByName().entrySet()) {
                var source = baseModel.getBoneOrNull(entry.getKey());

                if (source != null) {
                    sourceList.add(source);
                    targetList.add(entry.getValue());
                }
            }

            this.sources = sourceList.toArray(new AzBone[0]);
            this.targets = targetList.toArray(new AzBone[0]);
            this.boundBase = baseModel;
        }

        @SuppressWarnings("unchecked")
        private <T> void animate(AzAnimator<?, T> baseAnimator, T animatable, float partialTick) {
            var overlayAnimator = (AzAnimator<?, T>) animator;

            // Tick the timer first so controllers are aligned against this frame's time, then animate without
            // ticking it again.
            overlayAnimator.context().timer().tick();
            alignControllers(baseAnimator, overlayAnimator);
            overlayAnimator.animate(animatable, partialTick, false);
        }

        private void alignControllers(AzAnimator<?, ?> baseAnimator, AzAnimator<?, ?> overlayAnimator) {
            var baseControllers = baseAnimator.getAnimationControllerContainer();

            for (var overlayController : overlayAnimator.getAnimationControllerContainer().getAll()) {
                var baseController = baseControllers.getOrNull(overlayController.name());

                if (baseController != null) {
                    alignController(baseController, overlayController);
                }
            }
        }

        /**
         * Makes {@code overlay} follow {@code base}. Commands are already forwarded, so this mostly corrects drift: an
         * overlay created mid-animation, one that wasn't rendered (and so not ticked) for a while, sequences started
         * mid-way, and server-sequence priority differences.
         */
        private void alignController(AzAnimationController<?> base, AzAnimationController<?> overlay) {
            var baseMachine = base.stateMachine();
            var overlayMachine = overlay.stateMachine();
            var sequence = base.currentSequence();

            // 1. Same sequence, same origin. A stopped base is left alone so a finished one-shot doesn't replay.
            if (sequence != null && !baseMachine.isStopped()) {
                var differs = !sequence.equals(overlay.currentSequence())
                    || base.currentSequenceOrigin() != overlay.currentSequenceOrigin()
                    || overlayMachine.isStopped();

                if (differs && !sequence.equals(unplayable.get(overlay.name()))) {
                    overlay.mirrorSequence(base.currentSequenceOrigin(), sequence);

                    if (overlay.currentSequence() == null) {
                        unplayable.put(overlay.name(), sequence);
                        return;
                    }

                    unplayable.remove(overlay.name());
                }
            }

            // 2. Same stage and progress. Only while both are in the same state: a freshly started overlay blends in
            // over its transition, then snaps into step once both are playing.
            var bothPlaying = baseMachine.isPlaying() && overlayMachine.isPlaying();
            var bothTransitioning = baseMachine.isTransitioning() && overlayMachine.isTransitioning();

            if (!bothPlaying && !bothTransitioning) {
                return;
            }

            var baseAnimation = base.currentAnimation();

            if (baseAnimation == null) {
                return;
            }

            if (!Objects.equals(baseAnimation, overlay.currentAnimation())) {
                // A multi-stage sequence where the base is on a later stage than the overlay.
                overlay.animationQueue().copyFrom(base.animationQueue());
                overlay.setCurrentAnimation(baseAnimation);
            }

            if (overlay.repeatCount() != base.repeatCount()) {
                overlay.resetRepeatCount();

                while (overlay.repeatCount() < base.repeatCount()) {
                    overlay.incrementRepeatCount();
                }
            }

            if (overlay.isPlayingReversed() != base.isPlayingReversed()) {
                overlay.flipDirection();
            }

            overlay.controllerTimer().seek(base.controllerTimer().getAdjustedTick());
        }

        private void syncShared() {
            for (int i = 0; i < sources.length; i++) {
                copyPose(sources[i], targets[i]);
            }
        }

        /**
         * Applies the source bone's offset from its bind pose on top of the target's own bind pose, so overlays whose
         * bones share a name but not a bind pose still line up.
         */
        private static void copyPose(AzBone source, AzBone target) {
            var sourceBind = source.getInitialAzSnapshot();
            var targetBind = target.getInitialAzSnapshot();

            target.updateRotation(
                targetBind.getRotX() + source.getRotX() - sourceBind.getRotX(),
                targetBind.getRotY() + source.getRotY() - sourceBind.getRotY(),
                targetBind.getRotZ() + source.getRotZ() - sourceBind.getRotZ()
            );
            target.updatePosition(
                targetBind.getOffsetX() + source.getPosX() - sourceBind.getOffsetX(),
                targetBind.getOffsetY() + source.getPosY() - sourceBind.getOffsetY(),
                targetBind.getOffsetZ() + source.getPosZ() - sourceBind.getOffsetZ()
            );
            target.updateScale(
                scale(targetBind.getScaleX(), source.getScaleX(), sourceBind.getScaleX()),
                scale(targetBind.getScaleY(), source.getScaleY(), sourceBind.getScaleY()),
                scale(targetBind.getScaleZ(), source.getScaleZ(), sourceBind.getScaleZ())
            );

            // Mirror visibility so a hidden or LOD-culled base bone hides the overlay on it too.
            target.setHidden(source.isHidden());
            target.setChildrenHidden(source.isHidingChildren());
            target.setLodHidden(source.getLodHidden());
        }

        private static float scale(float targetBind, float source, float sourceBind) {
            return sourceBind == 0f ? source : targetBind * (source / sourceBind);
        }
    }
}
