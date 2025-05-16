package mod.azure.azurelib.rewrite.render.block;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import mod.azure.azurelib.rewrite.render.AzRendererPipeline;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;

/**
 * Represents a specialized rendering context for handling {@link TileEntity} rendering in a pipeline-based rendering
 * framework. This class extends {@link AzRendererPipelineContext} to provide specific functionality tailored to block
 * entities within the AzureLib rendering system.
 *
 * @param <T> The type of {@link TileEntity} to be rendered.
 */
public class AzBlockEntityRendererPipelineContext<T extends TileEntity> extends AzRendererPipelineContext<T> {

    public AzBlockEntityRendererPipelineContext(AzRendererPipeline<T> rendererPipeline) {
        super(rendererPipeline);
    }

    @Override
    public RenderType getDefaultRenderType(
        T animatable,
        ResourceLocation texture,
        IRenderTypeBuffer bufferSource,
        float partialTick
    ) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
