package mod.azure.azurelib.render.layer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.function.Supplier;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.AzRendererPipelineContext;

/**
 * Render layer base class for rendering additional layers of effects or textures over an existing model at runtime.<br>
 * <p>
 * This interface defines the lifecycle hooks used by the rendering pipeline to apply custom visual effects, overlays,
 * or transformations on top of the base model.<br>
 * <p>
 *
 * @param <K> The type of the key used to identify the animatable object. Typically, a UUID for items/entities and Long
 *            for BlockEntities.
 * @param <T> The type of the animatable object this layer is applied to.
 * @see AzRendererPipeline
 */
public interface AzRenderLayer<K, T> {

    /**
     * Called by the {@link AzRendererPipeline} before rendering begins, immediately after
     * {@link AzRendererPipeline#preRender}.
     * <p>
     * Allows render layers to perform setup tasks such as hiding/showing bones or other pre-render state modifications.
     *
     * @param context The active renderer context containing pipeline state, the animatable instance, and the associated
     *                key {@code K}.
     */
    void preRender(AzRendererPipelineContext<K, T> context);

    /**
     * Called by the renderer after the main animatable has been rendered, but before supplementary features (like name
     * tags).
     * <p>
     * This is the primary method to implement custom render layer logic.
     *
     * @param context The active renderer context containing pipeline state, the animatable instance, and the associated
     *                key {@code K}.
     */
    void render(AzRendererPipelineContext<K, T> context);

    /**
     * Called by the {@link AzRendererPipeline} for each {@link AzBone} being rendered.<br>
     * <p>
     * This is more expensive than {@link #render} since it occurs per-bone, but it has the advantage of having all
     * matrix transformations already applied.<br>
     * <p>
     * The {@link AzBone} in question has already been rendered by this stage.<br>
     * <p>
     * <b>Important:</b> If you modify the {@link VertexConsumer buffer}, reset it to the previous one via
     * {@link MultiBufferSource#getBuffer} before returning.
     *
     * @param context The active renderer context containing pipeline state, the animatable instance, and the associated
     *                key {@code K}.
     * @param bone    The bone currently being rendered.
     */
    void renderForBone(AzRendererPipelineContext<K, T> context, AzBone bone);

    /**
     * Whether this layer's translucent geometry is sorted together with the models when the renderer has translucency
     * sorting turned on ({@code setTranslucencySorting(true)} on its config builder). Return {@code false} for layers
     * that draw over the model's own surfaces, like a glow or overlay texture: those are coplanar with the model, so
     * sorting gains nothing and interleaving them with the model's quads costs a draw call per switch. Opted-out layers
     * still draw after all sorted geometry, in order, as they do without sorting.
     *
     * @return {@code true} (the default) to sort this layer's translucent geometry with the model's
     */
    default boolean sortsTranslucency() {
        return true;
    }

    /**
     * Re-renders the given renderer pipeline context using a specified baked model, posed to match the model that was
     * just rendered. Every bone of {@code model} that shares a name with a bone of the current model copies that bone's
     * animated pose (relative to each model's own bind pose) and visibility; bones that only exist in {@code model}
     * keep their bind pose relative to their parent. If the specified model is {@code null}, a default baked model is
     * used.
     * <p>
     * The animatable's bone cache is left untouched, so animation state, bone snapshots and bone matrix tracking (used
     * by hitboxes and bone-attached effects) of the base model are unaffected by the layer. The animator is not run a
     * second time.
     *
     * @param context The active renderer pipeline context containing the pipeline state, the animatable instance, and
     *                other rendering properties.
     * @param model   The baked model to render in place of the current one for this pass.
     */
    default void reRenderWithBakedModel(AzRendererPipelineContext<K, T> context, AzBakedModel model) {
        var baseModel = context.bakedModel();
        var targetModel = model != null ? model : AzBakedModel.getDefault();
        var baseAnimator = AzAnimatorAccessor.getOrNull(context.animatable());
        var baseContext = baseAnimator != null ? baseAnimator.context() : null;
        var overlay = baseModel != null
            ? AzOverlayModels.posedOverlay(baseContext, baseModel, targetModel)
            : targetModel;

        renderOverlay(context, overlay);
    }

    /**
     * Opt-in variant of {@link #reRenderWithBakedModel} for overlays that have bones of their own which need to be
     * animated (a strap that swings, a flap that opens, etc.).
     * <p>
     * The overlay gets its own animator, created once per animatable from {@code overlayAnimatorFactory} and kept for
     * as long as the animatable's animator lives. That animator:
     * <ul>
     * <li>animates its own copy of {@code model} each time this is called, then every bone shared with the current
     * model is overwritten with the current model's pose, exactly as in {@link #reRenderWithBakedModel}, so shared
     * geometry still lines up with the base model;</li>
     * <li>receives every animation command dispatched to the animatable (client and server), so controller settings
     * such as speed, weight and reversal follow the base animator;</li>
     * <li>is aligned every frame before it animates: each controller with the same name as one of the base animator's
     * is set to the same sequence, origin side, stage, repeat count, direction and progress. Overlay-only bones stay
     * frame-exact with the base animation, including when the overlay is created mid-animation or wasn't rendered for a
     * while. A newly started overlay animation blends in over its transition, then snaps into step.</li>
     * </ul>
     * Typically the overlay animator returns the base animation file from {@link AzAnimator#getAnimationLocation} and
     * registers controllers with the same names as the base animator, with the overlay-only bones keyed in that file.
     * Don't register sound/particle/custom keyframe handlers on it unless they should fire a second time. This costs
     * one extra animator update per animatable per frame, so prefer {@link #reRenderWithBakedModel} when the overlay
     * has no bones of its own to animate.
     *
     * @param context                The active renderer pipeline context.
     * @param model                  The overlay's baked model template, e.g. from {@code AzBakedModelCache}.
     * @param overlayAnimatorFactory Creates the overlay's animator; called once per animatable and overlay model.
     */
    default void reRenderWithAnimatedBakedModel(
        AzRendererPipelineContext<K, T> context,
        AzBakedModel model,
        Supplier<? extends AzAnimator<?, T>> overlayAnimatorFactory
    ) {
        var baseModel = context.bakedModel();
        var targetModel = model != null ? model : AzBakedModel.getDefault();
        AzAnimator<?, T> baseAnimator = AzAnimatorAccessor.getOrNull(context.animatable());
        var overlay = baseModel != null
            ? AzOverlayModels.animatedOverlay(
                baseAnimator,
                context.animatable(),
                baseModel,
                targetModel,
                overlayAnimatorFactory,
                context.partialTick()
            )
            : targetModel;

        renderOverlay(context, overlay);
    }

    private void renderOverlay(AzRendererPipelineContext<K, T> context, AzBakedModel overlay) {
        var previousModel = context.bakedModel();

        context.setBakedModel(overlay);

        try {
            // Never re-run the base animator: the overlay is already posed.
            context.rendererPipeline().reRender(context, false);
        } finally {
            context.setBakedModel(previousModel);
        }
    }
}
