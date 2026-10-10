package mod.azure.azurelib.render.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.RenderType;

/**
 * Represents a specialized rendering context for handling {@link TileEntity} rendering in a pipeline-based rendering
 * framework. This class extends {@link AzRendererPipelineContext} to provide specific functionality tailored to block
 * entities within the AzureLib rendering system.
 *
 * @param <T> The type of {@link TileEntity} to be rendered.
 */
public class AzBlockEntityRendererPipelineContext<T extends TileEntity> extends AzRendererPipelineContext<Long, T> {

    public AzBlockEntityRendererPipelineContext(AzRendererPipeline<Long, T> rendererPipeline) {
        super(rendererPipeline);
    }

    @Override
    public @Nonnull RenderType getDefaultRenderType(
        T animatable,
        ResourceLocation texture,
        @Nullable MultiBufferSource bufferSource,
        float partialTick,
        RenderType defaultRenderType,
        float alpha
    ) {
        return defaultRenderType;
    }
}
