package mod.azure.azurelib.render.entity;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.hitbox.AzHitboxHolder;
import mod.azure.azurelib.hitbox.client.AzHitboxClientHooks;
import mod.azure.azurelib.hitbox.client.AzHitboxClientTicker;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.render.AzProvider;
import mod.azure.azurelib.render.lod.AzLodConfig;
import mod.azure.azurelib.render.lod.AzLodManager;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.GlStateManager;
import mod.azure.azurelib.render.vertex.LightTexture;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.ResourceLocation;
import mod.azure.azurelib.util.client.AzRenderTick;
import mod.azure.azurelib.util.math.Vec3;

/**
 * Base entity renderer for AzureLib-animated entities on Beta 1.7.3.
 * <p>
 * Register it from your {@code client-init} entrypoint with
 * {@code AzEntityRendererRegistry.register(MyEntity.class, MyEntityRenderer::new)}. Renderers are created without a
 * dispatcher; the registry assigns it when it installs the renderer.
 *
 * @see mod.azure.azurelib.render.registry.AzEntityRendererRegistry
 */
public abstract class AzEntityRenderer<T extends Entity> extends EntityRenderer {

    private final AzEntityRendererConfig<T> config;

    protected final AzProvider<UUID, T> provider;

    protected final AzEntityRendererPipeline<T> rendererPipeline;

    @Nullable
    private AzEntityAnimator<T> reusedAzEntityAnimator;

    private final Map<T, AzLodManager> lodManagers = new WeakHashMap<>();

    private boolean animateThisFrame = true;

    protected AzEntityRenderer(AzEntityRendererConfig<T> config) {
        this.config = config;
        this.provider = new AzProvider<>(config::createAnimator, config::modelLocation, Mc::entityUuid);
        this.rendererPipeline = createPipeline(config);
    }

    public void poseOffscreen(T entity, float partialTick) {
        if (!(rendererPipeline.modelRenderer() instanceof AzEntityModelRenderer)) {
            return;
        }

        AzEntityModelRenderer<T> modelRenderer = (AzEntityModelRenderer<T>) rendererPipeline.modelRenderer();
        AzEntityAnimator<T> animator = (AzEntityAnimator<T>) provider.provideAnimator(entity, entity);
        AzBakedModel azBakedModel = provider.provideBakedModel(entity, entity);

        if (azBakedModel == null) {
            return;
        }

        AzEntityAnimator<T> previousAnimator = reusedAzEntityAnimator;
        reusedAzEntityAnimator = animator;

        try {
            modelRenderer.poseBonesOffscreen(entity, azBakedModel, animator, partialTick);
            AzHitboxClientHooks.afterOffscreenPose(entity, azBakedModel, partialTick);
        } finally {
            reusedAzEntityAnimator = previousAnimator;
        }
    }

    protected AzEntityRendererPipeline<T> createPipeline(AzEntityRendererConfig<T> config) {
        return new AzEntityRendererPipeline<>(config, this);
    }

    /** The entity's texture. Beta's renderers bind textures by path themselves, so this is not a vanilla override. */
    @SuppressWarnings("unchecked")
    protected ResourceLocation getEntityTexture(Entity animatable) {
        return config.textureLocation((T) animatable, (T) animatable);
    }

    /**
     * Renders the name tag, called from the pipeline once the model has been drawn. The GL matrix is already translated
     * to the entity, so the label is drawn at the origin.
     */
    public void superRender(
        @Nonnull T entity,
        float entityYaw,
        float partialTick,
        @Nonnull PoseStack poseStack,
        @Nonnull MultiBufferSource bufferSource,
        int packedLight
    ) {
        if (AzEntityNameRenderUtil.shouldShowName(entity)) {
            AzEntityNameRenderUtil.renderNameTag(entity, 0, 0, 0);
        }
    }

    /**
     * Beta's entity render entry point. MAPPING: EntityRenderer#render(Entity, double, double, double, float, float).
     * MCP: Render.doRender.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void render(Entity rawEntity, double x, double y, double z, float entityYaw, float partialTick) {
        AzRenderTick.set(partialTick);
        T entity = (T) rawEntity;
        AzEntityAnimator<T> cachedEntityAnimator = (AzEntityAnimator<T>) provider.provideAnimator(entity, entity);
        AzBakedModel azBakedModel = provider.provideBakedModel(entity, entity);

        this.shadowSize = config.shadowRadius(entity);
        reusedAzEntityAnimator = cachedEntityAnimator;
        animateThisFrame = updateLod(entity, cachedEntityAnimator, azBakedModel);

        AzBufferSource bufferSource = AzBufferSource.getInstance();
        bufferSource.endBatch();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

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
                LightTexture.fromBrightness(Mc.brightness(entity, partialTick))
            );
            bufferSource.endBatch();
            if (entity instanceof AzHitboxHolder && azBakedModel != null) {
                AzHitboxClientHooks.afterEntityRender(
                    entity,
                    azBakedModel,
                    partialTick,
                    AzHitboxClientTicker.isDebugEnabled()
                );
            }
        } finally {
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

    public EntityRenderDispatcher getRenderManager() {
        return this.dispatcher;
    }
}
