package mod.azure.azurelib.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.animation.impl.AzBlockAnimator;
import mod.azure.azurelib.render.AzBufferSource;
import mod.azure.azurelib.render.AzProvider;

/**
 * The {@code AzBlockEntityRenderer} class is an abstract base class for rendering custom block entities. It leverages
 * an animation and rendering pipeline mechanism to provide extended functionalities, such as dynamic animations and
 * model customization.
 * <p>
 * As with {@code AzEntityRenderer}, the AzureLib pipeline runs during extraction against an identity pose and records
 * its vertices; {@link #submit} only replays them.
 *
 * @param <T> The specific type of {@link BlockEntity} that this renderer processes.
 */
public abstract class AzBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, AzBlockEntityRenderState> {

    private final AzProvider<Long, T> provider;

    private final AzBlockEntityRendererPipeline<T> rendererPipeline;

    @Nullable
    private AzBlockAnimator<T> reusedAzBlockAnimator;

    protected AzBlockEntityRenderer(AzBlockEntityRendererConfig<T> config) {
        this.provider = new AzProvider<>(
            config::createAnimator,
            config::modelLocation,
            blockEntity -> blockEntity.getBlockPos().asLong()
        );
        this.rendererPipeline = createPipeline(config);
    }

    protected AzBlockEntityRendererPipeline<T> createPipeline(AzBlockEntityRendererConfig<T> config) {
        return new AzBlockEntityRendererPipeline<>(config, this);
    }

    @Override
    public @NotNull AzBlockEntityRenderState createRenderState() {
        return new AzBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(
        @NotNull T blockEntity,
        @NotNull AzBlockEntityRenderState state,
        float partialTick,
        @NotNull Vec3 cameraPosition,
        @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);

        var cachedEntityAnimator = (AzBlockAnimator<T>) provider.provideAnimator(
            rendererPipeline.context().currentEntity(),
            blockEntity
        );
        var model = provider.provideBakedModel(rendererPipeline.context().currentEntity(), blockEntity);

        // Point the renderer's current animator reference to the cached entity animator before rendering.
        reusedAzBlockAnimator = cachedEntityAnimator;

        var geometry = new AzBufferSource();
        state.geometry = geometry;

        // Runs against an identity pose during extraction; the vertices recorded are block-local and get
        // translated/replayed relative to the camera during submit.
        rendererPipeline.render(
            new PoseStack(),
            model,
            blockEntity,
            geometry,
            null,
            null,
            0,
            partialTick,
            state.lightCoords
        );
    }

    @Override
    public void submit(
        @NotNull AzBlockEntityRenderState state,
        @NotNull PoseStack poseStack,
        @NotNull SubmitNodeCollector collector,
        @NotNull CameraRenderState cameraRenderState
    ) {
        if (state.geometry != null) {
            state.geometry.submitAll(collector, poseStack);
        }
    }

    public AzBlockAnimator<T> getAnimator() {
        return reusedAzBlockAnimator;
    }
}
