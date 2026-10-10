package mod.azure.azurelib.render.block;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

import mod.azure.azurelib.animation.impl.AzBlockAnimator;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.AzProvider;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.GlStateManager;
import mod.azure.azurelib.render.vertex.LightTexture;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.math.BlockPos;

/**
 * Base tile entity renderer for AzureLib-animated tile entities on 1.7.10. Register it with
 * {@code ClientRegistry.bindTileEntitySpecialRenderer(MyTileEntity.class, new MyRenderer())}.
 */
public abstract class AzBlockEntityRenderer<T extends TileEntity> extends TileEntitySpecialRenderer {

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

    @Override
    @SuppressWarnings("unchecked")
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick) {
        T entity = (T) tileEntity;
        AzBlockAnimator<T> cachedEntityAnimator = (AzBlockAnimator<T>) provider.provideAnimator(
            rendererPipeline.context().currentEntity(),
            entity
        );
        AzBakedModel model = provider.provideBakedModel(rendererPipeline.context().currentEntity(), entity);
        reusedAzBlockAnimator = cachedEntityAnimator;

        int packedLight = entity.hasWorldObj()
            ? entity.getWorld().getLightBrightnessForSkyBlocks(entity.xCoord, entity.yCoord, entity.zCoord, 0)
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
