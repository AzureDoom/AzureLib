package mod.azure.azurelib.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import mod.azure.azurelib.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.render.AzBufferSource;
import mod.azure.azurelib.render.AzProvider;

/**
 * AzEntityRenderer is an abstract class responsible for rendering entities in the game. It extends the base
 * functionality of {@link EntityRenderer} to provide additional rendering capabilities specific to animated and custom
 * entities. This class is parameterized with a generic type {@code T}, which must extend {@link Entity}. It integrates
 * several abstractions such as animation management, model caching, and advanced rendering pipelines for handling
 * complex rendering behavior. Users are expected to configure this renderer using an {@link AzEntityRendererConfig}.
 * Key components: - {@link AzEntityRendererConfig}: Defines configuration options such as textures, models, and
 * animator providers. - {@link AzProvider}: Supplies baked models and animators for entities. -
 * {@link AzEntityRendererPipeline}: Manages rendering logic through a custom pipeline.
 * <p>
 * Under 26.2's two-phase model the entire AzureLib pipeline — animation, procedural passes, geometry emission and leash
 * — runs inside {@link #extractRenderState}, where reading the entity is legal. The resulting vertices are recorded
 * into the {@link AzEntityRenderState}'s {@link AzBufferSource} and replayed in {@link #submit}.
 */
public abstract class AzEntityRenderer<T extends Entity> extends EntityRenderer<T, AzEntityRenderState> {

    protected final AzEntityRendererConfig<T> config;

    protected final AzProvider<UUID, T> provider;

    protected final AzEntityRendererPipeline<T> rendererPipeline;

    @Nullable
    private AzEntityAnimator<T> reusedAzEntityAnimator;

    private Vec3 currentRenderOffset = Vec3.ZERO;

    protected AzEntityRenderer(AzEntityRendererConfig<T> config, EntityRendererProvider.Context context) {
        super(context);
        this.config = config;
        this.provider = new AzProvider<>(config::createAnimator, config::modelLocation, Entity::getUUID);
        this.rendererPipeline = createPipeline(config);
    }

    public AzEntityRendererPipeline<T> createPipeline(AzEntityRendererConfig<T> config) {
        return new AzEntityRendererPipeline<>(config, this);
    }

    public @NotNull Identifier getTextureLocation(@NotNull T animatable) {
        return config.textureLocation(animatable, animatable);
    }

    @Override
    public @NotNull AzEntityRenderState createRenderState() {
        return new AzEntityRenderState();
    }

    @Override
    public void extractRenderState(@NotNull T entity, @NotNull AzEntityRenderState state, float partialTick) {
        this.shadowRadius = config.shadowRadius(entity);

        super.extractRenderState(entity, state, partialTick);

        var cachedEntityAnimator = (AzEntityAnimator<T>) provider.provideAnimator(entity, entity);
        var azBakedModel = provider.provideBakedModel(entity, entity);

        // Point the renderer's current animator reference to the cached entity animator before rendering.
        reusedAzEntityAnimator = cachedEntityAnimator;
        this.currentRenderOffset = getRenderOffset(state);

        var geometry = new AzBufferSource();
        state.geometry = geometry;

        // The pipeline still runs eagerly here (during extract, against an identity pose); the vertices it
        // records are entity-local and get translated/replayed relative to the camera during submit.
        rendererPipeline.render(
            new PoseStack(),
            azBakedModel,
            entity,
            geometry,
            null,
            null,
            bodyYaw(entity, partialTick),
            partialTick,
            state.lightCoords
        );
    }

    @Override
    public void submit(
        @NotNull AzEntityRenderState state,
        @NotNull PoseStack poseStack,
        @NotNull SubmitNodeCollector collector,
        @NotNull CameraRenderState cameraRenderState
    ) {
        super.submit(state, poseStack, collector, cameraRenderState);

        if (state.geometry != null) {
            state.geometry.submitAll(collector, poseStack);
        }
    }

    private static float bodyYaw(Entity entity, float partialTick) {
        if (entity instanceof LivingEntity living) {
            return Mth.rotLerp(partialTick, living.yBodyRotO, living.yBodyRot);
        }

        return Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
    }

    public Vec3 currentRenderOffset() {
        return currentRenderOffset;
    }

    /**
     * Whether the entity's nametag should be rendered or not.<br>
     */
    @Override
    protected boolean shouldShowName(@NotNull T entity, double distanceToCameraSq) {
        return AzEntityNameRenderUtil.shouldShowName(entityRenderDispatcher, entity);
    }

    @Override
    public int getBlockLightLevel(@NotNull T entity, @NotNull BlockPos pos) {
        return super.getBlockLightLevel(entity, pos);
    }

    public AzEntityAnimator<T> getAnimator() {
        return reusedAzEntityAnimator;
    }

    public AzEntityRendererConfig<T> config() {
        return config;
    }
}
