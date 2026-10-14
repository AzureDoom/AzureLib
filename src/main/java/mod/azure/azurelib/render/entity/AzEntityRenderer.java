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
 * Base entity renderer for AzureLib-animated entities on 1.7.10.
 * <p>
 * Register it like any other 1.7.10 renderer, e.g.
 * {@code RenderingRegistry.registerEntityRenderingHandler(MyEntity.class, new MyEntityRenderer())} during client init.
 * 1.7.10 renderers are created without a {@link RenderManager}; Minecraft assigns it when the renderer is registered.
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
     */
    @Override
    @SuppressWarnings("unchecked")
    public void render(Entity rawEntity, double x, double y, double z, float entityYaw, float partialTick) {
        AzRenderTick.set(partialTick);
        T entity = (T) rawEntity;
        AzEntityAnimator<T> cachedEntityAnimator = (AzEntityAnimator<T>) provider.provideAnimator(entity, entity);
        AzBakedModel azBakedModel = provider.provideBakedModel(entity, entity);

        this.shadowRadius = config.shadowRadius(entity);
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

    /** MAPPING: EntityRenderer#dispatcher. */
    public EntityRenderDispatcher getRenderManager() {
        return this.dispatcher;
    }
}
