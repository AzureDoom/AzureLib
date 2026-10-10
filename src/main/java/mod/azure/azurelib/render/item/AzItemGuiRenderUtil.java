package mod.azure.azurelib.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.render.vertex.RenderType;
import mod.azure.azurelib.render.vertex.VertexConsumer;

/**
 * Utility for rendering items in a GUI context.
 */
public class AzItemGuiRenderUtil {

    /**
     * Renders an item in the GUI. Items configured with {@code useEntityGuiLighting()} get world-style entity lighting;
     * everything else keeps 1.12.2's standard GUI item lighting.
     */
    public static void renderInGui(
        AzItemRendererConfig config,
        AzItemRendererPipeline rendererPipeline,
        ItemStack stack,
        AzBakedModel model,
        ItemStack currentItemStack,
        PoseStack poseStack,
        MultiBufferSource source,
        int packedLight
    ) {
        if (config.useEntityGuiLighting()) {
            RenderHelper.enableStandardItemLighting();
        } else {
            RenderHelper.enableGUIStandardItemLighting();
        }

        AzItemRendererPipelineContext context = (AzItemRendererPipelineContext) rendererPipeline.context();
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        RenderType renderType = context.getDefaultRenderType(
            stack,
            config.textureLocation(context.currentEntity(), stack),
            source,
            partialTick,
            config.getRenderType(context.currentEntity(), stack),
            config.alpha(stack)
        );
        boolean withGlint = currentItemStack != null && currentItemStack.hasEffect();
        VertexConsumer buffer = AzBufferSource.getFoilBuffer(source, renderType, withGlint);

        poseStack.pushPose();
        rendererPipeline.render(poseStack, model, stack, source, renderType, buffer, 0, partialTick, packedLight);

        if (source instanceof AzBufferSource) {
            ((AzBufferSource) source).endBatch();
        }

        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        poseStack.popPose();
    }
}
