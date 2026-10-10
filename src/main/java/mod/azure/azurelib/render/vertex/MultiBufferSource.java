package mod.azure.azurelib.render.vertex;

/**
 * Provides a {@link VertexConsumer} per {@link RenderType}, mirroring the 1.18
 * {@code net.minecraft.client.renderer.MultiBufferSource} API. See {@link AzBufferSource} for the 1.7.10
 * implementation.
 */
public interface MultiBufferSource {

    VertexConsumer getBuffer(RenderType renderType);
}
