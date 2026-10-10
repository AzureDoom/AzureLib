package mod.azure.azurelib.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.render.*;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.math.Matrix4f;

/**
 * Represents a renderer pipeline specifically designed for rendering entities. This pipeline facilitates stages of
 * rendering where contextual work like pre-translations, texture animations, and leash rendering are managed within a
 * customizable structure.
 *
 * @param <T> The type of entity this renderer pipeline handles. Extends from the base {@link Entity}.
 */
public class AzEntityRendererPipeline<T extends Entity> extends AzRendererPipeline<UUID, T> {

    private final AzEntityRenderer<T> entityRenderer;

    protected Matrix4f entityRenderTranslations = new Matrix4f();

    protected Matrix4f modelRenderTranslations = new Matrix4f();

    public AzEntityRendererPipeline(AzEntityRendererConfig<T> config, AzEntityRenderer<T> entityRenderer) {
        super(config);
        this.entityRenderer = entityRenderer;
    }

    @Override
    protected AzRendererPipelineContext<UUID, T> createContext(AzRendererPipeline<UUID, T> rendererPipeline) {
        return config.pipelineContext(this);
    }

    @Override
    protected AzModelRenderer<UUID, T> createModelRenderer(AzLayerRenderer<UUID, T> layerRenderer) {
        return config.modelRendererProvider(this, layerRenderer);
    }

    @Override
    protected AzLayerRenderer<UUID, T> createLayerRenderer(AzRendererConfig<UUID, T> config) {
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
        AnimatableTexture.setAndUpdate(config.textureLocation(context().currentEntity(), entity));
    }

    /**
     * Called before rendering the model to buffer. Allows for render modifications and preparatory work such as scaling
     * and translating.<br>
     * {@link PoseStack} translations made here are kept until the end of the render process
     */
    @Override
    public void preRender(AzRendererPipelineContext<UUID, T> context, boolean isReRender) {
        PoseStack poseStack = context.poseStack();
        this.entityRenderTranslations.load(poseStack.last().pose());

        AzEntityRendererConfig<T> config = entityRenderer.config();
        float scaleWidth = config.scaleWidth(context.animatable());
        float scaleHeight = config.scaleHeight(context.animatable());

        scaleModelForRender(context, scaleWidth, scaleHeight, isReRender);
        if (config.alpha(context.animatable()) < 1 || context.animatable().isInvisible()) {
            float setAlpha = context.animatable().isInvisible()
                ? (context.animatable()
                    .isInvisibleToPlayer(
                        Minecraft.getMinecraft().player
                    ) ? 0.0F : 0.38F)
                : config.alpha(context.animatable());
            context.setAlpha(setAlpha);
        }
        config.preRenderEntry(context);
    }

    @Override
    public void postRender(AzRendererPipelineContext<UUID, T> context, boolean isReRender) {
        config.postRenderEntry(context);
    }

    /**
     * Renders the final frame of the entity, including handling special cases such as entities with leashes.
     *
     * @param context the rendering context that contains all required data for rendering, such as the entity, pose
     *                stack, light information, and buffer source
     */
    @Override
    public void renderFinal(AzRendererPipelineContext<UUID, T> context) {
        MultiBufferSource bufferSource = context.multiBufferSource();
        T entity = context.animatable();
        int packedLight = context.packedLight();
        float partialTick = context.partialTick();
        PoseStack poseStack = context.poseStack();

        if (bufferSource instanceof AzBufferSource) {
            ((AzBufferSource) bufferSource).endBatch();
        }

        entityRenderer.superRender(entity, 0, partialTick, poseStack, bufferSource, packedLight);

        if (bufferSource instanceof AzBufferSource) {
            ((AzBufferSource) bufferSource).endBatch();
        }

        if (!(entity instanceof EntityLiving)) {
            return;
        }

        EntityLiving mob = (EntityLiving) entity;

        Entity leashHolder = mob.getLeashHolder();

        if (leashHolder == null) {
            return;
        }

        AzEntityLeashRenderUtil.renderLeash(entityRenderer, mob, partialTick, poseStack, bufferSource, leashHolder);
    }

    @Override
    protected void doPostRenderCleanup(AzRendererPipelineContext<UUID, T> context) {
        context.setCurrentEntity(null);
    }

    public AzEntityRenderer<T> getRenderer() {
        return entityRenderer;
    }
}
