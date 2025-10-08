package mod.azure.azurelib.rewrite.render.block;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.rewrite.render.AzRendererPipeline;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;

/**
 * Represents a specialized rendering context for handling {@link BlockEntity} rendering in a pipeline-based rendering
 * framework. This class extends {@link AzRendererPipelineContext} to provide specific functionality tailored to block
 * entities within the AzureLib rendering system.
 *
 * @param <T> The type of {@link BlockEntity} to be rendered.
 */
public class AzBlockEntityRendererPipelineContext<T extends BlockEntity> extends AzRendererPipelineContext<T> {

    public AzBlockEntityRendererPipelineContext(AzRendererPipeline<T> rendererPipeline) {
        super(rendererPipeline);
    }

    @Override
    public @NotNull RenderType getDefaultRenderType(
        T animatable,
        ResourceLocation texture,
        @Nullable MultiBufferSource bufferSource,
        float partialTick,
        RenderType defaultRenderType
    ) {
        return defaultRenderType;
    }
}
