package mod.azure.azurelib.render.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;

import javax.annotation.Nullable;

import mod.azure.azurelib.animation.impl.AzBlockAnimator;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.render.AzProvider;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.GlStateManager;
import mod.azure.azurelib.render.vertex.LightTexture;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.math.BlockPos;

/**
 * Base block entity renderer for AzureLib-animated block entities on Beta 1.7.3. Register it from your
 * {@code client-init} entrypoint with
 * {@code AzBlockEntityRendererRegistry.register(MyBlockEntity.class, MyBlockEntityRenderer::new)}.
 *
 * @see mod.azure.azurelib.render.registry.AzBlockEntityRendererRegistry
 */
public abstract class AzBlockEntityRenderer<T extends BlockEntity> extends BlockEntityRenderer {

    private final AzProvider<Long, T> provider;

    private final AzBlockEntityRendererPipeline<T> rendererPipeline;

    @Nullable
    private AzBlockAnimator<T> reusedAzBlockAnimator;

    protected AzBlockEntityRenderer(AzBlockEntityRendererConfig<T> config) {
        this.provider = new AzProvider<>(
            config::createAnimator,
            config::modelLocation,
            blockEntity -> BlockPos.of(blockEntity).toLong()
        );
        this.rendererPipeline = createPipeline(config);
    }

    protected AzBlockEntityRendererPipeline<T> createPipeline(AzBlockEntityRendererConfig<T> config) {
        return new AzBlockEntityRendererPipeline<>(config, this);
    }

    /**
     * Beta's block entity render entry point. MAPPING: BlockEntityRenderer#render(BlockEntity, double, double, double,
     * float). MCP: TileEntitySpecialRenderer.renderTileEntityAt.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void render(BlockEntity tileEntity, double x, double y, double z, float partialTick) {
        mod.azure.azurelib.util.client.AzRenderTick.set(partialTick);
        T entity = (T) tileEntity;
        AzBlockAnimator<T> cachedEntityAnimator = (AzBlockAnimator<T>) provider.provideAnimator(
            rendererPipeline.context().currentEntity(),
            entity
        );
        AzBakedModel model = provider.provideBakedModel(rendererPipeline.context().currentEntity(), entity);
        reusedAzBlockAnimator = cachedEntityAnimator;

        int packedLight = Mc.world(entity) != null
            ? LightTexture.fromBrightness(Mc.brightness(Mc.world(entity), Mc.x(entity), Mc.y(entity), Mc.z(entity)))
            : LightTexture.FULL_BRIGHT;

        AzBufferSource bufferSource = AzBufferSource.getInstance();
        bufferSource.endBatch();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        try {
            rendererPipeline.render(
                new PoseStack(),
                model,
                entity,
                bufferSource,
                null,
                null,
                0,
                partialTick,
                packedLight
            );
            bufferSource.endBatch();
        } finally {
            GlStateManager.popMatrix();
        }
    }

    public AzBlockAnimator<T> getAnimator() {
        return reusedAzBlockAnimator;
    }
}
