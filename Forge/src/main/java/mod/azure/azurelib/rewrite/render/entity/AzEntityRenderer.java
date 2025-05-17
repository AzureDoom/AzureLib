package mod.azure.azurelib.rewrite.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import mod.azure.azurelib.rewrite.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.rewrite.model.AzBakedModel;
import mod.azure.azurelib.rewrite.render.AzProvider;

/**
 * AzEntityRenderer is an abstract class responsible for rendering entities in the game. It extends the base
 * functionality of {@link EntityRenderer} to provide additional rendering capabilities specific to animated and custom
 * entities. This class is parameterized with a generic type {@code T}, which must extend {@link Entity}. It integrates
 * several abstractions such as animation management, model caching, and advanced rendering pipelines for handling
 * complex rendering behavior. Users are expected to configure this renderer using an {@link AzEntityRendererConfig}.
 * Key components: - {@link AzEntityRendererConfig}: Defines configuration options such as textures, models, and
 * animator providers. - {@link AzProvider}: Supplies baked models and animators for entities. -
 * {@link AzEntityRendererPipeline}: Manages rendering logic through a custom pipeline.
 */
public abstract class AzEntityRenderer<T extends Entity> extends EntityRenderer<T> {

    private final AzEntityRendererConfig<T> config;

    private final AzProvider<T> provider;

    private final AzEntityRendererPipeline<T> rendererPipeline;

    private AzEntityAnimator<T> reusedAzEntityAnimator;

    protected AzEntityRenderer(AzEntityRendererConfig<T> config, EntityRendererManager context) {
        super(context);
        this.config = config;
        this.provider = new AzProvider<>(config::createAnimator, config::modelLocation);
        this.rendererPipeline = createPipeline(config);
    }

    protected AzEntityRendererPipeline<T> createPipeline(AzEntityRendererConfig<T> config) {
        return new AzEntityRendererPipeline<>(config, this);
    }

    @Override
    public final ResourceLocation getEntityTexture(T animatable) {
        return config.textureLocation(animatable);
    }

    public void superRender(
        T entity,
        float entityYaw,
        float partialTick,
        MatrixStack poseStack,
        IRenderTypeBuffer bufferSource,
        int packedLight
    ) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public void render(
        T entity,
        float entityYaw,
        float partialTick,
        MatrixStack poseStack,
        IRenderTypeBuffer bufferSource,
        int packedLight
    ) {
        AzEntityAnimator<T> cachedEntityAnimator = (AzEntityAnimator<T>) provider.provideAnimator(entity);
        AzBakedModel azBakedModel = provider.provideBakedModel(entity);

        if (cachedEntityAnimator != null && azBakedModel != null) {
            cachedEntityAnimator.setActiveModel(azBakedModel);
        }

        this.shadowOpaque = config.shadowRadius(entity);

        // Point the renderer's current animator reference to the cached entity animator before rendering.
        reusedAzEntityAnimator = cachedEntityAnimator;

        // Execute the render pipeline.
        rendererPipeline.render(
            poseStack,
            azBakedModel,
            entity,
            bufferSource,
            null,
            null,
            entityYaw,
            partialTick,
            packedLight
        );
    }

    /**
     * Whether the entity's nametag should be rendered or not.<br>
     * Pretty much exclusively used in {@link EntityRenderer#renderName}
     */
    @Override
    public boolean canRenderName(T entity) {
        return AzEntityNameRenderUtil.shouldShowName(renderManager, entity);
    }

    // Proxy method override for super.getBlockLightLevel external access.
    @Override
    protected int getBlockLight(T entity, float pos) {
        return super.getBlockLight(entity, pos);
    }

    public AzEntityAnimator<T> getAnimator() {
        return reusedAzEntityAnimator;
    }

    public AzEntityRendererConfig<T> config() {
        return config;
    }
}
