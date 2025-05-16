package mod.azure.azurelib.rewrite.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.rewrite.render.*;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;

/**
 * Represents a renderer pipeline specifically designed for rendering entities. This pipeline facilitates stages of
 * rendering where contextual work like pre-translations, texture animations, and leash rendering are managed within a
 * customizable structure.
 *
 * @param <T> The type of entity this renderer pipeline handles. Extends from the base {@link Entity}.
 */
public class AzEntityRendererPipeline<T extends Entity> extends AzRendererPipeline<T> {

    private final AzEntityRenderer<T> entityRenderer;

    protected Matrix4f entityRenderTranslations = new Matrix4f();

    protected Matrix4f modelRenderTranslations = new Matrix4f();

    public AzEntityRendererPipeline(AzEntityRendererConfig<T> config, AzEntityRenderer<T> entityRenderer) {
        super(config);
        this.entityRenderer = entityRenderer;
    }

    @Override
    protected AzRendererPipelineContext<T> createContext(AzRendererPipeline<T> rendererPipeline) {
        return new AzEntityRendererPipelineContext<>(this);
    }

    @Override
    protected AzModelRenderer<T> createModelRenderer(AzLayerRenderer<T> layerRenderer) {
        return new AzEntityModelRenderer<>(this, layerRenderer);
    }

    @Override
    protected AzLayerRenderer<T> createLayerRenderer(AzRendererConfig<T> config) {
        return new AzEntityLayerRenderer<>(config::renderLayers);
    }

    /**
     * Update the current frame of a {@link AnimatableTexture potentially animated} texture used by this
     * GeoRenderer.<br>
     * This should only be called immediately prior to rendering, and only
     *
     * @see AnimatableTexture#setAndUpdate(ResourceLocation, int)
     */
    @Override
    public void updateAnimatedTextureFrame(T entity) {
        AnimatableTexture.setAndUpdate(config.textureLocation(entity));
    }

    /**
     * Called before rendering the model to buffer. Allows for render modifications and preparatory work such as scaling
     * and translating.<br>
     * {@link MatrixStack} translations made here are kept until the end of the render process
     */
    @Override
    public void preRender(AzRendererPipelineContext<T> context, boolean isReRender) {
        MatrixStack poseStack = context.poseStack();
        RenderUtils.copy(this.entityRenderTranslations, poseStack.getLast().getMatrix());

        AzEntityRendererConfig<T> config = entityRenderer.config();
        float scaleWidth = config.scaleWidth(context.animatable());
        float scaleHeight = config.scaleHeight(context.animatable());

        scaleModelForRender(context, scaleWidth, scaleHeight, isReRender);
        if (config.alpha(context.animatable()) < 1) {
            context.setAlpha(config.alpha(context.animatable()));
        }
        config.preRenderEntry(context);
    }

    @Override
    public void postRender(AzRendererPipelineContext<T> context, boolean isReRender) {
        config.postRenderEntry(context);
    }

    /**
     * Renders the final frame of the entity, including handling special cases such as entities with leashes.
     *
     * @param context the rendering context that contains all required data for rendering, such as the entity, pose
     *                stack, light information, and buffer source
     */
    @Override
    public void renderFinal(AzRendererPipelineContext<T> context) {
        IRenderTypeBuffer bufferSource = context.multiBufferSource();
        T entity = context.animatable();
        int packedLight = context.packedLight();
        float partialTick = context.partialTick();
        MatrixStack poseStack = context.poseStack();

        entityRenderer.superRender(entity, 0, partialTick, poseStack, bufferSource, packedLight);

        if (!(entity instanceof MobEntity)) {
            return;
        }

        MobEntity mob = (MobEntity) entity;

        Entity leashHolder = mob.getLeashHolder();

        if (leashHolder == null) {
            return;
        }

        AzEntityLeashRenderUtil.renderLeash(entityRenderer, mob, partialTick, poseStack, bufferSource, leashHolder);
    }

    public AzEntityRenderer<T> getRenderer() {
        return entityRenderer;
    }
}
