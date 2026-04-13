package mod.azure.azurelib.render.layer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;

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
     * Re-renders the given renderer pipeline context using a specified baked model. The method temporarily replaces the
     * current baked model with the provided one, triggers a re-render, and restores the original baked model upon
     * completion. If the specified model is {@code null}, a default baked model is used.
     *
     * @param context The active renderer pipeline context containing the pipeline state, the animatable instance, and
     *                other rendering properties.
     * @param model   The baked model that will temporarily replace the current one for a re-render.
     */
    default void reRenderWithBakedModel(AzRendererPipelineContext<K, T> context, AzBakedModel model) {
        var pipeline = context.rendererPipeline();
        var previousContextModel = context.bakedModel();
        var targetModel = model != null ? model : AzBakedModel.getDefault();

        var animator = AzAnimatorAccessor.getOrNull(context.animatable());

        if (animator == null || animator.context() == null || animator.context().boneCache() == null) {
            context.setBakedModel(targetModel);
            pipeline.reRender(context, true);
            context.setBakedModel(previousContextModel);
            return;
        }

        var boneCache = animator.context().boneCache();
        var previousTemplateModel = boneCache.getTemplateModel();
        var previousAnimatedModel = boneCache.getBakedModel();

        try {
            boneCache.setActiveModel(targetModel);
            context.setBakedModel(boneCache.getBakedModel());
            pipeline.reRender(context, true);
        } finally {
            if (previousTemplateModel != null) {
                boneCache.setActiveModel(previousTemplateModel);
                context.setBakedModel(previousAnimatedModel);
            } else {
                boneCache.setBakedModel(previousAnimatedModel);
                context.setBakedModel(previousAnimatedModel);
            }
        }
    }
}
