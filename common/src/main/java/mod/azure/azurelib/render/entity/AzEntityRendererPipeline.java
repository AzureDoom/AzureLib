package mod.azure.azurelib.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.joml.Matrix4f;

import java.util.UUID;

import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.render.*;

/**
 * Represents a renderer pipeline specifically designed for rendering entities. This pipeline facilitates stages of
 * rendering where contextual work like pre-translations, texture animations, and leash rendering are managed within a
 * customizable structure.
 *
 * @param <T> The type of entity this renderer pipeline handles. Extends from the base {@link Entity}.
 */
public class AzEntityRendererPipeline<T extends Entity> extends AzRendererPipeline<UUID, T> {

    private final AzEntityRenderer<T> entityRenderer;

    public Matrix4f entityRenderTranslations = new Matrix4f();

    public Matrix4f modelRenderTranslations = new Matrix4f();

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
        var poseStack = context.poseStack();
        this.entityRenderTranslations.set(poseStack.last().pose());

        var config = entityRenderer.config();
        var scaleWidth = config.scaleWidth(context.animatable());
        var scaleHeight = config.scaleHeight(context.animatable());

        scaleModelForRender(context, scaleWidth, scaleHeight, isReRender);
        if (config.alpha(context.animatable()) < 1 || context.animatable().isInvisible()) {
            var setAlpha = context.animatable().isInvisible()
                ? (context.animatable()
                    .isInvisibleTo(
                        Minecraft.getInstance().player
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
        var bufferSource = context.multiBufferSource();
        var entity = context.animatable();
        var packedLight = context.packedLight();
        var partialTick = context.partialTick();
        var poseStack = context.poseStack();

        entityRenderer.superRender(entity, 0, partialTick, poseStack, bufferSource, packedLight);

        if (!(entity instanceof Mob mob)) {
            return;
        }

        var leashHolder = mob.getLeashHolder();

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
