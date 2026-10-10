package mod.azure.azurelib.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.AzProvider;
import mod.azure.azurelib.render.lod.AzLodConfig;
import mod.azure.azurelib.render.lod.AzLodManager;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.math.Vec3;

/**
 * Base entity renderer for AzureLib-animated entities on 1.12.2.
 * <p>
 * Register it like any other 1.12.2 renderer, e.g.
 * {@code RenderingRegistry.registerEntityRenderingHandler(MyEntity.class, MyEntityRenderer::new)} during client
 * pre-init, where {@code MyEntityRenderer(RenderManager)} calls {@code super(config, renderManager)}.
 */
public abstract class AzEntityRenderer<T extends Entity> extends Render<T> {

    private final AzEntityRendererConfig<T> config;

    protected final AzProvider<UUID, T> provider;

    protected final AzEntityRendererPipeline<T> rendererPipeline;

    @Nullable
    private AzEntityAnimator<T> reusedAzEntityAnimator;

    private final Map<T, AzLodManager> lodManagers = new WeakHashMap<>();

    private boolean animateThisFrame = true;

    protected AzEntityRenderer(AzEntityRendererConfig<T> config, RenderManager renderManager) {
        super(renderManager);
        this.config = config;
        this.provider = new AzProvider<>(config::createAnimator, config::modelLocation, Entity::getUniqueID);
        this.rendererPipeline = createPipeline(config);
    }

    protected AzEntityRendererPipeline<T> createPipeline(AzEntityRendererConfig<T> config) {
        return new AzEntityRendererPipeline<>(config, this);
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull T animatable) {
        return config.textureLocation(animatable, animatable);
    }

    /**
     * Runs vanilla's {@link Render#doRender} (name tag), called from the pipeline once the model has been drawn. The GL
     * matrix is already translated to the entity, so the position passed to vanilla is the origin.
     */
    public void superRender(
        @Nonnull T entity,
        float entityYaw,
        float partialTick,
        @Nonnull PoseStack poseStack,
        @Nonnull MultiBufferSource bufferSource,
        int packedLight
    ) {
        super.doRender(entity, 0, 0, 0, entityYaw, partialTick);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doRender(@Nonnull T entity, double x, double y, double z, float entityYaw, float partialTick) {
        AzEntityAnimator<T> cachedEntityAnimator = (AzEntityAnimator<T>) provider.provideAnimator(entity, entity);
        AzBakedModel azBakedModel = provider.provideBakedModel(entity, entity);

        this.shadowSize = config.shadowRadius(entity);
        reusedAzEntityAnimator = cachedEntityAnimator;
        animateThisFrame = updateLod(entity, cachedEntityAnimator, azBakedModel);

        AzBufferSource bufferSource = AzBufferSource.getInstance();
        bufferSource.endBatch();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        try {
            rendererPipeline.render(
                new PoseStack(),
                azBakedModel,
                entity,
                bufferSource,
                null,
                null,
                entityYaw,
                partialTick,
                entity.getBrightnessForRender()
            );
            bufferSource.endBatch();
        } finally {
            if (this.renderOutlines) {
                GlStateManager.disableOutlineMode();
                GlStateManager.disableColorMaterial();
            }

            GlStateManager.popMatrix();
        }
    }

    protected boolean updateLod(T entity, @Nullable AzEntityAnimator<T> animator, @Nullable AzBakedModel bakedModel) {
        AzLodConfig lodConfig = config.lodConfig();

        if (lodConfig == AzLodConfig.DISABLED || bakedModel == null || animator == null) {
            return true;
        }

        if (
            animator.context() == null || animator.context().boneCache().isEmpty() || animator.context()
                .boneCache()
                .getBakedModel() != bakedModel
        ) {
            return true;
        }

        AzLodManager manager = lodManagers.get(entity);

        if (manager == null) {
            manager = new AzLodManager(lodConfig);
            lodManagers.put(entity, manager);
        }

        return manager.update(entity, bakedModel);
    }

    @Override
    protected boolean canRenderName(@Nonnull T entity) {
        return AzEntityNameRenderUtil.shouldShowName(renderManager, entity);
    }

    /**
     * Offset applied to bone world-space matrices, mirroring 1.18's {@code EntityRenderer#getRenderOffset}.
     */
    public Vec3 getRenderOffset(T entity, float partialTick) {
        return Vec3.ZERO;
    }

    public AzEntityAnimator<T> getAnimator() {
        return reusedAzEntityAnimator;
    }

    public boolean shouldAnimateThisFrame() {
        return animateThisFrame;
    }

    public AzEntityRendererConfig<T> config() {
        return config;
    }

    public RenderManager getRenderManager() {
        return renderManager;
    }
}
