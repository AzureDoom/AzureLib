package mod.azure.azurelib.rewrite.render.layer;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import mod.azure.azurelib.cache.texture.AutoGlowingTexture;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * A {@link AzRenderLayer} dedicated to rendering the auto-generated glow layer functionality provided by AzureLib. This
 * utilizes texture files with the <i>_glowing</i> suffix to create glowing effects for models.
 */
public class AzAutoGlowingLayer<T> implements AzRenderLayer<T> {

    @Override
    public void preRender(AzRendererPipelineContext<T> context) {}

    /**
     * Handles the main rendering logic for the animatable object in the pipeline context. This includes switching to a
     * custom {@link RenderType} for glowing textures and rendering the object using the pipeline's re-render mechanism.
     * <p>
     * The rendering context's state is modified temporarily to apply a custom render type and packed light. After
     * rendering, the context is restored to its original state for consistency.
     * </p>
     *
     * @param context the rendering pipeline context, containing the animatable object and rendering state
     */
    @Override
    public void render(AzRendererPipelineContext<T> context) {
        T animatable = context.animatable();
        mod.azure.azurelib.rewrite.render.AzRendererPipeline<T> renderPipeline = context.rendererPipeline();
        ResourceLocation textureLocation = renderPipeline.config().textureLocation(animatable);
        RenderType renderType = AutoGlowingTexture.getRenderType(textureLocation);

        if (context.animatable() instanceof Entity) {
            Entity entity = (Entity) context.animatable();
            boolean isInvisibleButVisibleToPlayer = entity.isInvisible() && !entity.isInvisibleToPlayer(
                ClientUtils.getClientPlayer()
            );
            boolean shouldAppearGlowing = Minecraft.getInstance().pointedEntity.isGlowing();

            if (isInvisibleButVisibleToPlayer) {
                renderType = RenderType.getOutline(textureLocation);
            } else if (shouldAppearGlowing) {
                renderType = AutoGlowingTexture.getOutlineRenderType(textureLocation);
            }
        }

        if (context.renderType() != null) {
            RenderType prevRenderType = context.renderType();
            int prevPackedLight = context.packedLight();
            IVertexBuilder prevVertexConsumer = context.vertexConsumer();

            context.setRenderType(renderType);
            context.setPackedLight(0xF00000);
            context.setVertexConsumer(context.multiBufferSource().getBuffer(renderType));

            renderPipeline.reRender(context);

            // Restore context for sanity
            // TODO: Should probably cache the context as a whole somewhere and then restore it (a "previous" context).
            context.setRenderType(prevRenderType);
            context.setPackedLight(prevPackedLight);
            context.setVertexConsumer(prevVertexConsumer);
        }
    }

    @Override
    public void renderForBone(AzRendererPipelineContext<T> context, AzBone bone) {}
}
